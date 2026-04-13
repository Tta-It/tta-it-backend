package com.ttait.domain.admindashboard.mapper;

import com.ttait.domain.admindashboard.dto.projection.AdminPriorityRegionProjection;
import com.ttait.domain.admindashboard.dto.projection.AdminUsageAggregateProjection;
import com.ttait.domain.admindashboard.dto.response.AdminPendingApplicationResponse;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 총관리자 대시보드 화면에 필요한 집계 데이터를 조회하는 MyBatis 매퍼입니다.
 */
@Mapper
public interface AdminDashboardMapper {

    /**
     * 승인 대기 중인 협약 신청 수를 조회합니다.
     */
    Integer countPendingApplications();

    /**
     * 대여소 이용 통계 데이터가 존재하는 가장 이른 날짜를 조회합니다.
     */
    LocalDate findEarliestUsageStatDate();

    /**
     * 대여소 이용 통계 데이터가 존재하는 가장 최근 날짜를 조회합니다.
     */
    LocalDate findLatestUsageStatDate();

    /**
     * 지정 기간의 이용량을 일자와 지역 단위로 집계합니다.
     */
    List<AdminUsageAggregateProjection> findUsageAggregates(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /**
     * 현재 기간과 이전 동일 기간을 비교해 배치 우선 검토 후보를 조회합니다.
     */
    List<AdminPriorityRegionProjection> findPriorityRegionCandidates(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("previousFrom") LocalDate previousFrom,
            @Param("previousTo") LocalDate previousTo
    );

    /**
     * 최근 승인 대기 협약 신청 목록을 조회합니다.
     */
    List<AdminPendingApplicationResponse> findPendingApplications();
}
