package com.ttait.domain.companyadmindashboard.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * 기업 관리자 대시보드에서 조회할 기준 월을 받는 요청 객체입니다.
 * 보상 대상 계산은 프론트에서 처리하므로 백엔드는 월 기준 데이터만 조회합니다.
 */
@Getter
@Setter
public class CompanyAdminDashboardSearchRequest {

    private String targetMonth;
}
