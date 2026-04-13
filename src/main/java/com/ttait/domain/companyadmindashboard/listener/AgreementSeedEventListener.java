package com.ttait.domain.companyadmindashboard.listener;

import com.ttait.domain.companyadmindashboard.service.CompanyAdminDashboardSeedService;
import com.ttait.domain.notification.event.ApplicationApprovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgreementSeedEventListener {

    private final CompanyAdminDashboardSeedService companyAdminDashboardSeedService;

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
