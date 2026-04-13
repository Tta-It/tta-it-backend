package com.ttait.domain.admindashboard.dto.projection;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 대여소 이용 통계를 일자와 지역 단위로 집계한 내부 조회 결과입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUsageAggregateProjection {

    private LocalDate statDate;
    private String regionName;
    private long usageCount;
}
