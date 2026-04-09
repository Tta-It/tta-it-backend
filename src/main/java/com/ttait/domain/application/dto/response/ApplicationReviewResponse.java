package com.ttait.domain.application.dto.response;

import com.ttait.domain.organization.domain.AgreementStatus;
import java.time.LocalDateTime;

// 승인/반려 공통 응답 (승인 시 approvedAt, 반려 시 reviewComment 필드가 채워짐)
public record ApplicationReviewResponse(
        Long organizationId,
        AgreementStatus agreementStatus,
        LocalDateTime approvedAt,
        String reviewComment
) {
}
