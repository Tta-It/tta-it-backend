package com.ttait.domain.admindashboard.dto.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 최근 승인 대기 협약 신청 목록에 표시할 데이터입니다.
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
