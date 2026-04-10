package com.ttait.domain.employeeusage.mapper;

import com.ttait.domain.employeeusage.domain.EmployeeUsageStat;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmployeeUsageStatMapper {

    long countByOrganizationId(@Param("organizationId") Long organizationId);

    int insertAll(@Param("usages") List<EmployeeUsageStat> usages);
}
