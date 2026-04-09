package com.ttait.domain.organization.mapper;

import com.ttait.domain.organization.domain.AgreementStatus;
import com.ttait.domain.organization.domain.Organization;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrganizationMapper {

    Organization findById(@Param("id") Long id);

    Organization findByBusinessNumber(@Param("businessNumber") String businessNumber);

    int insert(Organization organization);

    int updateApplicationSubmission(Organization organization); // 협약서 제출

    // 관리자 협약 신청 목록 조회 (DRAFT 제외, submitted_at DESC)
    List<Organization> searchApplications(
            @Param("keyword") String keyword,
            @Param("status") AgreementStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    // 검색 조건에 해당하는 총 건수
    long countApplications(
            @Param("keyword") String keyword,
            @Param("status") AgreementStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // 승인: PENDING -> ACTIVE, approved_at 기록
    int approveApplication(@Param("id") Long id);

    // 반려: PENDING -> REJECTED, review_comment 저장
    int rejectApplication(@Param("id") Long id, @Param("reviewComment") String reviewComment);
}
