package com.ttait.domain.companyadmindashboard.mapper;

import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardEmployeeDailyUsageResponse;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardEmployeeDetailResponse;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardEmployeeUsageResponse;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardMonthlyUsageResponse;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardSummaryResponse;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 기업 관리자 대시보드 조회용 집계 SQL 매퍼.
 */
@Mapper
public interface CompanyAdminDashboardMapper {

    CompanyAdminDashboardSummaryResponse findSummary(
            @Param("organizationId") Long organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<CompanyAdminDashboardEmployeeUsageResponse> findEmployeeUsages(
            @Param("organizationId") Long organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    CompanyAdminDashboardEmployeeDetailResponse findEmployeeDetail(
            @Param("organizationId") Long organizationId,
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<CompanyAdminDashboardEmployeeDailyUsageResponse> findEmployeeDailyUsages(
            @Param("organizationId") Long organizationId,
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    CompanyAdminDashboardEmployeeDailyUsageResponse findLatestEmployeeUsage(
            @Param("organizationId") Long organizationId,
            @Param("employeeId") Long employeeId
    );

    List<CompanyAdminDashboardMonthlyUsageResponse> findMonthlyUsages(
            @Param("organizationId") Long organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
