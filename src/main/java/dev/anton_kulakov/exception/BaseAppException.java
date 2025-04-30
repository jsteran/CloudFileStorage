package dev.anton_kulakov.exception;

public class BaseAppException extends RuntimeException {
    public BaseAppException(String message) {
        super(message);
    }
}
