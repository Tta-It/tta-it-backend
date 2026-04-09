package com.ttait.domain.notification.listener;

import com.ttait.domain.notification.dto.NotificationMessage;
import com.ttait.domain.notification.event.ApplicationApprovedEvent;
import com.ttait.domain.notification.event.ApplicationRejectedEvent;
import com.ttait.domain.notification.event.ApplicationSubmittedEvent;
import com.ttait.domain.notification.publisher.NotificationPublisher;
import com.ttait.domain.user.domain.RoleType;
import com.ttait.domain.user.domain.User;
import com.ttait.domain.user.mapper.UserMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 협약 신청 관련 이벤트를 수신하여 WebSocket 으로 실시간 알림을 발송하는 리스너
 * 기존 서비스(트랜잭션) 이 성공적으로 커밋된 직후에만 알림 전송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private static final String TYPE_SUBMITTED = "PARTNERSHIP_APPLICATION_SUBMITTED";
    private static final String TYPE_APPROVED = "PARTNERSHIP_APPLICATION_APPROVED";
    private static final String TYPE_REJECTED = "PARTNERSHIP_APPLICATION_REJECTED";

    private final UserMapper userMapper;
    private final NotificationPublisher notificationPublisher;

    /**
     * 기업 관리자가 협약 신청서를 제출했을 때 → ADMIN 전원에게 푸시
     * 관리자 수는 소수로 가정하므로 순회하며 개별 유저 큐로 전송한다
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubmitted(ApplicationSubmittedEvent event) {
        List<User> admins = userMapper.findAllByRole(RoleType.ADMIN);
        if (admins.isEmpty()) {
            log.warn("[WS] no ADMIN user to notify — event={}", event);
            return;
        }

        NotificationMessage message = NotificationMessage.of(
                TYPE_SUBMITTED,
                "새 협약 신청",
                event.organizationName() + "(이)가 협약을 신청했습니다.",
                event.organizationId()
        );

        for (User admin : admins) {
            notificationPublisher.sendToUser(admin.getLoginId(), message);
        }
    }

    /**
     * 관리자가 협약 신청을 승인했을 때 → 해당 기업의 담당자에게 푸시
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApproved(ApplicationApprovedEvent event) {
        User companyAdmin = userMapper.findByOrganizationId(event.organizationId());
        if (companyAdmin == null) {
            log.warn("[WS] no company admin found for orgId={}", event.organizationId());
            return;
        }

        NotificationMessage message = NotificationMessage.of(
                TYPE_APPROVED,
                "협약 신청 승인",
                event.organizationName() + " 협약 신청이 승인되었습니다.",
                event.organizationId()
        );
        notificationPublisher.sendToUser(companyAdmin.getLoginId(), message);
    }

    /**
     * 관리자가 협약 신청을 반려했을 때 → 해당 기업의 담당자에게 푸시 (반려 사유 포함)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRejected(ApplicationRejectedEvent event) {
        User companyAdmin = userMapper.findByOrganizationId(event.organizationId());
        if (companyAdmin == null) {
            log.warn("[WS] no company admin found for orgId={}", event.organizationId());
            return;
        }

        NotificationMessage message = NotificationMessage.of(
                TYPE_REJECTED,
                "협약 신청 반려",
                "사유: " + event.reviewComment(),
                event.organizationId()
        );
        notificationPublisher.sendToUser(companyAdmin.getLoginId(), message);
    }
}
