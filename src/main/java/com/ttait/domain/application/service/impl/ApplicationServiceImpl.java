package com.ttait.domain.application.service.impl;

import com.ttait.domain.application.domain.ApplicationFile;
import com.ttait.domain.application.dto.request.SubmitApplicationRequest;
import com.ttait.domain.application.dto.response.ApplicationFileResponse;
import com.ttait.domain.application.dto.response.MyApplicationResponse;
import com.ttait.domain.application.dto.response.SubmitApplicationResponse;
import com.ttait.domain.application.mapper.ApplicationFileMapper;
import com.ttait.domain.application.service.ApplicationService;
import com.ttait.domain.notification.event.ApplicationSubmittedEvent;
import com.ttait.domain.organization.domain.AgreementStatus;
import com.ttait.domain.organization.domain.Organization;
import com.ttait.domain.organization.mapper.OrganizationMapper;
import com.ttait.global.exception.BusinessException;
import com.ttait.global.exception.ErrorCode;
import com.ttait.global.file.FileStorageService;
import com.ttait.global.file.StoredFile;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 기업 관리자의 협약 신청 도메인 서비스.
 * <p>
 * 본 구현에서 지키는 불변식:
 * <ul>
 *     <li>회원가입 시 organization row 가 이미 DRAFT 상태로 생성되어 있다.
 *     <li>기업 관리자 1명당 organization 1개가 1:1 로 연결되어 있다
 *         ({@code T_USER.organization_id}).
 *     <li>협약 신청은 한 번만 제출 가능하다 (중복 제출 불가).
 *         재신청 flow 는 후속 이슈로 분리.
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final OrganizationMapper organizationMapper;
    private final ApplicationFileMapper applicationFileMapper;
    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public SubmitApplicationResponse submit(Long userId, Long organizationId, SubmitApplicationRequest request) {

        // 사용자의 기업 존재 여부 확인
        if (organizationId == null) {
            throw new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND);
        }
        Organization organization = organizationMapper.findById(organizationId);
        if (organization == null) {
            throw new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND);
        }

        // AgreementStatus 기반 중복 제출 방지
        if (organization.getAgreementStatus() != AgreementStatus.DRAFT) {
            throw new BusinessException(ErrorCode.APPLICATION_ALREADY_SUBMITTED);
        }

        // 제출 정보 설정 후 AgreementStatus를 PENDING으로 변경
        organization.setIndustryType(request.getIndustryType());
        organization.setEmployeeCount(request.getEmployeeCount());
        organization.setAddress(request.getAddress());
        organization.setAgreementStatus(AgreementStatus.PENDING);
        organizationMapper.updateApplicationSubmission(organization);

        // 파일 디스크에 저장하고 T_APPLICATION_FILE 메타데이터 insert
        List<Long> savedFileIds = new ArrayList<>();
        for (MultipartFile multipartFile : request.getFiles()) {
            StoredFile stored = fileStorageService.store(multipartFile, organization.getId());

            ApplicationFile file = ApplicationFile.builder()
                    .userId(userId)
                    .originalFileName(multipartFile.getOriginalFilename())
                    .storedFileName(stored.storedFileName())
                    .filePath(stored.relativePath())
                    .fileSize(stored.fileSize())
                    .contentType(stored.contentType())
                    .build();
            applicationFileMapper.insert(file);
            savedFileIds.add(file.getId());
        }

        // 제출 후 organization 재조회해서 최신 submitted_at 포함 응답 생성
        Organization refreshed = organizationMapper.findById(organization.getId());

        log.info("[APPLICATION] submitted — userId={}, orgId={}, files={}",
                userId, organization.getId(), savedFileIds.size());

        // 실시간 알림 이벤트 발행 (AFTER_COMMIT 리스너가 WebSocket 으로 push)
        // 현재 트랜잭션이 커밋된 후에만 관리자에게 알림 전송
        eventPublisher.publishEvent(new ApplicationSubmittedEvent(
                refreshed.getId(),
                refreshed.getOrganizationName(),
                userId
        ));

        return new SubmitApplicationResponse(
                refreshed.getId(),
                refreshed.getAgreementStatus(),
                refreshed.getSubmittedAt(),
                savedFileIds
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MyApplicationResponse getMyApplication(Long userId, Long organizationId) {
        // organizationId는 로그인된 COMPANY_ADMIN 에게 항상 존재해야 함
        if (organizationId == null) {
            throw new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND);
        }
        Organization organization = organizationMapper.findById(organizationId);
        if (organization == null) {
            throw new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND);
        }

        // 업로드된 파일 목록 조회
        List<ApplicationFile> files = applicationFileMapper.findByUserId(userId);
        List<ApplicationFileResponse> fileResponses = files.stream()
                .map(ApplicationFileResponse::from)
                .toList();

        return MyApplicationResponse.of(organization, fileResponses);
    }
}
