package com.ttait.domain.user.service.impl;

import com.ttait.domain.user.domain.User;
import com.ttait.domain.user.dto.response.UserMeResponse;
import com.ttait.domain.user.mapper.UserMapper;
import com.ttait.domain.user.service.UserService;
import com.ttait.global.exception.BusinessException;
import com.ttait.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public UserMeResponse getMyInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        return new UserMeResponse(
                user.getId(),
                user.getOrganizationId(),
                user.getLoginId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
}
