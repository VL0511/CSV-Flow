package io.csvflow.controller;

import io.csvflow.dto.BasesDTO;
import io.csvflow.exception.BaseNotFoundException;
import io.csvflow.exception.FileImportException;
import io.csvflow.models.Bases;
import io.csvflow.service.BasesService;
import io.csvflow.service.FileImportService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/v1/bases")
public class BasesController {

    private static final Logger log = LoggerFactory.getLogger(BasesController.class);
    private final BasesService basesService;
    private final FileImportService fileImportService;

    public BasesController(BasesService basesService, FileImportService fileImportService) {
        this.basesService = basesService;
        this.fileImportService =  fileImportService;
    }

    @PostMapping
    public ResponseEntity<Bases> save(@RequestBody @Valid BasesDTO basesDTO) {
        try {
            Bases savedBase = basesService.save(basesDTO);
            return ResponseEntity.created(URI.create("/v1/bases/" + savedBase.getId())).body(savedBase);
        } catch (Exception e) {
            log.error("Error saving base", e);
            throw new FileImportException("Error while saving base: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Bases>> findAll() {
        try {
            List<Bases> basesList = basesService.findAll();
            return ResponseEntity.ok(basesList);
        } catch (Exception e) {
            log.error("Error retrieving bases", e);
            throw new FileImportException("Error while fetching all bases: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bases> findById(@PathVariable Long id) {
        try {
            Bases base = basesService.findById(id);
            return ResponseEntity.ok(base);
        } catch (BaseNotFoundException e) {
            log.error("Base not found with ID: {}", id, e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bases> update(@PathVariable Long id, @RequestBody @Valid BasesDTO dto) {
        try {
            Bases updated = basesService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating base with ID: {}", id, e);
            throw new FileImportException("Error while updating base: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            basesService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting base with ID: {}", id, e);
            throw new FileImportException("Error while deleting base: " + e.getMessage());
        }
    }

    @GetMapping("/download/csv/{baseId}")
    public ResponseEntity<byte[]> downloadCsvFile(@PathVariable Long baseId) {
        try {
            File csvFile = fileImportService.exportTableToCSV(baseId);

            if (!csvFile.exists()) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = Files.readAllBytes(csvFile.toPath());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + csvFile.getName())
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                    .body(fileContent);

        } catch (IOException e) {
            log.error("Error downloading CSV file for base with ID: {}", baseId, e);
            throw new FileImportException("Error downloading CSV file: " + e.getMessage());
        }
    }

    @PostMapping("/import/{baseId}")
    public CompletableFuture<ResponseEntity<String>> importCSV(@PathVariable Long baseId, @RequestParam("file") MultipartFile file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                fileImportService.importCSVFile(baseId, file);
                return ResponseEntity.status(HttpStatus.OK).body("Importação realizada com sucesso!");
            } catch (FileImportException e) {
                log.error("Error importing CSV file for base with ID: {}", baseId, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao importar o arquivo: " + e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error during CSV import for base with ID: {}", baseId, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro inesperado ao importar o arquivo: " + e.getMessage());
            }
        });
    }
}