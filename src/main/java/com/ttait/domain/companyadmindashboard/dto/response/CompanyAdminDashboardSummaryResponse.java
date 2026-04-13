package com.ttait.domain.companyadmindashboard.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 선택 월 기준 전체 이용 횟수, 이동 거리, 탄소 절감량 KPI를 담는 응답입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyAdminDashboardSummaryResponse {

    private Long totalUsageCount;
    private BigDecimal totalTravelDistance;
    private BigDecimal totalCarbonReduction;
}
