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
public class CompanyAdminDashboardSummaryResponse {

    private Long totalUsageCount;
    private BigDecimal totalTravelDistance;
    private BigDecimal totalCarbonReduction;
    private Integer rewardTargetEmployeeCount;
}
