package com.ttait.domain.notification.event;

/**
 * 관리자가 협약 신청을 반려했을 때 발행되는 이벤트
 * 수신자: 해당 organization 의 COMPANY_ADMIN
 * 반려 사유(reviewComment) 를 함께 전달하여 토스트에 노출
 */
public record ApplicationRejectedEvent(
        Long organizationId,
        String organizationName,
        String reviewComment
) {
}
