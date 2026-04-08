package com.ttait.domain.auth.service.impl;

import com.ttait.domain.auth.dto.request.LoginRequest;
import com.ttait.domain.auth.dto.request.SignUpRequest;
import com.ttait.domain.auth.dto.response.TokenResponse;
import com.ttait.domain.auth.service.AuthService;
import com.ttait.domain.user.domain.RoleType;
import com.ttait.domain.user.domain.User;
import com.ttait.domain.user.domain.UserStatus;
import com.ttait.domain.user.mapper.UserMapper;
import com.ttait.global.exception.BusinessException;
import com.ttait.global.exception.ErrorCode;
import com.ttait.global.security.CustomUserPrincipal;
import com.ttait.global.security.JwtProperties;
import com.ttait.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public Long signUpAdmin(SignUpRequest request) {
        return signUp(request, RoleType.ADMIN, UserStatus.ACTIVE);
    }

    @Override
    @Transactional
    public Long signUpCompanyAdmin(SignUpRequest request) {
        return signUp(request, RoleType.COMPANY_ADMIN, UserStatus.PENDING);
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.loginId(), request.password()));
            CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();

            User user = userMapper.findById(principal.getId());
            String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getLoginId(), user.getRole());
            userMapper.updateLastLoginAt(user.getId());

            return new TokenResponse(
                    "Bearer",
                    accessToken,
                    jwtProperties.accessTokenValiditySeconds(),
                    user.getId(),
                    user.getLoginId(),
                    user.getRole()
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (AuthenticationException exception) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
    }

    private Long signUp(SignUpRequest request, RoleType role, UserStatus status) {
        validateDuplicate(request);

        User user = User.builder()
                .loginId(request.loginId())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .role(role)
                .status(status)
                .build();

        userMapper.insert(user);
        return user.getId();
    }

    private void validateDuplicate(SignUpRequest request) {
        if (userMapper.findByLoginId(request.loginId()) != null) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        if (userMapper.findByEmail(request.email()) != null) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }
}
