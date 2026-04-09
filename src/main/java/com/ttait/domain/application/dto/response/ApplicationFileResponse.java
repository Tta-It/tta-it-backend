package com.ttait.domain.application.dto.response;

import com.ttait.domain.application.domain.ApplicationFile;

// 첨부 파일 메타 정보 (다운로드 링크용)
public record ApplicationFileResponse(
        Long id,
        String originalFileName,
        Long fileSize,
        String contentType
) {
    public static ApplicationFileResponse from(ApplicationFile file) {
        return new ApplicationFileResponse(
                file.getId(),
                file.getOriginalFileName(),
                file.getFileSize(),
                file.getContentType()
        );
    }
}
