package io.csvflow.service;

import io.csvflow.models.Bases;
import io.csvflow.models.ImportErros;
import io.csvflow.repository.BasesRepository;
import io.csvflow.repository.ImportErrosRepository;
import io.csvflow.util.CSVUtils;
import io.csvflow.util.TypeUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FileImportService {

    private final BasesRepository basesRepository;
    private final ImportErrosRepository importErrosRepository;
    private final EntityManager entityManager;

    public FileImportService(BasesRepository basesRepository
            , EntityManager entityManager
            , ImportErrosRepository importErrosRepository
    ) {
        this.basesRepository = basesRepository;
        this.entityManager = entityManager;
        this.importErrosRepository = importErrosRepository;
    }

    @Async
    @Transactional
    public void importCSVFile(Long baseId, MultipartFile file) throws Exception {
        Bases base = getBaseById(baseId);
        String tableName = base.getNameTableTmp();
        List<CSVRecord> errorRecords = new ArrayList<>();

        Map<String, Integer> columnTypes = getTableColumnTypes(tableName);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            int batchSize = 1000;
            List<CSVRecord> batch = new ArrayList<>();

            for (CSVRecord record : csvParser) {
                processCSVRecord(record, columnTypes, baseId, batch, errorRecords);
                if (batch.size() >= batchSize) {
                    insertBatchRecords(batch, tableName, csvParser.getHeaderNames());
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                insertBatchRecords(batch, tableName, csvParser.getHeaderNames());
            }

        }

        handleErrorRecords(errorRecords, file.getOriginalFilename());
    }

    private void processCSVRecord(CSVRecord record
            , Map<String, Integer> columnTypes
            , Long baseId, List<CSVRecord> batch, List<CSVRecord> errorRecords) {
        if (isValidRecord(record, columnTypes, baseId)) {
            batch.add(record);
        } else {
            errorRecords.add(record);
        }
    }

    private void handleErrorRecords(List<CSVRecord> errorRecords, String fileName) throws IOException {
        if (!errorRecords.isEmpty()) {
            File errorLogFile = generateErrorLogFile(errorRecords, fileName);
        }
    }

    private boolean isValidRecord(CSVRecord record, Map<String, Integer> columnTypes, Long baseId) {
        for (Map.Entry<String, Integer> entry : columnTypes.entrySet()) {
            String columnName = entry.getKey();
            int type = entry.getValue();

            String value = record.get(columnName);
            if (value == null || value.isBlank()) continue;

            if (!TypeUtils.isValidValueForType(value, type)) {
                logImportError(baseId, columnName, value);
                return false;
            }
        }
        return true;
    }

    private void logImportError(Long baseId, String columnName, String value) {
        ImportErros importErro = new ImportErros();
        Bases base = getBaseById(baseId);
        importErro.setId_base(base);
        importErro.setErro("Erro ao processar coluna " + columnName + " com valor " + value);
        importErrosRepository.save(importErro);
    }

    @Transactional
    private void insertBatchRecords(List<CSVRecord> records, String tableName, List<String> headers) {
        String columns = String.join(", ", headers);
        String sql = String.format("INSERT INTO %s (%s) VALUES ", tableName, columns);

        String values = records.stream()
                .map(record -> headers.stream()
                        .map(header -> "'" + record.get(header).replace("'", "''") + "'")
                        .collect(Collectors.joining(", ")))
                .collect(Collectors.joining("), ("));

        String fullSql = sql + "(" + values + ")";
        entityManager.createNativeQuery(fullSql).executeUpdate();
    }

    private Map<String, Integer> getTableColumnTypes(String tableName) {
        String sql = "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = :tableName";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("tableName", tableName);

        List<Object[]> results = query.getResultList();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> mapSqlTypeToJavaType((String) row[1])
                ));
    }

    private int mapSqlTypeToJavaType(String sqlType) {
        switch (sqlType.toLowerCase()) {
            case "varchar", "text", "char":
                return Types.VARCHAR;
            case "int", "integer", "smallint":
                return Types.INTEGER;
            case "decimal", "numeric", "float", "double":
                return Types.DECIMAL;
            case "date", "datetime", "timestamp":
                return Types.TIMESTAMP;
            default:
                return Types.VARCHAR;
        }
    }

    private File generateErrorLogFile(List<CSVRecord> errorRecords, String fileName) throws IOException {
        File errorLog = File.createTempFile("log_erros_" + fileName, ".csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(errorLog))) {
            writer.write("Error Log\n");
            for (CSVRecord record : errorRecords) {
                writer.write(record.toString());
                writer.newLine();
            }
        }
        return errorLog;
    }

    private Bases getBaseById(Long baseId) {
        return basesRepository.findById(baseId)
                .orElseThrow(() -> new IllegalArgumentException("Base não encontrada com ID: " + baseId));
    }

    public File exportTableToCSV(Long baseId) throws IOException {
        Bases base = getBaseById(baseId);
        String tableName = base.getNameTableTmp();
        List<String> columnHeaders = getTableColumnHeaders(tableName);

        return CSVUtils.generateCSVFileWithHeadersOnly(columnHeaders);
    }

    private List<String> getTableColumnHeaders(String tableName) {
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = :tableName";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("tableName", tableName);

        return query.getResultList();
    }
}
