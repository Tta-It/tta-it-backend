package com.ttait.domain.companyadmindashboard.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * 기업 관리자 대시보드 조회 조건을 담는 요청 객체.
 */
@Getter
@Setter
public class CompanyAdminDashboardSearchRequest {

    private String targetMonth;
}
