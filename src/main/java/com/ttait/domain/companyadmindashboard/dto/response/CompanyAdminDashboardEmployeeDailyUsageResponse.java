package com.ttait.domain.companyadmindashboard.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 직원 상세 화면의 일자별 이용내역과 최근 이용내역에 공통으로 사용하는 응답입니다.
 */
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
