package com.ttait.domain.admindashboard.service.impl;

import com.ttait.domain.admindashboard.dto.projection.AdminPriorityRegionProjection;
import com.ttait.domain.admindashboard.dto.projection.AdminUsageAggregateProjection;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 총 관리자 대시보드 응답을 조립하는 서비스 구현체.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final AdminDashboardMapper adminDashboardMapper;

    @Override
    public AdminDashboardResponse getDashboard(AdminDashboardSearchRequest request) {
        // 기간이 없으면 최근 7일 기준으로 조회한다.
        LocalDate latestUsageStatDate = resolveLatestUsageStatDate();
        LocalDate earliestUsageStatDate = adminDashboardMapper.findEarliestUsageStatDate();
        LocalDate to = request.getTo() != null ? request.getTo() : latestUsageStatDate;
        LocalDate from = request.getFrom() != null ? request.getFrom() : to.minusDays(6);
        validateDateRange(from, to);

        if (latestUsageStatDate != null && from.isAfter(latestUsageStatDate)) {
            long periodDays = ChronoUnit.DAYS.between(from, to);
            to = latestUsageStatDate;
            from = latestUsageStatDate.minusDays(periodDays);
        }

        long periodDays = ChronoUnit.DAYS.between(from, to) + 1;
        LocalDate previousTo = from.minusDays(1);
        LocalDate previousFrom = previousTo.minusDays(periodDays - 1);

        Integer pendingApplicationCount = adminDashboardMapper.countPendingApplications();
        List<AdminUsageAggregateProjection> usageAggregates = adminDashboardMapper.findUsageAggregates(from, to);
        List<AdminRegionUsageResponse> regionUsages = createRegionUsages(usageAggregates);
        List<AdminUsageTrendResponse> usageTrends = createUsageTrends(usageAggregates);
        List<AdminTopRegionResponse> topRegions = createTopRegions(regionUsages);
        List<AdminPriorityRegionProjection> priorityRegionCandidates =
                hasFullPreviousUsageStatPeriod(earliestUsageStatDate, previousFrom)
                        ? adminDashboardMapper.findPriorityRegionCandidates(from, to, previousFrom, previousTo)
                        : List.of();
        List<AdminPriorityRegionResponse> priorityRegions = createPriorityRegions(priorityRegionCandidates);
        List<AdminPendingApplicationResponse> pendingApplications = adminDashboardMapper.findPendingApplications();

        return AdminDashboardResponse.builder()
                .summary(AdminDashboardSummaryResponse.builder()
                        .totalUsageCount(calculateTotalUsageCount(usageAggregates))
                        .topRegionCount(topRegions.size())
                        .priorityReviewCount(calculatePriorityReviewCount(priorityRegionCandidates))
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

    private LocalDate resolveLatestUsageStatDate() {
        LocalDate latestUsageStatDate = adminDashboardMapper.findLatestUsageStatDate();
        return latestUsageStatDate != null ? latestUsageStatDate : LocalDate.now();
    }

    private boolean hasFullPreviousUsageStatPeriod(LocalDate earliestUsageStatDate, LocalDate previousFrom) {
        return earliestUsageStatDate != null && !previousFrom.isBefore(earliestUsageStatDate);
    }

    private long calculateTotalUsageCount(List<AdminUsageAggregateProjection> usageAggregates) {
        return usageAggregates.stream()
                .mapToLong(AdminUsageAggregateProjection::getUsageCount)
                .sum();
    }

    private int calculatePriorityReviewCount(List<AdminPriorityRegionProjection> priorityRegionCandidates) {
        if (priorityRegionCandidates.isEmpty()) {
            return 0;
        }

        return priorityRegionCandidates.get(0).getTotalCount();
    }

    private List<AdminRegionUsageResponse> createRegionUsages(
            List<AdminUsageAggregateProjection> usageAggregates
    ) {
        Map<String, Long> usageCountByRegion = new HashMap<>();
        for (AdminUsageAggregateProjection usageAggregate : usageAggregates) {
            usageCountByRegion.merge(usageAggregate.getRegionName(), usageAggregate.getUsageCount(), Long::sum);
        }

        return usageCountByRegion.entrySet().stream()
                .map(entry -> AdminRegionUsageResponse.builder()
                        .regionName(entry.getKey())
                        .usageCount(entry.getValue())
                        .build())
                .sorted(Comparator.comparingLong(AdminRegionUsageResponse::getUsageCount).reversed()
                        .thenComparing(AdminRegionUsageResponse::getRegionName))
                .toList();
    }

    private List<AdminUsageTrendResponse> createUsageTrends(
            List<AdminUsageAggregateProjection> usageAggregates
    ) {
        Map<LocalDate, Long> usageCountByDate = new TreeMap<>();
        for (AdminUsageAggregateProjection usageAggregate : usageAggregates) {
            usageCountByDate.merge(usageAggregate.getStatDate(), usageAggregate.getUsageCount(), Long::sum);
        }

        return usageCountByDate.entrySet().stream()
                .map(entry -> AdminUsageTrendResponse.builder()
                        .statDate(entry.getKey())
                        .usageCount(entry.getValue())
                        .build())
                .toList();
    }

    private List<AdminTopRegionResponse> createTopRegions(List<AdminRegionUsageResponse> regionUsages) {
        int size = Math.min(5, regionUsages.size());
        List<AdminTopRegionResponse> topRegions = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            AdminRegionUsageResponse regionUsage = regionUsages.get(i);
            topRegions.add(AdminTopRegionResponse.builder()
                    .rank(i + 1)
                    .regionName(regionUsage.getRegionName())
                    .usageCount(regionUsage.getUsageCount())
                    .usageGrowthRate(null)
                    .build());
        }

        return topRegions;
    }

    private List<AdminPriorityRegionResponse> createPriorityRegions(
            List<AdminPriorityRegionProjection> priorityRegionCandidates
    ) {
        return priorityRegionCandidates.stream()
                .map(priorityRegion -> AdminPriorityRegionResponse.builder()
                        .rank(priorityRegion.getRank())
                        .stationName(priorityRegion.getStationName())
                        .usageGrowthRate(priorityRegion.getUsageGrowthRate())
                        .reviewStatus(priorityRegion.getReviewStatus())
                        .build())
                .toList();
    }
}
