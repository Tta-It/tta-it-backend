package com.ttait.domain.application.dto.response;

import com.ttait.domain.organization.domain.AgreementStatus;
import com.ttait.domain.organization.domain.Organization;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 기업 관리자의 내 협약 신청 현황 응답 DTO
 */
public record MyApplicationResponse(
        Long organizationId,
        String organizationName,
        String businessNumber,
        String industryType,
        Integer employeeCount,
        String address,
        AgreementStatus agreementStatus,
        LocalDateTime submittedAt,
        LocalDateTime approvedAt,
        List<ApplicationFileResponse> files
) {
    public static MyApplicationResponse of(Organization org, List<ApplicationFileResponse> files) {
        return new MyApplicationResponse(
                org.getId(),
                org.getOrganizationName(),
                org.getBusinessNumber(),
                org.getIndustryType(),
                org.getEmployeeCount(),
                org.getAddress(),
                org.getAgreementStatus(),
                org.getSubmittedAt(),
                org.getApprovedAt(),
                files
        );
    }
}
