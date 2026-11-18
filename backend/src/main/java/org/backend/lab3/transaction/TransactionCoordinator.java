package org.backend.lab3.transaction;

import lombok.RequiredArgsConstructor;
import org.backend.lab3.exceptions.custom.MinioOperationException;
import org.backend.lab3.imports.ImportService;
import org.backend.lab3.persons.dto.PersonResponse;
import org.backend.lab3.user.User;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class TransactionCoordinator {
    private final MinioParticipant minioParticipant;
    private final DbParticipant dbParticipant;
    private final ImportService importService;

    public List<PersonResponse> importPersons(MultipartFile file, User user) {
        int maxAttempts = 5;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            String fileKey = UUID.randomUUID() + "_" + file.getOriginalFilename();
            TransactionContext context = new TransactionContext(fileKey, file, user);

            boolean minioReady = minioParticipant.prepare(context);
            if (!minioReady) {
                minioParticipant.rollback(context);
                throw new MinioOperationException("MinIO preparation failed");
            }

            boolean dbReady = false;
            try {
                dbReady = dbParticipant.prepare(context);
            } catch (Exception e) {
                context.setError(e);
            }
            if (dbReady) {
                try {
                    minioParticipant.commit(context);
                    dbParticipant.commit(context);
                    return context.getResponses();
                } catch (Exception e) {
                    dbParticipant.rollback(context);
                    minioParticipant.rollback(context);
                    if (isSerializationError(e) && attempt < maxAttempts) {
                        retrySleep();
                        continue;
                    }
                    throw new RuntimeException("Commit failed", e);
                }
            } else {
                dbParticipant.rollback(context);
                Exception error = context.getError();

                if (isSerializationError(error) && attempt < maxAttempts) {
                    minioParticipant.rollback(context);
                    retrySleep();
                    continue;
                }

                if (error instanceof IllegalArgumentException) {
                    minioParticipant.commit(context);
                    importService.logFailedImport(user.getId(), 0L, context.getFileKey());
                    throw (IllegalArgumentException) error;
                } else {
                    minioParticipant.rollback(context);
                    throw new RuntimeException("Database failed during import", error);
                }
            }
        }
        throw new RuntimeException("Import failed");
    }

    private boolean isSerializationError(Throwable t) {
        while (t != null) {
            if (t.getMessage() != null && t.getMessage().contains("could not serialize access")) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private void retrySleep() {
        try {
            Thread.sleep((long) ((long) 100 + Math.random() * 50));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
