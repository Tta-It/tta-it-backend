package com.ttait.domain.admindashboard.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 총 관리자 대시보드 전체 화면 데이터를 묶는 응답 객체.
 */
@Getter
@Builder
public class AdminDashboardResponse {

    private final AdminDashboardSummaryResponse summary;
    private final List<AdminRegionUsageResponse> regionUsages;
    private final List<AdminUsageTrendResponse> usageTrends;
    private final List<AdminTopRegionResponse> topRegions;
    private final List<AdminPriorityRegionResponse> priorityRegions;
    private final List<AdminPendingApplicationResponse> pendingApplications;
}
