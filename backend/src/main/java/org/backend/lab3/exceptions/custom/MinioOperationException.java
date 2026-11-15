package org.backend.lab3.exceptions.custom;

public class MinioOperationException extends RuntimeException {
    public MinioOperationException(String message) {
        super(message);
    }
}
