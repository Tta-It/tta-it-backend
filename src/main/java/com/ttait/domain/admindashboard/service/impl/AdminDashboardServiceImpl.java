package com.ttait.domain.admindashboard.service.impl;

import com.ttait.domain.admindashboard.dto.request.AdminDashboardSearchRequest;
import com.ttait.domain.admindashboard.dto.response.AdminDashboardResponse;
import com.ttait.domain.admindashboard.dto.response.AdminDashboardSummaryResponse;
import com.ttait.domain.admindashboard.service.AdminDashboardService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 총 관리자 대시보드 응답을 조립하는 서비스 구현체.
 */
@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @Override
    public AdminDashboardResponse getDashboard(AdminDashboardSearchRequest request) {
        // 기간이 없으면 최근 7일 기준으로 조회한다.
        LocalDate from = request.getFrom() != null ? request.getFrom() : LocalDate.now().minusDays(6);
        LocalDate to = request.getTo() != null ? request.getTo() : LocalDate.now();

        // SQL 연결 전까지는 화면 구조 확인용 기본 응답을 내려준다.
        return AdminDashboardResponse.builder()
                .summary(AdminDashboardSummaryResponse.builder()
                        .totalUsageCount(0L)
                        .topRegionCount(0)
                        .priorityReviewCount(0)
                        .pendingApplicationCount(null)
                        .build())
                .regionUsages(List.of())
                .usageTrends(List.of())
                .topRegions(List.of())
                .priorityRegions(List.of())
                .pendingApplications(List.of())
                .build();
    }
}
