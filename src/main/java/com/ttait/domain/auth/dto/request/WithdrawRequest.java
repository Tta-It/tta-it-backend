package com.ttait.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

// 탈퇴 실수 방지를 위해 현재 비밀번호 재확인 요구
public record WithdrawRequest(
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}
