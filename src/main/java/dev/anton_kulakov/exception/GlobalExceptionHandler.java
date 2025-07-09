package dev.anton_kulakov.exception;

import dev.anton_kulakov.dto.ErrorMessage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.ConcurrentModificationException;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleResourceNotFoundException(ResourceNotFoundException e) {
        return new ErrorMessage(e.getMessage());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage handleResourceAlreadyExistsException(ResourceAlreadyExistsException e) {
        return new ErrorMessage(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleValidationException(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        String message = "There is a validation error. " + String.join(", ", errors);
        return new ErrorMessage(message);
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleMissingServletRequestParameterException(Exception e) {
        return new ErrorMessage("One or more required parameters are missing " + e);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        return new ErrorMessage(e.getMessage());
    }

    @ExceptionHandler(InvalidMoveOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleInvalidMoveOperationException(InvalidMoveOperationException e) {
        return new ErrorMessage(e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleConstraintViolationException(ConstraintViolationException e) {
        List<String> errors = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        String message = "There is a validation error. " + String.join(", ", errors);
        return new ErrorMessage(message);
    }

    @ExceptionHandler(UsernameAlreadyTakenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage handleUsernameAlreadyTakenException(UsernameAlreadyTakenException e) {
        return new ErrorMessage(e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        long maxUploadSize = e.getMaxUploadSize();
        return new ErrorMessage("File or folder size exceeds limit " + maxUploadSize);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleDefaultException(Exception e) {
        return new ErrorMessage("We're sorry, but an unexpected error has occurred. Please try again later " + e);
    }

    @ExceptionHandler(ConcurrentModificationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage handleConcurrentModificationException(ConcurrentModificationException e) {
        return new ErrorMessage("Resource file or folder was modified during the move operation. Please try again " + e);
    }
}
