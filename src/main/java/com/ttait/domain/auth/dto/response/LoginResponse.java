package com.ttait.domain.auth.dto.response;

import com.ttait.domain.organization.domain.AgreementStatus;
import com.ttait.domain.user.domain.RoleType;

public record LoginResponse(
    String accessToken,
    Long userId,
    String loginId,
    RoleType role,
    // 기업 관리자: 본인 organization 의 협약 진행 상태
    // 관리자(ADMIN): 조직 없음 -> null
    AgreementStatus agreementStatus
) {
}
