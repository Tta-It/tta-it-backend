package com.ttait.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminSignUpRequest(
        @NotBlank(message = "로그인 아이디는 필수입니다.")
        @Size(min = 4, max = 20, message = "로그인 아이디는 4자 이상 20자 이하로 입력해주세요.")
        String loginId,
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,20}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8자 이상 20자 이하로 입력해주세요."
        )
        String password,
        @NotBlank(message = "담당자명은 필수입니다.")
        String name,
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,
        String phone
) {
}
