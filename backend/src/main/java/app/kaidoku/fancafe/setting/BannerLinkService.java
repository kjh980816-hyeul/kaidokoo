package app.kaidoku.fancafe.setting;

import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.setting.dto.BannerItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 배너 항목(이름+링크 리스트) 조회/설정. 값은 site_setting의 단일 키(JSON)로 저장한다.
 * 관리자가 자유롭게 추가/삭제할 수 있다. 코드 하드코딩 금지 룰에 따라 값은 DB에서.
 */
@Service
@Transactional(readOnly = true)
public class BannerLinkService {

    /** URL 값 최대 길이. */
    private static final int MAX_URL_LENGTH = 500;
    /** 이름 최대 길이. */
    private static final int MAX_LABEL_LENGTH = 30;
    /** 배너 최대 개수(설정값 길이 한계 보호). */
    private static final int MAX_ITEMS = 12;

    private final SiteSettingRepository siteSettingRepository;
    private final ObjectMapper objectMapper;

    public BannerLinkService(SiteSettingRepository siteSettingRepository, ObjectMapper objectMapper) {
        this.siteSettingRepository = siteSettingRepository;
        this.objectMapper = objectMapper;
    }

    /** 공개 배너 항목. BANNER_ITEMS(JSON)가 있으면 그걸, 없으면 레거시 5키에서 폴백 구성. */
    public List<BannerItem> getItems() {
        String json = read(SiteSetting.KEY_BANNER_ITEMS);
        if (json != null) {
            try {
                return objectMapper.readValue(json, new TypeReference<List<BannerItem>>() {});
            } catch (JsonProcessingException e) {
                return List.of();
            }
        }
        return legacyItems();
    }

    /** 배너 항목 일괄 교체(관리자). 빈 항목 제거 + 각 항목 검증 후 JSON 1키로 저장. */
    @Transactional
    public List<BannerItem> updateItems(List<BannerItem> items) {
        List<BannerItem> normalized = new ArrayList<>();
        if (items != null) {
            for (BannerItem item : items) {
                String label = item.label() == null ? "" : item.label().trim();
                String url = item.url() == null ? "" : item.url().trim();
                if (label.isEmpty() && url.isEmpty()) {
                    continue; // 빈 줄은 무시
                }
                normalized.add(new BannerItem(validateLabel(label), validateUrl(url)));
            }
        }
        if (normalized.size() > MAX_ITEMS) {
            throw ApiException.badRequest("배너는 최대 " + MAX_ITEMS + "개까지 등록할 수 있습니다.");
        }
        try {
            upsert(SiteSetting.KEY_BANNER_ITEMS, objectMapper.writeValueAsString(normalized));
        } catch (JsonProcessingException e) {
            throw ApiException.badRequest("배너 저장에 실패했습니다.");
        }
        return getItems();
    }

    private List<BannerItem> legacyItems() {
        List<BannerItem> list = new ArrayList<>();
        addLegacy(list, "유튜브", SiteSetting.KEY_BANNER_YOUTUBE);
        addLegacy(list, "X", SiteSetting.KEY_BANNER_X);
        addLegacy(list, "씨미", SiteSetting.KEY_BANNER_SEEME);
        addLegacy(list, "팬심", SiteSetting.KEY_BANNER_FANCIM);
        addLegacy(list, "팬심M", SiteSetting.KEY_BANNER_FANCIMM);
        return list;
    }

    private void addLegacy(List<BannerItem> list, String label, String key) {
        String url = read(key);
        if (url != null) {
            list.add(new BannerItem(label, url));
        }
    }

    private String read(String key) {
        return siteSettingRepository.findById(key)
                .map(SiteSetting::getValue)
                .filter(v -> v != null && !v.isBlank())
                .orElse(null);
    }

    private String validateLabel(String label) {
        if (label.isEmpty()) {
            throw ApiException.badRequest("배너 이름을 입력해 주세요.");
        }
        if (label.length() > MAX_LABEL_LENGTH) {
            throw ApiException.badRequest("배너 이름은 " + MAX_LABEL_LENGTH + "자 이하여야 합니다.");
        }
        return label;
    }

    private String validateUrl(String url) {
        if (url.length() > MAX_URL_LENGTH) {
            throw ApiException.badRequest("링크는 " + MAX_URL_LENGTH + "자 이하여야 합니다.");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw ApiException.badRequest("링크는 http:// 또는 https:// 로 시작해야 합니다: " + url);
        }
        return url;
    }

    private void upsert(String key, String value) {
        siteSettingRepository.findById(key)
                .ifPresentOrElse(
                        s -> s.changeValue(value),
                        () -> siteSettingRepository.save(SiteSetting.of(key, value)));
    }
}
