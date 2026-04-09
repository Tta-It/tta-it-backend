package com.ttait.domain.notification.event;

/**
 * 기업 관리자가 협약 신청서를 제출했을 때 발행되는 이벤트
 * 수신자: 모든 ADMIN
 */
public record ApplicationSubmittedEvent(
        Long organizationId,
        String organizationName,
        Long applicantUserId
) {
}
