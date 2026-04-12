package com.ttait.domain.admindashboard.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
