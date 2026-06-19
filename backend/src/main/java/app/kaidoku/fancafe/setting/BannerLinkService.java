package app.kaidoku.fancafe.setting;

import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.setting.dto.BannerLinksRequest;
import app.kaidoku.fancafe.setting.dto.BannerLinksResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 배너 외부 링크(site_setting 5개 키) 조회/설정. 코드 하드코딩 금지 룰에 따라 값은 DB에서. */
@Service
@Transactional(readOnly = true)
public class BannerLinkService {

    /** URL 값 최대 길이. */
    private static final int MAX_URL_LENGTH = 500;

    private final SiteSettingRepository siteSettingRepository;

    public BannerLinkService(SiteSettingRepository siteSettingRepository) {
        this.siteSettingRepository = siteSettingRepository;
    }

    /** 공개 배너 링크. 미설정 키는 null. */
    public BannerLinksResponse getLinks() {
        return new BannerLinksResponse(
                read(SiteSetting.KEY_BANNER_YOUTUBE),
                read(SiteSetting.KEY_BANNER_X),
                read(SiteSetting.KEY_BANNER_SEEME),
                read(SiteSetting.KEY_BANNER_FANCIM),
                read(SiteSetting.KEY_BANNER_FANCIMM));
    }

    /** 배너 링크 일괄 설정(관리자). 각 값 검증 후 5개 키 upsert. 권한 검증은 컨트롤러에서. */
    @Transactional
    public BannerLinksResponse updateLinks(BannerLinksRequest request) {
        upsert(SiteSetting.KEY_BANNER_YOUTUBE, validate(request.youtube()));
        upsert(SiteSetting.KEY_BANNER_X, validate(request.x()));
        upsert(SiteSetting.KEY_BANNER_SEEME, validate(request.seeme()));
        upsert(SiteSetting.KEY_BANNER_FANCIM, validate(request.fancim()));
        upsert(SiteSetting.KEY_BANNER_FANCIMM, validate(request.fancimM()));
        return getLinks();
    }

    private String read(String key) {
        return siteSettingRepository.findById(key)
                .map(SiteSetting::getValue)
                .filter(v -> v != null && !v.isBlank())
                .orElse(null);
    }

    /** 빈값이면 null(해제). 비어있지 않으면 http/https 시작 + 길이 검증. */
    private String validate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_URL_LENGTH) {
            throw ApiException.badRequest("링크는 " + MAX_URL_LENGTH + "자 이하여야 합니다.");
        }
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            throw ApiException.badRequest("링크는 http:// 또는 https:// 로 시작해야 합니다: " + trimmed);
        }
        return trimmed;
    }

    private void upsert(String key, String value) {
        siteSettingRepository.findById(key)
                .ifPresentOrElse(
                        s -> s.changeValue(value),
                        () -> siteSettingRepository.save(SiteSetting.of(key, value)));
    }
}
