package io.csvflow.service;

import io.csvflow.dto.BasesDTO;
import io.csvflow.models.Bases;
import io.csvflow.repository.BasesRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BasesService {

    private final BasesRepository basesRepository;

    public BasesService(BasesRepository basesRepository) {
        this.basesRepository = basesRepository;
    }

    public List<Bases> findAll() {
        return basesRepository.findAll();
    }

    public Bases save(BasesDTO dto) {
        Bases base = convertToEntity(dto);
        return basesRepository.save(base);
    }

    public Bases findById(Long id) {
        return getBaseOrThrow(id);
    }

    public Bases update(Long id, BasesDTO dto) {
        Bases existing = getBaseOrThrow(id);
        updateBaseFields(existing, dto);
        return basesRepository.save(existing);
    }

    public void delete(Long id) {
        Bases existing = getBaseOrThrow(id);
        basesRepository.delete(existing);
    }

    public Optional<Bases> findByOptional(Long id) {
        return basesRepository.findById(id);
    }

    public Bases getBaseOrThrow(Long id) {
        return basesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Base não encontrada com ID: " + id));
    }

    private Bases convertToEntity(BasesDTO dto) {
        return Bases.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .nameTableTmp(dto.getNameTableTmp())
                .nameTableRaw(dto.getNameTableRaw())
                .build();
    }

    private void updateBaseFields(Bases existing, BasesDTO dto) {
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setNameTableTmp(dto.getNameTableTmp());
        existing.setNameTableRaw(dto.getNameTableRaw());
    }
}
