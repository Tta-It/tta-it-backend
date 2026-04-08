package com.ttait.domain.user.dto.response;

import com.ttait.domain.user.domain.RoleType;
import com.ttait.domain.user.domain.UserStatus;
import java.time.LocalDateTime;

public record UserMeResponse(
        Long id,
        Long organizationId,
        String loginId,
        String name,
        String email,
        String phone,
        RoleType role,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {
}
