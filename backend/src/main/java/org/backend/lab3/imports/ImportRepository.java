package org.backend.lab3.imports;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportRepository extends JpaRepository<ImportLogEntity, Long> {
    List<ImportLogEntity> findByUserId(Long id);
}
