package com.ttait.domain.admindashboard.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 총 관리자 대시보드 전체 화면 데이터를 묶는 응답 객체.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {

    private AdminDashboardSummaryResponse summary;
    private List<AdminRegionUsageResponse> regionUsages;
    private List<AdminUsageTrendResponse> usageTrends;
    private List<AdminTopRegionResponse> topRegions;
    private List<AdminPriorityRegionResponse> priorityRegions;
    private List<AdminPendingApplicationResponse> pendingApplications;
}
