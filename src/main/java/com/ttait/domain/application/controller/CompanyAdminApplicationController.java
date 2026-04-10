package com.ttait.domain.application.controller;

import com.ttait.domain.application.dto.request.SubmitApplicationRequest;
import com.ttait.domain.application.dto.response.MyApplicationResponse;
import com.ttait.domain.application.dto.response.SubmitApplicationResponse;
import com.ttait.domain.application.service.ApplicationService;
import com.ttait.global.response.ApiResponse;
import com.ttait.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 기업 관리자(COMPANY_ADMIN) 전용 협약 신청 API
 * COMPANY_ADMIN ROLE만 접근 가능
 */
@RestController
@RequestMapping("/api/v1/company-admin/applications")
@RequiredArgsConstructor
public class CompanyAdminApplicationController {

    private final ApplicationService applicationService;

    // 협약 신청서 제출
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SubmitApplicationResponse>> submit(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @ModelAttribute SubmitApplicationRequest request) {

        SubmitApplicationResponse response = applicationService.submit(
                principal.getId(),
                principal.getOrganizationId(),
                request
        );
        return ResponseEntity.status(201).body(ApiResponse.ok(response));
    }

    // 반려된 협약 신청 재제출
    @PostMapping(value = "/resubmit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<SubmitApplicationResponse> resubmit(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @ModelAttribute SubmitApplicationRequest request) {

        return ApiResponse.ok(
                applicationService.resubmit(
                        principal.getId(),
                        principal.getOrganizationId(),
                        request
                )
        );
    }

    // 본인 기업의 협약 신청 현황 조회
    @GetMapping("/me")
    public ApiResponse<MyApplicationResponse> getMyApplication(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ApiResponse.ok(
                applicationService.getMyApplication(
                        principal.getId(),
                        principal.getOrganizationId()
                )
        );
    }
}
