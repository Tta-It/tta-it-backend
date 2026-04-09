package com.ttait.domain.admindashboard.service.impl;

import com.ttait.domain.admindashboard.dto.request.AdminDashboardSearchRequest;
import com.ttait.domain.admindashboard.dto.response.AdminDashboardResponse;
import com.ttait.domain.admindashboard.dto.response.AdminDashboardSummaryResponse;
import com.ttait.domain.admindashboard.service.AdminDashboardService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @Override
    public AdminDashboardResponse getDashboard(AdminDashboardSearchRequest request) {
        LocalDate from = request.getFrom() != null ? request.getFrom() : LocalDate.now().minusDays(6);
        LocalDate to = request.getTo() != null ? request.getTo() : LocalDate.now();

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
