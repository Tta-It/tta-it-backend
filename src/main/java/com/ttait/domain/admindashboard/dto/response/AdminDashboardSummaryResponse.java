package com.ttait.domain.admindashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardSummaryResponse {

    private final long totalUsageCount;
    private final int topRegionCount;
    private final int priorityReviewCount;
    private final Integer pendingApplicationCount;
}
