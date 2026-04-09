package com.ttait.domain.application.service.impl;

import com.ttait.domain.application.domain.ApplicationFile;
import com.ttait.domain.application.dto.response.AdminApplicationDetailResponse;
import com.ttait.domain.application.dto.response.AdminApplicationListItem;
import com.ttait.domain.application.dto.response.AdminApplicationListResponse;
import com.ttait.domain.application.dto.response.ApplicationFileResponse;
import com.ttait.domain.application.dto.response.ApplicationReviewResponse;
import com.ttait.domain.application.mapper.ApplicationFileMapper;
import com.ttait.domain.application.service.AdminApplicationService;
import com.ttait.domain.notification.event.ApplicationApprovedEvent;
import com.ttait.domain.notification.event.ApplicationRejectedEvent;
import com.ttait.domain.organization.domain.AgreementStatus;
import com.ttait.domain.organization.domain.Organization;
import com.ttait.domain.organization.mapper.OrganizationMapper;
import com.ttait.domain.user.domain.User;
import com.ttait.domain.user.mapper.UserMapper;
import com.ttait.global.exception.BusinessException;
import com.ttait.global.exception.ErrorCode;
import com.ttait.global.file.FileStorageService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 협약 신청 관리 서비스
 * 목록/상세/파일 다운로드/승인/반려 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminApplicationServiceImpl implements AdminApplicationService {

    private static final int MAX_PAGE_SIZE = 100;

    private final OrganizationMapper organizationMapper;
    private final ApplicationFileMapper applicationFileMapper;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public AdminApplicationListResponse searchApplications(
            String keyword,
            AgreementStatus status,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    ) {
        // 페이지 파라미터 방어
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int offset = (safePage - 1) * safeSize;

        long totalCount = organizationMapper.countApplications(keyword, status, from, to);

        // 전체 건수가 0이면 굳이 조회 쿼리 실행 안 함
        if (totalCount == 0) {
            return new AdminApplicationListResponse(0, safePage, safeSize, List.of());
        }

        List<Organization> orgs = organizationMapper.searchApplications(keyword, status, from, to, offset, safeSize);
        List<AdminApplicationListItem> items = orgs.stream()
                .map(AdminApplicationListItem::from)
                .toList();

        return new AdminApplicationListResponse(totalCount, safePage, safeSize, items);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminApplicationDetailResponse getApplicationDetail(Long organizationId) {
        Organization organization = findSubmittedOrganizationOrThrow(organizationId);

        // 담당자 id 기준으로 업로드된 첨부 파일 목록 조회
        User contactUser = userMapper.findByOrganizationId(organizationId);
        List<ApplicationFileResponse> files = contactUser == null
                ? List.of()
                : applicationFileMapper.findByUserId(contactUser.getId()).stream()
                        .map(ApplicationFileResponse::from)
                        .toList();

        return AdminApplicationDetailResponse.of(organization, files);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadedFile downloadFile(Long organizationId, Long fileId) {
        ApplicationFile file = applicationFileMapper.findById(fileId);
        if (file == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        // 타 기업 파일 접근 차단: 파일을 업로드한 user의 organization_id가 요청path의 organizationId와 일치해야 함
        User uploader = userMapper.findById(file.getUserId());
        if (uploader == null || !organizationId.equals(uploader.getOrganizationId())) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        Resource resource = fileStorageService.loadAsResource(file.getFilePath());
        return new DownloadedFile(resource, file.getOriginalFileName(), file.getContentType());
    }

    @Override
    @Transactional
    public ApplicationReviewResponse approve(Long organizationId) {
        Organization organization = findSubmittedOrganizationOrThrow(organizationId);

        // 승인 가능 여부: PENDING 상태여야 함
        if (organization.getAgreementStatus() != AgreementStatus.PENDING) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_REVIEWABLE);
        }

        int updated = organizationMapper.approveApplication(organizationId);
        if (updated == 0) {
            // 동시성 상황 방어 (다른 트랜잭션이 상태를 바꿔버린 경우)
            throw new BusinessException(ErrorCode.APPLICATION_NOT_REVIEWABLE);
        }

        Organization refreshed = organizationMapper.findById(organizationId);
        log.info("[ADMIN-REVIEW] approved — orgId={}", organizationId);

        // 실시간 알림 이벤트 발행 (AFTER_COMMIT 리스너가 기업 관리자에게 WebSocket push)
        eventPublisher.publishEvent(new ApplicationApprovedEvent(
                refreshed.getId(),
                refreshed.getOrganizationName()
        ));

        return new ApplicationReviewResponse(
                refreshed.getId(),
                refreshed.getAgreementStatus(),
                refreshed.getApprovedAt(),
                null
        );
    }

    @Override
    @Transactional
    public ApplicationReviewResponse reject(Long organizationId, String reviewComment) {
        Organization organization = findSubmittedOrganizationOrThrow(organizationId);

        if (organization.getAgreementStatus() != AgreementStatus.PENDING) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_REVIEWABLE);
        }

        int updated = organizationMapper.rejectApplication(organizationId, reviewComment);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_REVIEWABLE);
        }

        Organization refreshed = organizationMapper.findById(organizationId);
        log.info("[ADMIN-REVIEW] rejected — orgId={}", organizationId);

        // 실시간 알림 이벤트 발행 (AFTER_COMMIT 리스너가 기업 관리자에게 WebSocket push, 반려 사유 포함)
        eventPublisher.publishEvent(new ApplicationRejectedEvent(
                refreshed.getId(),
                refreshed.getOrganizationName(),
                refreshed.getReviewComment()
        ));

        return new ApplicationReviewResponse(
                refreshed.getId(),
                refreshed.getAgreementStatus(),
                null,
                refreshed.getReviewComment()
        );
    }

    /**
     * organizationId 로 조회하되, 존재하지 않거나 DRAFT(아직 미제출) 상태면 404
     * 관리자 화면에서 다뤄지는 단위는 (제출된 신청서) 기준
     */
    private Organization findSubmittedOrganizationOrThrow(Long organizationId) {
        Organization organization = organizationMapper.findById(organizationId);
        if (organization == null || organization.getAgreementStatus() == AgreementStatus.DRAFT) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_SUBMITTED);
        }
        return organization;
    }
}
