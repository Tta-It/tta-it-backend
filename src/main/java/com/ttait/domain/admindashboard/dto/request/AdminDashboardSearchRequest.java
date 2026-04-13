package com.ttait.domain.admindashboard.dto.request;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * 총관리자 대시보드 조회 기간 조건을 담는 요청 객체입니다.
 */
@Getter
@Setter
public class AdminDashboardSearchRequest {

    private LocalDate from;
    private LocalDate to;
    private String district;
}
