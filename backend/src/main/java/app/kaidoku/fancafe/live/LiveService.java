package app.kaidoku.fancafe.live;

import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.live.dto.LiveStatusResponse;
import app.kaidoku.fancafe.live.dto.LiveUpdateRequest;
import app.kaidoku.fancafe.setting.SiteSetting;
import app.kaidoku.fancafe.setting.SiteSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 라이브 배너 상태 조회/설정 + 씨미 폴링 결과 반영. 씨미 호출 자체는 infra/seeme에 격리. */
@Service
@Transactional(readOnly = true)
public class LiveService {

    private final LiveStatusRepository liveStatusRepository;
    private final SiteSettingRepository siteSettingRepository;

    public LiveService(LiveStatusRepository liveStatusRepository,
                       SiteSettingRepository siteSettingRepository) {
        this.liveStatusRepository = liveStatusRepository;
        this.siteSettingRepository = siteSettingRepository;
    }

    /** 공개 라이브 배너 상태. 행이 없으면 안전 폴백(OFF). */
    public LiveStatusResponse getPublicStatus() {
        return liveStatusRepository.findFirstByOrderByIdAsc()
                .map(s -> LiveStatusResponse.from(s, channelId()))
                .orElseGet(LiveStatusResponse::off);
    }

    /** 관리자 설정(모드/제목/링크 + 씨미 채널 ID). */
    @Transactional
    public LiveStatusResponse updateSetting(LiveUpdateRequest request) {
        LiveStatus status = getStatusRow();
        status.applySetting(request.mode(), request.title(), request.streamUrl());
        saveChannelId(request.channelId());
        return LiveStatusResponse.from(status, channelId());
    }

    /** 폴러가 AUTO 모드일 때만 채널 ID를 받아간다(아니면 null → 폴링 생략). */
    public String getAutoPollChannelId() {
        boolean auto = liveStatusRepository.findFirstByOrderByIdAsc()
                .map(s -> s.getOverrideMode() == LiveOverrideMode.AUTO)
                .orElse(false);
        if (!auto) {
            return null;
        }
        String channelId = channelId();
        return (channelId == null || channelId.isBlank()) ? null : channelId;
    }

    /** 씨미 폴링 결과 캐시 반영(SeemeLivePoller 전용). */
    @Transactional
    public void applyPoll(boolean live, String title) {
        getStatusRow().applyPoll(live, title);
    }

    private LiveStatus getStatusRow() {
        return liveStatusRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> ApiException.notFound("라이브 상태 행이 없습니다. 마이그레이션(V5)을 확인하세요."));
    }

    private String channelId() {
        return siteSettingRepository.findById(SiteSetting.KEY_SEEME_CHANNEL_ID)
                .map(SiteSetting::getValue)
                .orElse(null);
    }

    private void saveChannelId(String channelId) {
        String normalized = (channelId == null || channelId.isBlank()) ? null : channelId.trim();
        siteSettingRepository.findById(SiteSetting.KEY_SEEME_CHANNEL_ID)
                .ifPresentOrElse(
                        s -> s.changeValue(normalized),
                        () -> siteSettingRepository.save(
                                SiteSetting.of(SiteSetting.KEY_SEEME_CHANNEL_ID, normalized)));
    }
}
