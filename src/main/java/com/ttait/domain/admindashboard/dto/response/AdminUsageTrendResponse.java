package com.ttait.domain.admindashboard.dto.response;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUsageTrendResponse {

    private final LocalDate statDate;
    private final long usageCount;
}
