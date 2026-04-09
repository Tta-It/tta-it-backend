package com.ttait.domain.notification.publisher;

import com.ttait.domain.notification.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * WebSocket 푸시 전담 컴포넌트
 * SimpMessagingTemplate을 얇게 래핑해서 특정 유저(loginId 기반)에게 메시지 전송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private static final String USER_DESTINATION = "/queue/notifications";

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 특정 유저(loginId) 에게 실시간 알림 전송
     * StompAuthChannelInterceptor 에서 주입한 Principal.getName()과 매칭
     */
    public void sendToUser(String loginId, NotificationMessage message) {
        messagingTemplate.convertAndSendToUser(loginId, USER_DESTINATION, message);
        log.info("[WS-PUSH] loginId={}, type={}, targetId={}",
                loginId, message.type(), message.targetId());
    }
}
