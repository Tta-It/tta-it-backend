package com.ttait.domain.user.mapper;

import com.ttait.domain.user.domain.RoleType;
import com.ttait.domain.user.domain.User;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User findById(@Param("id") Long id);

    User findByLoginId(@Param("loginId") String loginId);

    User findByEmail(@Param("email") String email);

    // 특정 기업의 담당자(기업 관리자) 조회
    User findByOrganizationId(@Param("organizationId") Long organizationId);

    // 특정 role 의 ACTIVE 유저 전체 조회
    List<User> findAllByRole(@Param("role") RoleType role);

    int insert(User user);

    int updateLastLoginAt(@Param("id") Long id);
}
