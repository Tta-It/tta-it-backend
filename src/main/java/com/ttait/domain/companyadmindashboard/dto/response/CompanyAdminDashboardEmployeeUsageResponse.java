package com.ttait.domain.companyadmindashboard.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 임직원 목록에서 보여줄 직원별 월간 이용 요약 응답입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyAdminDashboardEmployeeUsageResponse {

    private Long employeeId;
    private String employeeName;
    private String department;
    private Long usageCount;
    private BigDecimal travelDistance;
    private BigDecimal carbonReduction;
}
