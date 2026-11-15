package org.backend.lab3.minio;

import io.minio.CopyObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SourceObject;
import lombok.RequiredArgsConstructor;
import org.backend.lab3.exceptions.custom.MinioOperationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RequiredArgsConstructor
@Service
public class MinioService {
    private final MinioClient minioClient;
    @Value("${minio.bucket-name}")
    private String bucket;

    public void uploadFile(String objectName, MultipartFile file) {
        try {
            String contentType = file.getContentType();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1L)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new MinioOperationException("error uploading file");
        }
    }

    public InputStream getFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new MinioOperationException(String.format("error getting file: %s", objectName));
        }
    }

    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new MinioOperationException(String.format("error deleting file: %s", objectName));
        }
    }

    public void moveFile(String sourceObject, String targetObject) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucket)
                            .object(targetObject)
                            .source(
                                    SourceObject.builder()
                                            .bucket(bucket)
                                            .object(sourceObject)
                                            .build()
                            )
                            .build()
            );
            deleteFile(sourceObject);
        } catch (Exception e) {
            throw new MinioOperationException(
                    String.format("error moving file: %s -> %s", sourceObject, targetObject)
            );
        }
    }
}
