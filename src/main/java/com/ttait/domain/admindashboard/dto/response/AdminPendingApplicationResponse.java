package com.ttait.domain.admindashboard.dto.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 추후 협약 신청 현황 영역에 사용할 데이터.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPendingApplicationResponse {

    private String organizationName;
    private String managerName;
    private LocalDate requestedDate;
    private String status;
    private String areaName;
}
