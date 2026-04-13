package com.ttait.domain.companyadmindashboard.dto.response;

import java.math.BigDecimal;
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
public class CompanyAdminDashboardEmployeeUsageResponse {

    private Long employeeId;
    private String employeeName;
    private String department;
    private Long usageCount;
    private BigDecimal travelDistance;
    private BigDecimal carbonReduction;
}
