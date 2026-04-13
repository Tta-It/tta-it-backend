package com.ttait.domain.companyadmindashboard.listener;

import com.ttait.domain.companyadmindashboard.service.CompanyAdminDashboardSeedService;
import com.ttait.domain.notification.event.ApplicationApprovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 협약 승인 이벤트를 받아 기업 관리자 대시보드용 시드 생성을 시작하는 리스너입니다.
 * 승인 트랜잭션이 커밋된 뒤 별도 스레드에서 실행해 승인 API 응답 지연을 줄입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgreementSeedEventListener {

    private final CompanyAdminDashboardSeedService companyAdminDashboardSeedService;

    /**
     * 협약 승인 완료 후 해당 기업의 임직원과 이용내역 시드 데이터를 백그라운드에서 생성합니다.
     */
    @Async("companyDashboardSeedExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void seedAfterAgreementApproved(ApplicationApprovedEvent event) {
        try {
            companyAdminDashboardSeedService.seedIfNeeded(event.organizationId());
        } catch (RuntimeException ex) {
            log.error("Failed to seed company dashboard data. organizationId={}", event.organizationId(), ex);
        }
    }
}
