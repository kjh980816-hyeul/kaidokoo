package app.kaidoku.fancafe.infra.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 업로드 파일을 {@code /uploads/**}로 정적 서빙(개발·폴백용).
 * 운영에서는 Nginx가 같은 디렉터리를 직접 서빙하지만, 둘 다 같은 경로 규약을 쓴다.
 */
@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    private final String location;

    public UploadResourceConfig(@Value("${app.upload.dir:uploads}") String uploadDir) {
        // file: URI 끝에 / 가 있어야 하위 경로가 매핑된다.
        this.location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }
}
