package com.example.quote.api;

import com.example.quote.api.dto.ErrorResponse;
import com.example.quote.api.dto.FieldErrorDto;
import com.example.quote.api.dto.ValidationErrorResponse;
import com.example.quote.service.exception.IdempotencyConflictException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TRACE_ID_KEY = "traceId";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String traceId = resolveTraceId();

        List<FieldErrorDto> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(this::toFieldErrorDto)
                .collect(Collectors.toList());

        ValidationErrorResponse body = ValidationErrorResponse.builder()
                .traceId(traceId)
                .errorCode("VALIDATION_ERROR")
                .message("Request validation failed")
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex
    ) {
        String traceId = resolveTraceId();

        List<FieldErrorDto> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(violation -> FieldErrorDto.builder()
                        .field(violation.getPropertyPath().toString())
                        .message(violation.getMessage())
                        .rejectedValue(violation.getInvalidValue())
                        .code(violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName())
                        .build())
                .collect(Collectors.toList());

        ValidationErrorResponse body = ValidationErrorResponse.builder()
                .traceId(traceId)
                .errorCode("VALIDATION_ERROR")
                .message("Request validation failed")
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyConflict(IdempotencyConflictException ex) {
        String traceId = resolveTraceId();

        ErrorResponse body = ErrorResponse.builder()
                .traceId(traceId)
                .errorCode("IDEMPOTENCY_CONFLICT")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        String traceId = resolveTraceId();

        ErrorResponse body = ErrorResponse.builder()
                .traceId(traceId)
                .errorCode("UNAUTHENTICATED")
                .message("Invalid or missing credentials")
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        String traceId = resolveTraceId();

        ErrorResponse body = ErrorResponse.builder()
                .traceId(traceId)
                .errorCode("FORBIDDEN")
                .message("Insufficient permissions to perform this operation")
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        String traceId = resolveTraceId();

        ErrorResponse body = ErrorResponse.builder()
                .traceId(traceId)
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private FieldErrorDto toFieldErrorDto(FieldError fieldError) {
        return FieldErrorDto.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .code(fieldError.getCode())
                .build();
    }

    private String resolveTraceId() {
        String fromMdc = MDC.get(TRACE_ID_KEY);
        return (fromMdc != null && !fromMdc.isBlank())
                ? fromMdc
                : UUID.randomUUID().toString();
    }
}