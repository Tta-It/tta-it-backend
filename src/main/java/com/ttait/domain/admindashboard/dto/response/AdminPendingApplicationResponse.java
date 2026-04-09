package com.ttait.domain.admindashboard.dto.response;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

/**
 * 추후 협약 신청 현황 영역에 사용할 데이터.
 */
@Getter
@Builder
public class AdminPendingApplicationResponse {

    private final String organizationName;
    private final String managerName;
    private final LocalDate requestedDate;
    private final String status;
    private final String areaName;
}
