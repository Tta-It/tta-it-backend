package com.ttait.domain.admindashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminPriorityRegionResponse {

    private final int rank;
    private final String stationName;
    private final Double usageGrowthRate;
    private final String reviewStatus;
}
