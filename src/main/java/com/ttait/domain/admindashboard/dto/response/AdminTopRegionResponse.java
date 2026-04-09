package com.ttait.domain.admindashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminTopRegionResponse {

    private final int rank;
    private final String regionName;
    private final long usageCount;
    private final Double usageGrowthRate;
}
