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
 * 기업 관리자 대시보드 화면에 필요한 임직원 이용 통계를 조회하는 MyBatis 매퍼입니다.
 */
@Mapper
public interface CompanyAdminDashboardMapper {

    /**
     * 선택 월 기준 전체 이용 횟수, 이동 거리, 탄소 절감량을 집계합니다.
     */
    CompanyAdminDashboardSummaryResponse findSummary(
            @Param("organizationId") Long organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 선택 월 기준 임직원별 이용 요약 목록을 조회합니다.
     */
    List<CompanyAdminDashboardEmployeeUsageResponse> findEmployeeUsages(
            @Param("organizationId") Long organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 선택한 임직원의 선택 월 이용 요약 정보를 조회합니다.
     */
    CompanyAdminDashboardEmployeeDetailResponse findEmployeeDetail(
            @Param("organizationId") Long organizationId,
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 선택한 임직원의 선택 월 일자별 이용내역을 조회합니다.
     */
    List<CompanyAdminDashboardEmployeeDailyUsageResponse> findEmployeeDailyUsages(
            @Param("organizationId") Long organizationId,
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 선택한 임직원의 가장 최근 이용내역 1건을 조회합니다.
     */
    CompanyAdminDashboardEmployeeDailyUsageResponse findLatestEmployeeUsage(
            @Param("organizationId") Long organizationId,
            @Param("employeeId") Long employeeId
    );

    /**
     * 전체 임직원의 최근 월별 이용 횟수를 차트용으로 조회합니다.
     */
    List<CompanyAdminDashboardMonthlyUsageResponse> findMonthlyUsages(
            @Param("organizationId") Long organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
