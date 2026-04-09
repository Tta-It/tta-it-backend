package com.ttait.domain.admindashboard.dto.response;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

/**
 * 날짜별 이용량 추이 차트용 데이터.
 */
@Getter
@Builder
public class AdminUsageTrendResponse {

    private final LocalDate statDate;
    private final long usageCount;
}
