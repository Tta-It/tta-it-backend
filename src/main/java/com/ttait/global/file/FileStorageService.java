package com.ttait.global.file;

import com.ttait.global.exception.BusinessException;
import com.ttait.global.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 로컬 디스크 기반 파일 저장소
 * 현재는 외부 스토리지를 사용하지 않으므로 실제 설정 경로 아래에 파일 저장함
 */
@Slf4j
@Service
public class FileStorageService {

    /**
     * 허용 확장자 (소문자 기준)
     * 신청서에 첨부 가능한 문서 형식
     */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "hwp", "hwpx");

    private final Path baseDir;

    public FileStorageService(@Value("${app.file.upload-dir}") String uploadDir) {
        this.baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(baseDir);
            log.info("File storage initialized at {}", baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장 디렉터리 생성 실패: " + baseDir, e);
        }
    }

    /**
     * 하나의 업로드 파일을 검증 후 지정된 하위 디렉터리(subdirId) 에 저장
     */
    public StoredFile store(MultipartFile file, Long subdirId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        String originalName = file.getOriginalFilename();
        String extension = extractExtension(originalName);
        // 허용 확장자 검증 — 서버단에서 한 번 더 체크 (클라이언트 조작 방지)
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_FILE_EXTENSION);
        }

        // UUID 기반 파일명으로 저장 — 원본 파일명의 한글/공백/특수문자 이슈 회피
        String storedFileName = UUID.randomUUID() + "." + extension.toLowerCase();
        Path targetDir = baseDir.resolve(String.valueOf(subdirId));
        Path targetPath = targetDir.resolve(storedFileName);

        try {
            Files.createDirectories(targetDir);
            // REPLACE_EXISTING: UUID 충돌 케이스 대비
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", targetPath, e);
            throw new IllegalStateException("파일 저장 중 오류가 발생했습니다.", e);
        }

        // DB 에는 base 디렉터리를 뺀 상대 경로를 저장, 환경 이동 용이하게 함
        String relativePath = baseDir.relativize(targetPath).toString();

        return new StoredFile(
                storedFileName,
                relativePath,
                file.getSize(),
                file.getContentType()
        );
    }

    /**
     * 상대 경로를 Spring Resource 로 반환 (다운로드 시 사용)
     * 경로 탈출(path traversal)보안 취약점 공격 방지를 위해 base 밖을 가리키는 경로는 거부
     */
    public Resource loadAsResource(String relativePath) {
        Path absolute = baseDir.resolve(relativePath).normalize();
        if (!absolute.startsWith(baseDir)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        try {
            Resource resource = new UrlResource(absolute.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
            return resource;
        } catch (IOException e) {
            throw new IllegalStateException("파일 로드 실패: " + relativePath, e);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return null;
        }
        return filename.substring(dot + 1);
    }
}
