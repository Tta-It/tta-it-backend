package com.ttait.domain.auth.service;

import com.ttait.domain.auth.dto.request.AdminSignUpRequest;
import com.ttait.domain.auth.dto.request.CompanyAdminSignUpRequest;
import com.ttait.domain.auth.dto.request.LoginRequest;
import com.ttait.domain.auth.dto.response.LoginResponse;

public interface AuthService {

    Long signUpAdmin(AdminSignUpRequest request);

    Long signUpCompanyAdmin(CompanyAdminSignUpRequest request);

    LoginResponse login(LoginRequest request);
}
