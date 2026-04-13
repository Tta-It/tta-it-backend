package com.ttait.domain.admindashboard.mapper;

import com.ttait.domain.admindashboard.dto.projection.AdminPriorityRegionProjection;
import com.ttait.domain.admindashboard.dto.projection.AdminUsageAggregateProjection;
import com.ttait.domain.admindashboard.dto.response.AdminPendingApplicationResponse;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 총 관리자 대시보드 화면별 집계 조회를 담당하는 MyBatis 매퍼.
 */
@Mapper
public interface AdminDashboardMapper {

    // 대기 중인 협약 신청 수 KPI
    Integer countPendingApplications();

    LocalDate findEarliestUsageStatDate();

    LocalDate findLatestUsageStatDate();

    List<AdminUsageAggregateProjection> findUsageAggregates(@Param("from") LocalDate from, @Param("to") LocalDate to);

    List<AdminPriorityRegionProjection> findPriorityRegionCandidates(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("previousFrom") LocalDate previousFrom,
            @Param("previousTo") LocalDate previousTo
    );

    // 최근 대기 중 협약 신청 목록
    List<AdminPendingApplicationResponse> findPendingApplications();
}
