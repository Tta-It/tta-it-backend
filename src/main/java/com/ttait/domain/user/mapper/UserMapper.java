package com.ttait.domain.user.mapper;

import com.ttait.domain.user.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User findById(@Param("id") Long id);

    User findByLoginId(@Param("loginId") String loginId);

    User findByEmail(@Param("email") String email);

    int insert(User user);

    int updateLastLoginAt(@Param("id") Long id);
}
