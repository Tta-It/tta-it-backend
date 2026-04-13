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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


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
        List<Long> savedFileIds = storeFiles(userId, organization.getId(), request.getFiles());

        // 제출 후 organization 재조회해서 최신 submitted_at 포함 응답 생성
        Organization refreshed = organizationMapper.findById(organization.getId());

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

    @Override
    @Transactional
    public SubmitApplicationResponse resubmit(Long userId, Long organizationId, SubmitApplicationRequest request) {

        // 사용자의 기업 존재 여부 확인
        if (organizationId == null) {
            throw new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND);
        }
        Organization organization = organizationMapper.findById(organizationId);
        if (organization == null) {
            throw new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND);
        }

        // REJECTED 상태에서만 재신청 가능
        if (organization.getAgreementStatus() != AgreementStatus.REJECTED) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_REJECTED);
        }

        // 기존 첨부 파일 삭제 (디스크 -> DB 순서)
        List<ApplicationFile> oldFiles = applicationFileMapper.findByUserId(userId);
        for (ApplicationFile oldFile : oldFiles) {
            fileStorageService.delete(oldFile.getFilePath());
        }
        applicationFileMapper.deleteByUserId(userId);

        // 새 파일 저장
        List<Long> savedFileIds = storeFiles(userId, organization.getId(), request.getFiles());

        // 제출 정보 갱신 + 상태 REJECTED ->  PENDING 처리 및 submitted_at 갱신, review_comment/approved_at 리셋
        organization.setIndustryType(request.getIndustryType());
        organization.setEmployeeCount(request.getEmployeeCount());
        organization.setAddress(request.getAddress());
        int updated = organizationMapper.resubmitApplication(organization);
        if (updated == 0) {
            // 동시성 상황 방어 (다른 트랜잭션이 상태를 바꿔버린 경우)
            throw new BusinessException(ErrorCode.APPLICATION_NOT_REJECTED);
        }

        Organization refreshed = organizationMapper.findById(organization.getId());

        // 실시간 알림 이벤트 발행 — 재신청도 관리자 입장에선 새 검토 대상이므로 기존 이벤트 재사용
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

    /**
     * 업로드 파일 목록을 디스크에 저장하고 T_APPLICATION_FILE 에 메타데이터를 insert 한 뒤
     * 생성된 file id 목록을 반환한다. submit / resubmit 양쪽에서 공통 사용
     */
    private List<Long> storeFiles(Long userId, Long organizationId, List<MultipartFile> multipartFiles) {
        List<Long> savedFileIds = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            StoredFile stored = fileStorageService.store(multipartFile, organizationId);

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
        return savedFileIds;
    }
}
