package io.csvflow.repository;

import  io.csvflow.models.ImportErros;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportErrosRepository extends JpaRepository<ImportErros, Long> {
}
