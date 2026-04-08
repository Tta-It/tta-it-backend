package com.ttait.domain.user.controller;

import com.ttait.domain.user.dto.response.UserMeResponse;
import com.ttait.domain.user.service.UserService;
import com.ttait.global.response.ApiResponse;
import com.ttait.global.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserMeResponse> me(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ApiResponse.ok(userService.getMyInfo(principal.getId()));
    }
}
