package com.ttait.domain.auth.controller;

import com.ttait.domain.auth.dto.request.AdminSignUpRequest;
import com.ttait.domain.auth.dto.request.CompanyAdminSignUpRequest;
import com.ttait.domain.auth.dto.request.LoginRequest;
import com.ttait.domain.auth.dto.request.WithdrawRequest;
import com.ttait.domain.auth.dto.response.LoginResponse;
import com.ttait.domain.auth.service.AuthService;
import com.ttait.global.response.ApiResponse;
import com.ttait.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup/admin")
    public ResponseEntity<ApiResponse<Map<String, Long>>> signUpAdmin(
            @Valid @RequestBody AdminSignUpRequest request) {
        Long userId = authService.signUpAdmin(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + userId))
                .body(ApiResponse.ok(Map.of("userId", userId)));
    }

    @PostMapping("/signup/company-admin")
    public ResponseEntity<ApiResponse<Map<String, Long>>> signUpCompanyAdmin(
            @Valid @RequestBody CompanyAdminSignUpRequest request) {
        Long userId = authService.signUpCompanyAdmin(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + userId))
                .body(ApiResponse.ok(Map.of("userId", userId)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal CustomUserPrincipal principal) {
        authService.logout(principal.getId());
        return ApiResponse.ok(null);
    }

    @PostMapping("/withdraw")
    public ApiResponse<Void> withdraw(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody WithdrawRequest request) {
        authService.withdraw(principal.getId(), request);
        return ApiResponse.ok(null);
    }
}
