package app.kaidoku.fancafe.setting;

import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.setting.dto.BrandingResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 사이트 브랜딩(로고) 설정. 값은 site_setting LOGO_URL 키에 저장. */
@Service
@Transactional(readOnly = true)
public class BrandingService {

    private static final int MAX_URL_LENGTH = 500;

    private final SiteSettingRepository siteSettingRepository;

    public BrandingService(SiteSettingRepository siteSettingRepository) {
        this.siteSettingRepository = siteSettingRepository;
    }

    public BrandingResponse get() {
        String logoUrl = siteSettingRepository.findById(SiteSetting.KEY_LOGO_URL)
                .map(SiteSetting::getValue)
                .filter(v -> v != null && !v.isBlank())
                .orElse(null);
        return new BrandingResponse(logoUrl);
    }

    /** 로고 URL 설정(관리자). 빈값이면 해제(기본 엠블럼). 업로드 경로 또는 http(s)만 허용. */
    @Transactional
    public BrandingResponse updateLogo(String logoUrl) {
        String value = (logoUrl == null || logoUrl.isBlank()) ? null : logoUrl.trim();
        if (value != null) {
            if (value.length() > MAX_URL_LENGTH) {
                throw ApiException.badRequest("로고 URL이 너무 깁니다.");
            }
            boolean ok = value.startsWith("/uploads/")
                    || value.startsWith("http://") || value.startsWith("https://");
            if (!ok) {
                throw ApiException.badRequest("로고는 업로드 이미지(/uploads/...) 또는 http(s) URL이어야 합니다.");
            }
        }
        siteSettingRepository.findById(SiteSetting.KEY_LOGO_URL)
                .ifPresentOrElse(
                        s -> s.changeValue(value),
                        () -> siteSettingRepository.save(SiteSetting.of(SiteSetting.KEY_LOGO_URL, value)));
        return get();
    }
}
