package com.ttait.domain.organization.domain;

import com.ttait.global.common.BaseEntity;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Organization extends BaseEntity {

    private Long id;
    private String organizationName;
    private String businessNumber;
    private String industryType;
    private Integer employeeCount;
    private String address;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private AgreementStatus agreementStatus;
    private LocalDateTime submittedAt;  // 협약 신청서 제출 시각 (이후 PENDIND 처리)
    private LocalDateTime approvedAt;

    @Builder
    public Organization(Long id, String organizationName, String businessNumber, String industryType,
                        Integer employeeCount, String address, String contactName, String contactEmail,
                        String contactPhone, AgreementStatus agreementStatus,
                        LocalDateTime submittedAt, LocalDateTime approvedAt) {
        this.id = id;
        this.organizationName = organizationName;
        this.businessNumber = businessNumber;
        this.industryType = industryType;
        this.employeeCount = employeeCount;
        this.address = address;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.agreementStatus = agreementStatus;
        this.submittedAt = submittedAt;
        this.approvedAt = approvedAt;
    }
}
