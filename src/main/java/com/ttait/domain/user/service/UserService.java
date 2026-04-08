package com.ttait.domain.user.service;

import com.ttait.domain.user.dto.response.UserMeResponse;

public interface UserService {

    UserMeResponse getMyInfo(Long userId);
}
