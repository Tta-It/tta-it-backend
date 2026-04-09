package com.ttait.domain.admindashboard.service;

import com.ttait.domain.admindashboard.dto.request.AdminDashboardSearchRequest;
import com.ttait.domain.admindashboard.dto.response.AdminDashboardResponse;

public interface AdminDashboardService {

    AdminDashboardResponse getDashboard(AdminDashboardSearchRequest request);
}
