package com.ttait.domain.employee.mapper;

import com.ttait.domain.employee.domain.Employee;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmployeeMapper {

    long countByOrganizationId(@Param("organizationId") Long organizationId);

    List<Employee> findByOrganizationId(@Param("organizationId") Long organizationId);

    List<Long> findNextIds(@Param("count") int count);

    int insertAll(@Param("employees") List<Employee> employees);
}
