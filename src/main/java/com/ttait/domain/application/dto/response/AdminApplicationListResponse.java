package com.ttait.domain.application.dto.response;

import java.util.List;

// 관리자 협약 신청 목록 응답 (페이징 메타 + items)
public record AdminApplicationListResponse(
        long totalCount,
        int page,
        int size,
        List<AdminApplicationListItem> items
) {
}
