package com.ttait.domain.admindashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 수요 상위 지역 목록에 필요한 데이터.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminTopRegionResponse {

    private int rank;
    private String regionName;
    private long usageCount;
    private Double usageGrowthRate;
}
