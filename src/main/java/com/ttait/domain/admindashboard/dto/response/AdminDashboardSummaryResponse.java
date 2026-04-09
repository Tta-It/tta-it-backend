package com.ttait.domain.admindashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 상단 요약 카드 영역에 필요한 수치를 담는다.
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
