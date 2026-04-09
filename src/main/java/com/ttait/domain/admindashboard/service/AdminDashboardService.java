package com.ttait.domain.admindashboard.service;

import com.ttait.domain.admindashboard.dto.request.AdminDashboardSearchRequest;
import com.ttait.domain.admindashboard.dto.response.AdminDashboardResponse;

/**
 * 총 관리자 대시보드 조회 기능을 제공하는 서비스 인터페이스.
 */
public interface AdminDashboardService {

    AdminDashboardResponse getDashboard(AdminDashboardSearchRequest request);
}
