package com.ttait.domain.admindashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 상단 요약 카드 영역에 필요한 수치를 담는다.
 */
@Getter
@Builder
public class AdminDashboardSummaryResponse {

    private final long totalUsageCount;
    private final int topRegionCount;
    private final int priorityReviewCount;
    private final Integer pendingApplicationCount;
}
