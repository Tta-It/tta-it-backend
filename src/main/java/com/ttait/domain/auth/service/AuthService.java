package com.ttait.domain.auth.service;

import com.ttait.domain.auth.dto.request.LoginRequest;
import com.ttait.domain.auth.dto.request.SignUpRequest;
import com.ttait.domain.auth.dto.response.TokenResponse;

public interface AuthService {

    Long signUpAdmin(SignUpRequest request);

    Long signUpCompanyAdmin(SignUpRequest request);

    TokenResponse login(LoginRequest request);
}
