/* This stage adds
centralized error handling
consistent API response
proper HTTP status codes
validation field-level errors
safe generic 500 response
server-side logging for unexpected errors
sensitive value masking

Production API rule:
Tell the client enough to fix the request.
Do not expose internals.
Log internals on the server side.

* */


package com.sentinelpay.common.exception;

import com.sentinelpay.common.response.ApiErrorResponse;
import com.sentinelpay.common.response.FieldErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.CONFLICT;

        return ResponseEntity.status(status).body(
                buildErrorResponse(
                        status,
                        ErrorCode.DUPLICATE_RESOURCE.name(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        null
                )
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        return ResponseEntity.status(status).body(
                buildErrorResponse(
                        status,
                        ErrorCode.RESOURCE_NOT_FOUND.name(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        null
                )
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(
            BadRequestException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(
                buildErrorResponse(
                        status,
                        ErrorCode.BAD_REQUEST.name(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        null
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        List<FieldErrorResponse> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorResponse(
                        error.getField(),
                        error.getDefaultMessage(),
                        maskSensitiveValue(error.getField(), error.getRejectedValue())
                ))
                .toList();

        return ResponseEntity.status(status).body(
                buildErrorResponse(
                        status,
                        ErrorCode.VALIDATION_FAILED.name(),
                        "Request validation failed",
                        request.getRequestURI(),
                        fieldErrors
                )
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                ErrorCode.FORBIDDEN.name(),
                "You do not have permission to access this resource",
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(
                buildErrorResponse(
                        status,
                        ErrorCode.VALIDATION_FAILED.name(),
                        "Method validation failed",
                        request.getRequestURI(),
                        null
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(
                buildErrorResponse(
                        status,
                        ErrorCode.BAD_REQUEST.name(),
                        "Malformed JSON request",
                        request.getRequestURI(),
                        null
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error occurred at path: {}", request.getRequestURI(), ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(status).body(
                buildErrorResponse(
                        status,
                        ErrorCode.INTERNAL_SERVER_ERROR.name(),
                        "Something went wrong. Please contact support.",
                        request.getRequestURI(),
                        null
                )
        );
    }

    private ApiErrorResponse buildErrorResponse(
            HttpStatus status,
            String errorCode,
            String message,
            String path,
            List<FieldErrorResponse> fieldErrors
    ) {
        return new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                errorCode,
                message,
                path,
                fieldErrors
        );
    }

    private Object maskSensitiveValue(String fieldName, Object rejectedValue) {
        if (fieldName == null) {
            return rejectedValue;
        }

        String normalizedFieldName = fieldName.toLowerCase();

        if (normalizedFieldName.contains("password")
                || normalizedFieldName.contains("token")
                || normalizedFieldName.contains("secret")) {
            return "***";
        }

        return rejectedValue;
    }


}