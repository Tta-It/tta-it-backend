package com.ttait.domain.companyadmindashboard.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 기업 관리자 운영 대시보드 전체 응답 객체.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyAdminDashboardResponse {

    private CompanyAdminDashboardSummaryResponse summary;
    private List<CompanyAdminDashboardMonthlyUsageResponse> monthlyUsages;
    private List<CompanyAdminDashboardEmployeeUsageResponse> employeeUsages;
}
