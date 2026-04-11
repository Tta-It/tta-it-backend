package com.ttait.domain.employeeusage.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 임직원별 일자 단위 이용 통계를 담는 도메인 객체.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeUsageStat {

    private Long id;
    private Long organizationId;
    private Long employeeId;
    private LocalDate usageDate;
    private Integer usageCount;
    private BigDecimal travelDistance;
    private BigDecimal carbonAmount;
    private BigDecimal usageDurationMinutes;
    private LocalDateTime createdAt;
}
