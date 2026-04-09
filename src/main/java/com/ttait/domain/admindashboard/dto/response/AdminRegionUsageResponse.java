package com.ttait.domain.admindashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminRegionUsageResponse {

    private final String regionName;
    private final long usageCount;
}
