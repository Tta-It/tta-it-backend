package com.ttait.domain.admindashboard.dto.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 날짜별 이용량 추이 차트용 데이터.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUsageTrendResponse {

    private LocalDate statDate;
    private long usageCount;
}
