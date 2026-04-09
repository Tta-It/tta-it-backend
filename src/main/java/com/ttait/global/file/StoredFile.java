package com.ttait.global.file;

/**
 * FileStorageService가 파일을 디스크에 저장한 뒤 반환하는 결과값
 */
public record StoredFile(
        String storedFileName,
        String relativePath,
        long fileSize,
        String contentType
) {
}
