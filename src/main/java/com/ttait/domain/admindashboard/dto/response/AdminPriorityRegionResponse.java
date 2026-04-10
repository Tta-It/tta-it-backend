package com.ttait.domain.admindashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 배치 우선 검토 지역 목록에 필요한 데이터.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPriorityRegionResponse {

    private int rank;
    private String stationName;
    private Double usageGrowthRate;
    private String reviewStatus;
}
