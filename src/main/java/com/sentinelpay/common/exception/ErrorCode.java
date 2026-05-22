//Why enum ? Because we do not want random string error codes everywhere.

package com.sentinelpay.common.exception;

public enum ErrorCode {

    VALIDATION_FAILED,
    DUPLICATE_RESOURCE,
    RESOURCE_NOT_FOUND,
    BAD_REQUEST,
    UNAUTHORIZED,
    FORBIDDEN,
    INTERNAL_SERVER_ERROR
}