package com.ttait.domain.companyadmindashboard.controller;

import com.ttait.domain.companyadmindashboard.dto.request.CompanyAdminDashboardSearchRequest;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardEmployeeDetailResponse;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardResponse;
import com.ttait.domain.companyadmindashboard.service.CompanyAdminDashboardService;
import com.ttait.global.response.ApiResponse;
import com.ttait.global.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 기업 관리자 운영 대시보드 조회 API.
 */
@RestController
@RequestMapping("/api/v1/company-admin/dashboard")
@RequiredArgsConstructor
public class CompanyAdminDashboardController {

    private final CompanyAdminDashboardService companyAdminDashboardService;

    @GetMapping
    public ApiResponse<CompanyAdminDashboardResponse> getDashboard(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @ModelAttribute CompanyAdminDashboardSearchRequest request
    ) {
        return ApiResponse.ok(
                companyAdminDashboardService.getDashboard(principal.getOrganizationId(), request)
        );
    }

    @GetMapping("/employees/{employeeId}")
    public ApiResponse<CompanyAdminDashboardEmployeeDetailResponse> getEmployeeDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long employeeId,
            @ModelAttribute CompanyAdminDashboardSearchRequest request
    ) {
        return ApiResponse.ok(
                companyAdminDashboardService.getEmployeeDetail(principal.getOrganizationId(), employeeId, request)
        );
    }
}
