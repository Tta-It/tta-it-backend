package com.ttait.domain.companyadmindashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyAdminDashboardRewardCriteriaResponse {

    private String targetMonth;
    private Integer rewardTargetPercent;
    private Integer minimumMonthlyUsageCount;
    private boolean rewardOnly;
}
