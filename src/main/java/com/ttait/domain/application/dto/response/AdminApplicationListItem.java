package com.ttait.domain.application.dto.response;

import com.ttait.domain.organization.domain.AgreementStatus;
import com.ttait.domain.organization.domain.Organization;
import java.time.LocalDateTime;

// 관리자 협약 신청 목록의 행 단위 DTO
public record AdminApplicationListItem(
        Long organizationId,
        String organizationName,
        String contactName,
        LocalDateTime submittedAt,
        AgreementStatus agreementStatus
) {
    public static AdminApplicationListItem from(Organization org) {
        return new AdminApplicationListItem(
                org.getId(),
                org.getOrganizationName(),
                org.getContactName(),
                org.getSubmittedAt(),
                org.getAgreementStatus()
        );
    }
}
