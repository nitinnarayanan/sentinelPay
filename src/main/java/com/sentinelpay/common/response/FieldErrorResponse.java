package com.sentinelpay.common.response;

public record FieldErrorResponse(
        String field,
        String message,
        Object rejectedValue
) {
}