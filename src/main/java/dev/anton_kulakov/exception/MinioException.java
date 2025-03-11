package dev.anton_kulakov.exception;

public class MinioException extends RuntimeException {
    public MinioException(String message) {
        super(message);
    }
}
