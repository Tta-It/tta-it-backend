package com.ttait.domain.notification.event;

/**
 * 관리자가 협약 신청을 승인했을 때 발행되는 이벤트
 * 수신자: 해당 organization 의 COMPANY_ADMIN
 */
public record ApplicationApprovedEvent(
        Long organizationId,
        String organizationName
) {
}
