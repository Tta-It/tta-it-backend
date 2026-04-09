package com.ttait.domain.application.controller;

import com.ttait.domain.application.dto.request.RejectApplicationRequest;
import com.ttait.domain.application.dto.response.AdminApplicationDetailResponse;
import com.ttait.domain.application.dto.response.AdminApplicationListResponse;
import com.ttait.domain.application.dto.response.ApplicationReviewResponse;
import com.ttait.domain.application.service.AdminApplicationService;
import com.ttait.domain.application.service.AdminApplicationService.DownloadedFile;
import com.ttait.domain.organization.domain.AgreementStatus;
import com.ttait.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자(ADMIN) 전용 협약 신청 관리 API
 */
@RestController
@RequestMapping("/api/v1/admin/applications")
@RequiredArgsConstructor
public class AdminApplicationController {

    private final AdminApplicationService adminApplicationService;

    // 협약 신청 목록 조회 (검색 / 상태 필터 / 기간 필터 / 페이징)
    // 제출일(submitted_at) 내림차순 정렬, DRAFT 제외
    @GetMapping
    public ApiResponse<AdminApplicationListResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AgreementStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // 날짜 파라미터는 day 단위로 들어옴, 쿼리는 타임스탬프 경계로 변환
        // from 은 해당일 00:00:00 부터 포함, to 는 (to 다음 날 00:00:00) 미만으로 처리
        LocalDateTime fromDt = from == null ? null : from.atStartOfDay();
        LocalDateTime toDt = to == null ? null : to.plusDays(1).atStartOfDay();

        return ApiResponse.ok(
                adminApplicationService.searchApplications(keyword, status, fromDt, toDt, page, size)
        );
    }

    // 협약 신청 상세 조회
    @GetMapping("/{organizationId}")
    public ApiResponse<AdminApplicationDetailResponse> detail(@PathVariable Long organizationId) {
        return ApiResponse.ok(adminApplicationService.getApplicationDetail(organizationId));
    }

    // 첨부 파일 다운로드, RFC 5987 (UTF-8 인코딩)으로 한글 파일명 지원
    @GetMapping("/{organizationId}/files/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long organizationId,
            @PathVariable Long fileId
    ) {
        DownloadedFile file = adminApplicationService.downloadFile(organizationId, fileId);

        String encodedName = URLEncoder.encode(file.originalFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        String contentDisposition = "attachment; filename*=UTF-8''" + encodedName;

        MediaType mediaType = file.contentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(file.contentType());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(file.resource());
    }

    // 승인 (PENDING -> ACTIVE)
    @PostMapping("/{organizationId}/approve")
    public ApiResponse<ApplicationReviewResponse> approve(@PathVariable Long organizationId) {
        return ApiResponse.ok(adminApplicationService.approve(organizationId));
    }

    // 반려 (PENDING -> REJECTED, 사유 저장)
    @PostMapping("/{organizationId}/reject")
    public ApiResponse<ApplicationReviewResponse> reject(
            @PathVariable Long organizationId,
            @Valid @RequestBody RejectApplicationRequest request
    ) {
        return ApiResponse.ok(adminApplicationService.reject(organizationId, request.reviewComment()));
    }
}
