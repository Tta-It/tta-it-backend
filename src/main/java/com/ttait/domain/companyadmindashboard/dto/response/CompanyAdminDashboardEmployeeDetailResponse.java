package com.ttait.domain.companyadmindashboard.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 직원 상세 화면에서 사용하는 월간 이용 요약, 최근 이용내역, 일자별 이용내역 응답입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyAdminDashboardEmployeeDetailResponse {

    private Long employeeId;
    private String employeeName;
    private String department;
    private String position;
    private Long usageCount;
    private BigDecimal travelDistance;
    private BigDecimal carbonReduction;
    private CompanyAdminDashboardEmployeeDailyUsageResponse latestUsage;
    private List<CompanyAdminDashboardEmployeeDailyUsageResponse> dailyUsages;
}
