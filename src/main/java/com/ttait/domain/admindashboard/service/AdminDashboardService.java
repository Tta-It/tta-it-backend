package com.ttait.domain.admindashboard.service;

import com.ttait.domain.admindashboard.dto.request.AdminDashboardSearchRequest;
import com.ttait.domain.admindashboard.dto.response.AdminDashboardResponse;

/**
 * 총관리자 대시보드 조회 기능을 제공하는 서비스 인터페이스입니다.
 */
public interface AdminDashboardService {

    /**
     * 조회 조건에 맞는 총관리자 대시보드 데이터를 구성합니다.
     */
    AdminDashboardResponse getDashboard(AdminDashboardSearchRequest request);
}
