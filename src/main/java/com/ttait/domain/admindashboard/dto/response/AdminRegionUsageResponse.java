package com.ttait.domain.admindashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 지역별 이용량 차트용 데이터.
 */
@Getter
@Builder
public class AdminRegionUsageResponse {

    private final String regionName;
    private final long usageCount;
}
