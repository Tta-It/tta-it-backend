package com.ttait.domain.admindashboard.dto.projection;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUsageAggregateProjection {

    private LocalDate statDate;
    private String regionName;
    private long usageCount;
}
