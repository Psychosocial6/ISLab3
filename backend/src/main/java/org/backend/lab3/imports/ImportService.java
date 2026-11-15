package org.backend.lab3.imports;

import lombok.RequiredArgsConstructor;
import org.backend.lab3.cache.TrackCache;
import org.backend.lab3.imports.dto.ImportLog;
import org.backend.lab3.security.Role;
import org.backend.lab3.user.User;
import org.backend.lab3.utils.ImportLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ImportService {
    private final ImportRepository importRepository;
    private final ImportLogMapper importLogMapper;

    @TrackCache
    public List<ImportLog> getImportHistory(User user) {
        if (user.getRole() == Role.ROLE_USER) {
            return importRepository.findByUserId(user.getId())
                    .stream()
                    .map(importLogMapper::fromEntity)
                    .toList();
        }
        return importRepository.findAll()
                .stream()
                .map(importLogMapper::fromEntity)
                .toList();

    }

    @Transactional(propagation = Propagation.MANDATORY)
    public ImportLog logSuccessImport(Long userId, Long addedCount, String fileName) {
        ImportLogEntity importLogEntity = new ImportLogEntity(ImportStatus.SUCCESS, userId, addedCount, fileName);
        return importLogMapper.fromEntity(importRepository.save(importLogEntity));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportLog logFailedImport(Long userId, Long addedCount, String fileName) {
        ImportLogEntity importLogEntity = new ImportLogEntity(ImportStatus.FAILED, userId, addedCount, fileName);
        return importLogMapper.fromEntity(importRepository.save(importLogEntity));
    }
}
