package com.ttait.domain.organization.domain;

public enum AgreementStatus {
    PENDING,     // 검토 대기 (신청만 들어온 상태)
    ACTIVE,      // 승인 완료 (협약 체결)
    REJECTED,    // 반려 (관리자가 거부)
    TERMINATED,  // 해지 (협약 종료)
}
