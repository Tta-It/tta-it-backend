package com.ttait.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
    MESSAGE_NOT_READABLE(HttpStatus.BAD_REQUEST, "COMMON_002", "요청 본문을 확인해주세요."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_001", "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_002", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "만료된 토큰입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_005", "아이디 또는 비밀번호가 올바르지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 로그인 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_003", "이미 사용 중인 이메일입니다."),
    INACTIVE_USER(HttpStatus.FORBIDDEN, "USER_004", "활성 상태의 사용자가 아닙니다."),
    WITHDRAW_NOT_ALLOWED(HttpStatus.FORBIDDEN, "USER_005", "해당 역할은 탈퇴할 수 없습니다."),
    WITHDRAW_PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "USER_006", "비밀번호가 일치하지 않습니다."),
    DUPLICATE_BUSINESS_NUMBER(HttpStatus.CONFLICT, "ORG_001", "이미 등록된 사업자등록번호입니다."),
    ORGANIZATION_NOT_FOUND(HttpStatus.NOT_FOUND, "ORG_002", "기업 정보를 찾을 수 없습니다."),
    APPLICATION_ALREADY_SUBMITTED(HttpStatus.CONFLICT, "APP_001", "이미 협약 신청이 제출되었습니다."),
    APPLICATION_NOT_SUBMITTED(HttpStatus.NOT_FOUND, "APP_002", "아직 제출된 협약 신청이 없습니다."),
    APPLICATION_NOT_REVIEWABLE(HttpStatus.BAD_REQUEST, "APP_003", "검토 대기 상태의 신청만 승인/반려할 수 있습니다."),
    APPLICATION_NOT_REJECTED(HttpStatus.CONFLICT, "APP_004", "반려 상태의 신청만 재제출할 수 있습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "FILE_001", "허용되지 않은 파일 형식입니다. (PDF, DOCX, HWP, HWPX 만 가능)"),
    INVALID_FILE_COUNT(HttpStatus.BAD_REQUEST, "FILE_002", "파일은 1개 이상 10개 이하로 첨부해주세요."),
    MAX_FILE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_003", "파일 크기가 허용 한도를 초과했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_004", "파일을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_999", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
