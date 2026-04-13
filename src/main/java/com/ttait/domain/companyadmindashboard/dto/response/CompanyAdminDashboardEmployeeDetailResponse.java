package com.ttait.domain.companyadmindashboard.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
