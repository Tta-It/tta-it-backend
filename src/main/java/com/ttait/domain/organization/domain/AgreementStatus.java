package com.ttait.domain.organization.domain;

public enum AgreementStatus {
    DRAFT,       // 초안 (회원가입만 완료, 아직 협약 신청서를 제출하지 않은 상태)
    PENDING,     // 검토 대기 (협약 신청서 제출 완료, 관리자 검토 대기 중)
    ACTIVE,      // 승인 완료 (관리자 승인, 협약 체결 상태)
    REJECTED,    // 반려 (관리자가 거부)
    TERMINATED,  // 해지 (협약 종료)
}
