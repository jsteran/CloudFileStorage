package dev.anton_kulakov.exception;

public class InvalidMoveOperationException extends RuntimeException {
    public InvalidMoveOperationException(String message) {
        super(message);
    }
}
