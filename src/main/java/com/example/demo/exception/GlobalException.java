package com.example.demo.exception;

import com.example.demo.payload.Response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalException {
    @ExceptionHandler(ResourceNotFound.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFound ex){
            log.error("Resource exception: {}", ex.getMessage());
            ErrorResponse errorResponse=new ErrorResponse(
                400,
                ex.getMessage(),
                LocalDateTime.now()

            );
            return ResponseEntity.badRequest().body(errorResponse);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex){
        log.error("Illegal argument exception: {}", ex.getMessage());
        ErrorResponse errorResponse=new ErrorResponse(
                400,
                ex.getMessage(),
                LocalDateTime.now()

        );
        return ResponseEntity.badRequest().body(errorResponse);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex){
        log.error("Validation exception: {}", ex);
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ErrorResponse errorResponse= new ErrorResponse(
                400,
                message,
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException(MaxUploadSizeExceededException ex){
        ErrorResponse errorRespons = new ErrorResponse(
                400,
                "File size exceeds the maximum limit!",
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(errorRespons);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex){
        log.error("Exception: {}", ex);
        ErrorResponse errorResponse= new ErrorResponse(
                500,
                "Internal server error",
                LocalDateTime.now()
        );
        return ResponseEntity.status(500).body(errorResponse);
    }
}
