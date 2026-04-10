package com.ttait.domain.application.mapper;

import com.ttait.domain.application.domain.ApplicationFile;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApplicationFileMapper {

    int insert(ApplicationFile file);

    /**
     * 특정 사용자가 업로드한 모든 첨부 파일을 최신 순으로 조회
     * 기업 관리자의 "내 신청 조회" 시 사용
     */
    List<ApplicationFile> findByUserId(@Param("userId") Long userId);

    ApplicationFile findById(@Param("id") Long id);

    /**
     * 특정 사용자의 첨부 파일 메타데이터를 전부 삭제
     * 재신청(Resubmit) 시 기존 파일 레코드를 일괄 정리할 때 사용
     */
    int deleteByUserId(@Param("userId") Long userId);
}
