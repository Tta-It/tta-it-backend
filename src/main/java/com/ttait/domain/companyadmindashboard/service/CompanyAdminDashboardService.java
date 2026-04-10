package com.ttait.domain.companyadmindashboard.service;

import com.ttait.domain.companyadmindashboard.dto.request.CompanyAdminDashboardSearchRequest;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardEmployeeDetailResponse;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardResponse;

/**
 * 기업 관리자 운영 대시보드 조회 기능을 제공한다.
 */
public interface CompanyAdminDashboardService {

    CompanyAdminDashboardResponse getDashboard(Long organizationId, CompanyAdminDashboardSearchRequest request);

    CompanyAdminDashboardEmployeeDetailResponse getEmployeeDetail(
            Long organizationId,
            Long employeeId,
            CompanyAdminDashboardSearchRequest request
    );
}
