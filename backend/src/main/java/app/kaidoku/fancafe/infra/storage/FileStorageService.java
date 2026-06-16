package app.kaidoku.fancafe.infra.storage;

import app.kaidoku.fancafe.common.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/**
 * 업로드 파일(현재는 아바타) 저장. 로컬 디스크에 저장하고 {@code /uploads/...} 경로를 반환한다.
 * 저장 위치는 {@code app.upload.dir}(운영=환경변수 UPLOAD_DIR). 운영 Nginx가 같은 디렉터리를
 * {@code /uploads/}로 정적 서빙한다(개발은 Spring ResourceHandler).
 *
 * <p>보안(30-constraints): MIME 화이트리스트 + 용량 제한 + 서버 생성 파일명(원본명·경로 미사용 → 경로조작 차단).
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    /** 허용 이미지 MIME → 확장자. */
    private static final Map<String, String> ALLOWED = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif");
    private static final long MAX_BYTES = 2L * 1024 * 1024; // 2MB
    private static final String URL_PREFIX = "/uploads/avatars/";

    private final Path avatarDir;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.avatarDir = Paths.get(uploadDir, "avatars").toAbsolutePath().normalize();
        try {
            Files.createDirectories(avatarDir);
        } catch (IOException e) {
            throw new IllegalStateException("업로드 디렉터리 생성 실패: " + avatarDir, e);
        }
    }

    /** 아바타 저장 후 공개 URL 경로 반환. */
    public String storeAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("이미지 파일을 선택해 주세요.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw ApiException.badRequest("이미지는 2MB 이하만 올릴 수 있습니다.");
        }
        String ext = ALLOWED.get(file.getContentType());
        if (ext == null) {
            throw ApiException.badRequest("JPG·PNG·WEBP·GIF 이미지만 올릴 수 있습니다.");
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = avatarDir.resolve(filename).normalize();
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("이미지 저장 실패", e);
        }
        return URL_PREFIX + filename;
    }

    /** 우리가 저장한 아바타 파일이면 삭제(이전 프사 정리). 외부 URL·null은 무시. */
    public void deleteIfManaged(String url) {
        if (url == null || !url.startsWith(URL_PREFIX)) {
            return;
        }
        String filename = url.substring(URL_PREFIX.length());
        Path target = avatarDir.resolve(filename).normalize();
        // 디렉터리 이탈 방지(혹시 모를 경로조작 차단).
        if (!target.startsWith(avatarDir)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("이전 아바타 삭제 실패: {}", target, e);
        }
    }
}
