package com.ttait.domain.notification.dto;

import java.time.LocalDateTime;

/**
 * WebSocket 으로 발송되는 실시간 알림 페이로드
 * 저장하지 않고 즉시 전송만 함 (휘발성)
 */
public record NotificationMessage(
        String type,
        String title,
        String content,
        Long targetId,
        LocalDateTime sentAt
) {
    public static NotificationMessage of(String type, String title, String content, Long targetId) {
        return new NotificationMessage(type, title, content, targetId, LocalDateTime.now());
    }
}
