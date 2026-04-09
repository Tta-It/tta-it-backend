package com.ttait.domain.application.domain;

import com.ttait.global.common.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 협약 신청 첨부 파일 T_APPLICATION_FILE테이블과 매핑
 * 파일은 로컬 파일시스템이므로, 로컬 디스크({@code app.file.upload-dir})에 저장
 */
@Getter
@Setter
@NoArgsConstructor
public class ApplicationFile extends BaseEntity {

    private Long id;
    private Long userId;
    private String originalFileName;    // 사용자가 업로드한 원본 파일명
    private String storedFileName;      // 디스크에 실제 저장된 파일명 (UUID 기반, 충돌 방지)
    private String filePath;            // upload-dir 기준 상대 경로
    private Long fileSize;
    private String contentType;

    @Builder
    public ApplicationFile(Long id, Long userId, String originalFileName, String storedFileName,
                           String filePath, Long fileSize, String contentType) {
        this.id = id;
        this.userId = userId;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }
}
