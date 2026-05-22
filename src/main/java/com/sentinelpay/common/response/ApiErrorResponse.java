package com.sentinelpay.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

//@JsonInclude(JsonInclude.Include.NON_NULL) means if fieldErrors is null, it will not appear in the JSON.

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String errorCode,
        String message,
        String path,
        List<FieldErrorResponse> fieldErrors
) {
}