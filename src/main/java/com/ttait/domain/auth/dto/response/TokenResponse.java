package com.ttait.domain.auth.dto.response;

import com.ttait.domain.user.domain.RoleType;

public record TokenResponse(
        String grantType,
        String accessToken,
        long expiresIn,
        Long userId,
        String loginId,
        RoleType role
) {
}
