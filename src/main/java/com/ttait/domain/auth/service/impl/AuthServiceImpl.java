package com.ttait.domain.auth.service.impl;

import com.ttait.domain.auth.dto.request.AdminSignUpRequest;
import com.ttait.domain.auth.dto.request.CompanyAdminSignUpRequest;
import com.ttait.domain.auth.dto.request.LoginRequest;
import com.ttait.domain.auth.dto.response.LoginResponse;
import com.ttait.domain.auth.service.AuthService;
import com.ttait.domain.organization.domain.AgreementStatus;
import com.ttait.domain.organization.domain.Organization;
import com.ttait.domain.organization.mapper.OrganizationMapper;
import com.ttait.domain.user.domain.RoleType;
import com.ttait.domain.user.domain.User;
import com.ttait.domain.user.domain.UserStatus;
import com.ttait.domain.user.mapper.UserMapper;
import com.ttait.global.exception.BusinessException;
import com.ttait.global.exception.ErrorCode;
import com.ttait.global.security.CustomUserPrincipal;
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
    private final OrganizationMapper organizationMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public Long signUpAdmin(AdminSignUpRequest request) {
        validateUserDuplicate(request.loginId(), request.email());

        User user = User.builder()
                .loginId(request.loginId())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .role(RoleType.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        userMapper.insert(user);
        return user.getId();
    }

    @Override
    @Transactional
    public Long signUpCompanyAdmin(CompanyAdminSignUpRequest request) {
        validateUserDuplicate(request.loginId(), request.email());

        String normalizedBusinessNumber = normalizeBusinessNumber(request.businessNumber());
        if (organizationMapper.findByBusinessNumber(normalizedBusinessNumber) != null) {
            throw new BusinessException(ErrorCode.DUPLICATE_BUSINESS_NUMBER);
        }

        // 회원가입 시점의 기업은 DRAFT(협약 신청 이전) 상태로 생성
        Organization organization = Organization.builder()
                .organizationName(request.organizationName())
                .businessNumber(normalizedBusinessNumber)
                .contactName(request.name())
                .contactEmail(request.email())
                .contactPhone(request.phone())
                .agreementStatus(AgreementStatus.DRAFT)
                .build();
        organizationMapper.insert(organization);

        // 기업 관리자 계정은 가입 직후 즉시 로그인 가능
        User user = User.builder()
                .organizationId(organization.getId())
                .loginId(request.loginId())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .role(RoleType.COMPANY_ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        userMapper.insert(user);

        return user.getId();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.loginId(), request.password()));
            CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();

            User user = userMapper.findById(principal.getId());
            String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getLoginId(), user.getRole());
            userMapper.updateLastLoginAt(user.getId());

            // 기업 관리자: 본인 기업의 협약 진행 상태를 함께 응답
            // 관리자: null로 응답
            AgreementStatus agreementStatus = null;
            if (user.getOrganizationId() != null) {
                Organization organization = organizationMapper.findById(user.getOrganizationId());
                if (organization != null) {
                    agreementStatus = organization.getAgreementStatus();
                }
            }

            return new LoginResponse(
                    accessToken,
                    user.getId(),
                    user.getLoginId(),
                    user.getRole(),
                    agreementStatus
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (AuthenticationException exception) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
    }

    private void validateUserDuplicate(String loginId, String email) {
        if (userMapper.findByLoginId(loginId) != null) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        if (userMapper.findByEmail(email) != null) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    private String normalizeBusinessNumber(String businessNumber) {
        return businessNumber == null ? null : businessNumber.replace("-", "");
    }
}
