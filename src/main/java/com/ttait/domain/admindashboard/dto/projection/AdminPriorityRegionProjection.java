package com.ttait.domain.admindashboard.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 배치 우선 검토 대상 대여소를 계산할 때 사용하는 내부 조회 결과입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminPriorityRegionProjection {

    private int rank;
    private String stationName;
    private Double usageGrowthRate;
    private String reviewStatus;
    private int totalCount;
}
