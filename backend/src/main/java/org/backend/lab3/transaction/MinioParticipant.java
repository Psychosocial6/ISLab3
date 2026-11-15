package org.backend.lab3.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backend.lab3.minio.MinioService;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MinioParticipant implements TransactionParticipant {
    private final MinioService minioService;

    @Override
    public boolean prepare(TransactionContext context) {
        try {
            minioService.uploadFile("temp/" + context.getFileKey(), context.getFile());
            return true;
        } catch (Exception e) {
            log.error("MinIO prepare failed");
            return false;
        }
    }

    @Override
    public void commit(TransactionContext context) {
        minioService.moveFile("temp/" + context.getFileKey(), context.getFileKey());
        log.info("minio transaction committed");
    }

    @Override
    public void rollback(TransactionContext context) {
        minioService.deleteFile("temp/" + context.getFileKey());
        log.info("minio transaction rollback");
    }
}
