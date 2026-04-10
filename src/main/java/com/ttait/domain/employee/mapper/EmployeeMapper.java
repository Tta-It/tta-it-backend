package com.ttait.domain.employee.mapper;

import com.ttait.domain.employee.domain.Employee;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 임직원 기본 정보 조회 및 생성에 사용하는 매퍼.
 */
@Mapper
public interface EmployeeMapper {

    long countByOrganizationId(@Param("organizationId") Long organizationId);

    List<Employee> findByOrganizationId(@Param("organizationId") Long organizationId);

    List<Long> findNextIds(@Param("count") int count);

    int insertAll(@Param("employees") List<Employee> employees);
}
