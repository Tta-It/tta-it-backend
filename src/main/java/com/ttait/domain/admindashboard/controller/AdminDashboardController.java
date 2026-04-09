package com.ttait.domain.admindashboard.controller;

import com.ttait.domain.admindashboard.dto.request.AdminDashboardSearchRequest;
import com.ttait.domain.admindashboard.dto.response.AdminDashboardResponse;
import com.ttait.domain.admindashboard.service.AdminDashboardService;
import com.ttait.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 총 관리자 대시보드 조회 요청을 받는 컨트롤러.
 */
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ApiResponse<AdminDashboardResponse> getDashboard(@ModelAttribute AdminDashboardSearchRequest request) {
        return ApiResponse.ok(adminDashboardService.getDashboard(request));
    }
}
