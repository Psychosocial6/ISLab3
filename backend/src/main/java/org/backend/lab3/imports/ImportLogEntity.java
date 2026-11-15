package org.backend.lab3.imports;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "import_logs")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class ImportLogEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ImportStatus status;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "added_count")
    private Long addedCount;
    @Column(name = "file_name")
    private String fileName;

    public ImportLogEntity(ImportStatus status, Long userId, Long addedCount, String fileName) {
        this.status = status;
        this.userId = userId;
        this.addedCount = addedCount;
        this.fileName = fileName;
    }
}
