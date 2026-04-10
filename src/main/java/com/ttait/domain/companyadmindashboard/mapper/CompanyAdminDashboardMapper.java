package com.ttait.domain.companyadmindashboard.mapper;

import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardEmployeeDailyUsageResponse;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardEmployeeDetailResponse;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardEmployeeUsageResponse;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardSummaryResponse;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CompanyAdminDashboardMapper {

    CompanyAdminDashboardSummaryResponse findSummary(
            @Param("organizationId") Long organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("rewardTargetPercent") int rewardTargetPercent,
            @Param("minimumMonthlyUsageCount") int minimumMonthlyUsageCount
    );

    List<CompanyAdminDashboardEmployeeUsageResponse> findEmployeeUsages(
            @Param("organizationId") Long organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("rewardTargetPercent") int rewardTargetPercent,
            @Param("minimumMonthlyUsageCount") int minimumMonthlyUsageCount,
            @Param("rewardOnly") boolean rewardOnly
    );

    CompanyAdminDashboardEmployeeDetailResponse findEmployeeDetail(
            @Param("organizationId") Long organizationId,
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("rewardTargetPercent") int rewardTargetPercent,
            @Param("minimumMonthlyUsageCount") int minimumMonthlyUsageCount
    );

    List<CompanyAdminDashboardEmployeeDailyUsageResponse> findEmployeeDailyUsages(
            @Param("organizationId") Long organizationId,
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
