package com.ttait.domain.admindashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 수요 상위 지역 목록에 필요한 데이터.
 */
@Getter
@Builder
public class AdminTopRegionResponse {

    private final int rank;
    private final String regionName;
    private final long usageCount;
    private final Double usageGrowthRate;
}
