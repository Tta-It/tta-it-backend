package com.ttait.global.exception;

import com.ttait.global.common.MessageUtils;
import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001"),
    MESSAGE_NOT_READABLE(HttpStatus.BAD_REQUEST, "COMMON_002"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_001"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_002"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004"),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_005"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001"),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "USER_002"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_003"),
    INACTIVE_USER(HttpStatus.FORBIDDEN, "USER_004"),
    WITHDRAW_NOT_ALLOWED(HttpStatus.FORBIDDEN, "USER_005"),
    WITHDRAW_PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "USER_006"),
    DUPLICATE_BUSINESS_NUMBER(HttpStatus.CONFLICT, "ORG_001"),
    ORGANIZATION_NOT_FOUND(HttpStatus.NOT_FOUND, "ORG_002"),
    APPLICATION_ALREADY_SUBMITTED(HttpStatus.CONFLICT, "APP_001"),
    APPLICATION_NOT_SUBMITTED(HttpStatus.NOT_FOUND, "APP_002"),
    APPLICATION_NOT_REVIEWABLE(HttpStatus.BAD_REQUEST, "APP_003"),
    APPLICATION_NOT_REJECTED(HttpStatus.CONFLICT, "APP_004"),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "FILE_001"),
    INVALID_FILE_COUNT(HttpStatus.BAD_REQUEST, "FILE_002"),
    MAX_FILE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_003"),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_004"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_999");

    private final HttpStatus status;
    private final String code;

    ErrorCode(HttpStatus status, String code) {
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public String message() {
        return MessageUtils.getMessage(code);
    }
}
