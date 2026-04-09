package com.ttait.domain.admindashboard.mapper;

import com.ttait.domain.admindashboard.dto.response.AdminDashboardSummaryResponse;
import com.ttait.domain.admindashboard.dto.response.AdminPriorityRegionResponse;
import com.ttait.domain.admindashboard.dto.response.AdminRegionUsageResponse;
import com.ttait.domain.admindashboard.dto.response.AdminTopRegionResponse;
import com.ttait.domain.admindashboard.dto.response.AdminUsageTrendResponse;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 총 관리자 대시보드 화면별 집계 조회를 담당하는 MyBatis 매퍼.
 */
@Mapper
public interface AdminDashboardMapper {

    AdminDashboardSummaryResponse findSummary(@Param("from") LocalDate from, @Param("to") LocalDate to);

    List<AdminRegionUsageResponse> findRegionUsages(@Param("from") LocalDate from, @Param("to") LocalDate to);

    List<AdminUsageTrendResponse> findUsageTrends(@Param("from") LocalDate from, @Param("to") LocalDate to);

    List<AdminTopRegionResponse> findTopRegions(@Param("from") LocalDate from, @Param("to") LocalDate to);

    List<AdminPriorityRegionResponse> findPriorityRegions(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
