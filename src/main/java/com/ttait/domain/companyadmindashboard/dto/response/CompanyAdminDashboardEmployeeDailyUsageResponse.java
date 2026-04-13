package com.ttait.domain.companyadmindashboard.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
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
public class CompanyAdminDashboardEmployeeDailyUsageResponse {

    private LocalDate usageDate;
    private Long usageCount;
    private BigDecimal travelDistance;
    private BigDecimal carbonAmount;
    private BigDecimal usageDurationMinutes;
}
