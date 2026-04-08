package com.ttait.domain.user.domain;

import com.ttait.global.common.BaseEntity;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

    private Long id;
    private Long organizationId;
    private String loginId;
    private String password;
    private String name;
    private String email;
    private String phone;
    private RoleType role;
    private UserStatus status;
    private LocalDateTime lastLoginAt;

    @Builder
    public User(Long id, Long organizationId, String loginId, String password, String name, String email,
                String phone, RoleType role, UserStatus status, LocalDateTime lastLoginAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.lastLoginAt = lastLoginAt;
    }
}
