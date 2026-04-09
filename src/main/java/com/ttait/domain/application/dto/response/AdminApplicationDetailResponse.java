package com.ttait.domain.application.dto.response;

import com.ttait.domain.organization.domain.AgreementStatus;
import com.ttait.domain.organization.domain.Organization;
import java.time.LocalDateTime;
import java.util.List;

// 관리자 협약 신청 상세 응답
public record AdminApplicationDetailResponse(
        Long organizationId,
        String organizationName,
        String businessNumber,
        String industryType,
        Integer employeeCount,
        String address,
        String contactName,
        String contactEmail,
        String contactPhone,
        AgreementStatus agreementStatus,
        LocalDateTime submittedAt,
        LocalDateTime approvedAt,
        String reviewComment,
        List<ApplicationFileResponse> files
) {
    public static AdminApplicationDetailResponse of(Organization org, List<ApplicationFileResponse> files) {
        return new AdminApplicationDetailResponse(
                org.getId(),
                org.getOrganizationName(),
                org.getBusinessNumber(),
                org.getIndustryType(),
                org.getEmployeeCount(),
                org.getAddress(),
                org.getContactName(),
                org.getContactEmail(),
                org.getContactPhone(),
                org.getAgreementStatus(),
                org.getSubmittedAt(),
                org.getApprovedAt(),
                org.getReviewComment(),
                files
        );
    }
}
