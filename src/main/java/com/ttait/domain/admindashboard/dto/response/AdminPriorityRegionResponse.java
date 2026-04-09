package com.ttait.domain.admindashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 배치 우선 검토 지역 목록에 필요한 데이터.
 */
@Getter
@Builder
public class AdminPriorityRegionResponse {

    private final int rank;
    private final String stationName;
    private final Double usageGrowthRate;
    private final String reviewStatus;
}
