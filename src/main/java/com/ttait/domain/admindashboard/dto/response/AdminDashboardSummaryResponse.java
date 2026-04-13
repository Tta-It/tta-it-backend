package com.ttait.domain.admindashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 총관리자 대시보드 상단 KPI 카드에 표시할 요약 수치입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardSummaryResponse {

    private long totalUsageCount;
    private int topRegionCount;
    private int priorityReviewCount;
    private Integer pendingApplicationCount;
}
