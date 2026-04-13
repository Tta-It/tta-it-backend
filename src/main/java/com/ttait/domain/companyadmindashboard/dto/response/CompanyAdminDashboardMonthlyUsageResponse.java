package com.ttait.domain.companyadmindashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 기업 관리자 대시보드의 월별 전체 이용 횟수 차트에 사용하는 응답 데이터입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyAdminDashboardMonthlyUsageResponse {

    private String targetMonth;
    private Long usageCount;
}
