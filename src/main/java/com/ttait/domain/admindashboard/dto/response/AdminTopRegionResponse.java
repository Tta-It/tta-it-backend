package com.ttait.domain.admindashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 수요 상위 지역 목록에 표시할 지역 순위 데이터입니다.
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
