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
        String fileKey = UUID.randomUUID() + "_" + file.getOriginalFilename();
        TransactionContext context = new TransactionContext(fileKey, file, user);

        boolean minioReady = minioParticipant.prepare(context);
        if (!minioReady) {
            minioParticipant.rollback(context);
            throw new MinioOperationException("MinIO preparation failed");
        }

        boolean dbReady = dbParticipant.prepare(context);
        if (dbReady) {
            try {
                minioParticipant.commit(context);
                dbParticipant.commit(context);
                return context.getResponses();
            } catch (Exception e) {
                dbParticipant.rollback(context);
                minioParticipant.rollback(context);
                throw new RuntimeException("Commit failed", e);
            }
        } else {
            dbParticipant.rollback(context);
            Exception error = context.getError();

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
}
