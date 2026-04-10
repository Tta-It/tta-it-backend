package com.ttait.domain.companyadmindashboard.service;

import com.ttait.domain.companyadmindashboard.dto.request.CompanyAdminDashboardSearchRequest;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardEmployeeDetailResponse;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardResponse;

public interface CompanyAdminDashboardService {

    CompanyAdminDashboardResponse getDashboard(Long organizationId, CompanyAdminDashboardSearchRequest request);

    CompanyAdminDashboardEmployeeDetailResponse getEmployeeDetail(
            Long organizationId,
            Long employeeId,
            CompanyAdminDashboardSearchRequest request
    );
}
