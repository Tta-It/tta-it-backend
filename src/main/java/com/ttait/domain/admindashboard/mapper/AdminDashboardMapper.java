package com.ttait.domain.admindashboard.mapper;

import com.ttait.domain.admindashboard.dto.response.AdminPriorityRegionResponse;
import com.ttait.domain.admindashboard.dto.response.AdminPendingApplicationResponse;
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

    // 기간 내 전체 이용량 KPI
    Long findTotalUsageCount(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // 대기 중인 협약 신청 수 KPI
    Integer countPendingApplications();

    // 배치 검토 필요 지역 수 KPI
    Integer countPriorityRegions(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("previousFrom") LocalDate previousFrom,
            @Param("previousTo") LocalDate previousTo
    );

    // 지역별 이용량 Bar Chart
    List<AdminRegionUsageResponse> findRegionUsages(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // 기간별 이용량 추이 Line Chart
    List<AdminUsageTrendResponse> findUsageTrends(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // 수요 상위 지역 TOP 5
    List<AdminTopRegionResponse> findTopRegions(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // 배치 우선 검토 지역 목록
    List<AdminPriorityRegionResponse> findPriorityRegions(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("previousFrom") LocalDate previousFrom,
            @Param("previousTo") LocalDate previousTo
    );

    // 최근 대기 중 협약 신청 목록
    List<AdminPendingApplicationResponse> findPendingApplications();
}
