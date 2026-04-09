package com.ttait.domain.admindashboard.service.impl;

import com.ttait.domain.admindashboard.dto.request.AdminDashboardSearchRequest;
import com.ttait.domain.admindashboard.dto.response.AdminDashboardResponse;
import com.ttait.domain.admindashboard.dto.response.AdminDashboardSummaryResponse;
import com.ttait.domain.admindashboard.dto.response.AdminPendingApplicationResponse;
import com.ttait.domain.admindashboard.dto.response.AdminPriorityRegionResponse;
import com.ttait.domain.admindashboard.dto.response.AdminRegionUsageResponse;
import com.ttait.domain.admindashboard.dto.response.AdminTopRegionResponse;
import com.ttait.domain.admindashboard.dto.response.AdminUsageTrendResponse;
import com.ttait.domain.admindashboard.mapper.AdminDashboardMapper;
import com.ttait.global.exception.BusinessException;
import com.ttait.global.exception.ErrorCode;
import com.ttait.domain.admindashboard.service.AdminDashboardService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 총 관리자 대시보드 응답을 조립하는 서비스 구현체.
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final AdminDashboardMapper adminDashboardMapper;

    @Override
    public AdminDashboardResponse getDashboard(AdminDashboardSearchRequest request) {
        // 기간이 없으면 최근 7일 기준으로 조회한다.
        LocalDate from = request.getFrom() != null ? request.getFrom() : LocalDate.now().minusDays(6);
        LocalDate to = request.getTo() != null ? request.getTo() : LocalDate.now();
        validateDateRange(from, to);

        long periodDays = ChronoUnit.DAYS.between(from, to) + 1;
        LocalDate previousTo = from.minusDays(1);
        LocalDate previousFrom = previousTo.minusDays(periodDays - 1);

        Long totalUsageCount = adminDashboardMapper.findTotalUsageCount(from, to);
        Integer pendingApplicationCount = adminDashboardMapper.countPendingApplications();
        Integer priorityReviewCount = adminDashboardMapper.countPriorityRegions(from, to, previousFrom, previousTo);
        List<AdminRegionUsageResponse> regionUsages = adminDashboardMapper.findRegionUsages(from, to);
        List<AdminUsageTrendResponse> usageTrends = adminDashboardMapper.findUsageTrends(from, to);
        List<AdminTopRegionResponse> topRegions = adminDashboardMapper.findTopRegions(from, to);
        List<AdminPriorityRegionResponse> priorityRegions =
                adminDashboardMapper.findPriorityRegions(from, to, previousFrom, previousTo);
        List<AdminPendingApplicationResponse> pendingApplications = adminDashboardMapper.findPendingApplications();

        return AdminDashboardResponse.builder()
                .summary(AdminDashboardSummaryResponse.builder()
                        .totalUsageCount(totalUsageCount != null ? totalUsageCount : 0L)
                        .topRegionCount(topRegions.size())
                        .priorityReviewCount(priorityReviewCount != null ? priorityReviewCount : 0)
                        .pendingApplicationCount(pendingApplicationCount != null ? pendingApplicationCount : 0)
                        .build())
                .regionUsages(regionUsages)
                .usageTrends(usageTrends)
                .topRegions(topRegions)
                .priorityRegions(priorityRegions)
                .pendingApplications(pendingApplications)
                .build();
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
