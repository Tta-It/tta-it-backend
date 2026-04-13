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
 * 총관리자 대시보드 응답을 집계 데이터와 화면용 목록으로 조립하는 서비스 구현체입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final AdminDashboardMapper adminDashboardMapper;

    /**
     * 총관리자 대시보드 전체 응답을 조회합니다.
     */
    @Override
    public AdminDashboardResponse getDashboard(AdminDashboardSearchRequest request) {
        // 기간이 없으면 DB에 존재하는 최신 통계일 기준 최근 7일을 조회합니다.
        LocalDate latestUsageStatDate = resolveLatestUsageStatDate();
        LocalDate to = request.getTo() != null ? request.getTo() : latestUsageStatDate;
        LocalDate from = request.getFrom() != null ? request.getFrom() : to.minusDays(6);
        validateDateRange(from, to);

        if (latestUsageStatDate != null && from.isAfter(latestUsageStatDate)) {
            long periodDays = ChronoUnit.DAYS.between(from, to);
            to = latestUsageStatDate;
            from = latestUsageStatDate.minusDays(periodDays);
        }

        String district = normalizeDistrict(request.getDistrict());
        Integer pendingApplicationCount = adminDashboardMapper.countPendingApplications();
        List<AdminUsageAggregateProjection> usageAggregates = adminDashboardMapper.findUsageAggregates(from, to);
        List<AdminRegionUsageResponse> regionUsages = createRegionUsages(usageAggregates);
        List<AdminUsageTrendResponse> usageTrends = createUsageTrends(usageAggregates, from, to, district);
        List<AdminTopRegionResponse> topRegions = createTopRegions(regionUsages);
        List<AdminPriorityRegionProjection> priorityRegionCandidates =
                adminDashboardMapper.findPriorityRegionCandidates(from, to);
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

    /**
     * 조회 시작일이 종료일보다 늦지 않은지 확인합니다.
     */
    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 통계 데이터의 최신 일자를 조회하고, 데이터가 없으면 오늘 날짜를 사용합니다.
     */
    private LocalDate resolveLatestUsageStatDate() {
        LocalDate latestUsageStatDate = adminDashboardMapper.findLatestUsageStatDate();
        return latestUsageStatDate != null ? latestUsageStatDate : LocalDate.now();
    }

    private String normalizeDistrict(String district) {
        if (district == null || district.isBlank()) {
            return null;
        }

        return district.trim();
    }

    /**
     * 일자/지역 집계 결과에서 전체 이용량 KPI를 계산합니다.
     */
    private long calculateTotalUsageCount(List<AdminUsageAggregateProjection> usageAggregates) {
        return usageAggregates.stream()
                .mapToLong(AdminUsageAggregateProjection::getUsageCount)
                .sum();
    }

    /**
     * 배치 우선 검토 후보 전체 건수를 계산합니다.
     */
    private int calculatePriorityReviewCount(List<AdminPriorityRegionProjection> priorityRegionCandidates) {
        if (priorityRegionCandidates.isEmpty()) {
            return 0;
        }

        return priorityRegionCandidates.get(0).getTotalCount();
    }

    /**
     * 일자/지역 집계 결과를 지역별 이용량 목록으로 변환합니다.
     */
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

    /**
     * 일자/지역 집계 결과를 일자별 이용량 추이 목록으로 변환합니다.
     */
    private List<AdminUsageTrendResponse> createUsageTrends(
            List<AdminUsageAggregateProjection> usageAggregates,
            LocalDate from,
            LocalDate to,
            String district
    ) {
        if (district != null) {
            return adminDashboardMapper.findDistrictUsageTrends(from, to, district);
        }

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

    /**
     * 지역별 이용량 목록에서 상위 5개 지역을 추립니다.
     */
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

    /**
     * 배치 우선 검토 후보 조회 결과를 화면 응답 목록으로 변환합니다.
     */
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
