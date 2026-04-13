package com.ttait.domain.companyadmindashboard.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 기업 관리자 대시보드 첫 화면에 필요한 요약, 월별 추이, 임직원 이용 목록을 묶어 내려주는 응답입니다.
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
