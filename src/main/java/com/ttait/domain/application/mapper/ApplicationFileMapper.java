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
}
