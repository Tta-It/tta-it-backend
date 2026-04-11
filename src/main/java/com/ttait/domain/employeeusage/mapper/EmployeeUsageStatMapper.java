package com.ttait.domain.employeeusage.mapper;

import com.ttait.domain.employeeusage.domain.EmployeeUsageStat;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 임직원 이용 통계 조회 및 생성에 사용하는 매퍼.
 */
@Mapper
public interface EmployeeUsageStatMapper {

    long countByOrganizationId(@Param("organizationId") Long organizationId);

    List<Long> findEmployeeIdsByOrganizationId(@Param("organizationId") Long organizationId);

    List<Long> findNextIds(@Param("count") int count);

    int insertAll(@Param("usages") List<EmployeeUsageStat> usages);
}
