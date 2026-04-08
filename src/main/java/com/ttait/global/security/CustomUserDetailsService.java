package com.ttait.global.security;

import com.ttait.domain.user.domain.User;
import com.ttait.domain.user.domain.UserStatus;
import com.ttait.domain.user.mapper.UserMapper;
import com.ttait.global.exception.BusinessException;
import com.ttait.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userMapper.findByLoginId(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INACTIVE_USER);
        }
        return new CustomUserPrincipal(user);
    }
}
