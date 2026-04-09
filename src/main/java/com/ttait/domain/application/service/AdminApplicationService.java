package com.ttait.domain.application.service;

import com.ttait.domain.application.dto.response.AdminApplicationDetailResponse;
import com.ttait.domain.application.dto.response.AdminApplicationListResponse;
import com.ttait.domain.application.dto.response.ApplicationReviewResponse;
import com.ttait.domain.organization.domain.AgreementStatus;
import java.time.LocalDateTime;
import org.springframework.core.io.Resource;

// 관리자 전용 협약 신청 관리 서비스
public interface AdminApplicationService {

    // 검색/필터/페이징된 신청 목록 조회 (DRAFT 제외, 제출일 DESC)
    AdminApplicationListResponse searchApplications(
            String keyword,
            AgreementStatus status,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    );

    // 특정 기업의 협약 신청 상세 조회
    AdminApplicationDetailResponse getApplicationDetail(Long organizationId);

    // 첨부 파일 다운로드 리소스 + 원본 파일명 반환
    DownloadedFile downloadFile(Long organizationId, Long fileId);

    // 협약 신청 승인 (PENDING -> ACTIVE)
    ApplicationReviewResponse approve(Long organizationId);

    // 협약 신청 반려 (PENDING -> REJECTED) + 사유 저장
    ApplicationReviewResponse reject(Long organizationId, String reviewComment);

    // 파일 다운로드 시 controller 에 전달할 값 묶음
    record DownloadedFile(Resource resource, String originalFileName, String contentType) {}
}
