package com.ttait.domain.application.dto.response;

import com.ttait.domain.organization.domain.AgreementStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 협약 신청 제출 성공 응답
 */
public record SubmitApplicationResponse(
        Long organizationId,
        AgreementStatus agreementStatus,
        LocalDateTime submittedAt,
        List<Long> fileIds
) {
}
