package com.ttait.global.logging;

import com.ttait.global.security.CustomUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class ControllerLoggingAspect {

    @Around("within(com.ttait.domain..controller..*)")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        String method = request.getMethod();
        String uri = request.getRequestURI();
        Long userId = extractUserId();

        log.info("[REQUEST] {} {} | userId={}", method, uri, userId);

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[RESPONSE] {} {} | userId={} | {}ms", method, uri, userId, elapsed);
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("[RESPONSE] {} {} | userId={} | {}ms | error={}", method, uri, userId, elapsed, e.getMessage());
            throw e;
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    private Long extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal principal) {
            return principal.getId();
        }
        return null;
    }
}
