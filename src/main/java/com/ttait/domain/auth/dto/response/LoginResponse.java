package com.ttait.domain.auth.dto.response;

import com.ttait.domain.user.domain.RoleType;

public record LoginResponse(
    String accessToken,
    Long userId,
    String loginId,
    RoleType role
) {
}