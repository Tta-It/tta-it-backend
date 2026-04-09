package com.ttait.domain.admindashboard.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

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
