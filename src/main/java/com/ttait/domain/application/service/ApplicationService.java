package com.ttait.domain.application.service;

import com.ttait.domain.application.dto.request.SubmitApplicationRequest;
import com.ttait.domain.application.dto.response.MyApplicationResponse;
import com.ttait.domain.application.dto.response.SubmitApplicationResponse;

public interface ApplicationService {

    // 기업 관리자의 협약 신청서 제출
    SubmitApplicationResponse submit(Long userId, Long organizationId, SubmitApplicationRequest request);

    // 기업 관리자의 내 협약 신청 현황 조회
    MyApplicationResponse getMyApplication(Long userId, Long organizationId);
}
