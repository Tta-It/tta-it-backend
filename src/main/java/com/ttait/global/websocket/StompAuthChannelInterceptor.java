package com.ttait.global.websocket;

import com.ttait.global.security.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import com.ttait.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        // CONNECT 시점에만 JWT 검증 수행. 이후 메시지들은 이미 세션에 유저가 붙어있다
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader(AUTH_HEADER);
            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
                // 토큰 없거나 포맷이 다르면 CONNECT 거부
                throw new IllegalArgumentException("WebSocket 연결에 Authorization 헤더가 필요합니다.");
            }

            String token = authHeader.substring(BEARER_PREFIX.length());
            Claims claims = jwtTokenProvider.parseClaims(token); // 유효하지 않으면 BusinessException 발생

            // CustomUserDetailsService 로 활성 유저 조회 (상태 검증 포함)
            String loginId = claims.get("loginId", String.class);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginId);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

            // 세션에 Principal 주입, 이후 해당 유저에게 라우팅됨
            accessor.setUser(authentication);
            log.info("[WS] STOMP CONNECT authenticated — loginId={}", loginId);
        }

        return message;
    }
}
