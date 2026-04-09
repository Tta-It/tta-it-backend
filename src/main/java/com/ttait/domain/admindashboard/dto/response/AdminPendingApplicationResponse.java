package com.ttait.domain.admindashboard.dto.response;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminPendingApplicationResponse {

    private final String organizationName;
    private final String managerName;
    private final LocalDate requestedDate;
    private final String status;
    private final String areaName;
}
