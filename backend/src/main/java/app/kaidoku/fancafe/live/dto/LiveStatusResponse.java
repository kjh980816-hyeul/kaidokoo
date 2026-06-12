package app.kaidoku.fancafe.live.dto;

import app.kaidoku.fancafe.live.LiveOverrideMode;
import app.kaidoku.fancafe.live.LiveStatus;

import java.time.LocalDateTime;

/** 라이브 배너 응답. 꺼져 있으면 제목/링크는 노출하지 않는다. mode/channelId는 관리자 화면용. */
public record LiveStatusResponse(
        boolean live,
        String title,
        String streamUrl,
        LiveOverrideMode mode,
        String channelId,
        LocalDateTime updatedAt
) {
    public static LiveStatusResponse from(LiveStatus s, String channelId) {
        boolean live = s.isEffectiveLive();
        return new LiveStatusResponse(
                live,
                live ? s.getTitle() : null,
                live ? s.getStreamUrl() : null,
                s.getOverrideMode(),
                channelId,
                s.getUpdatedAt());
    }

    /** 상태 행이 아직 없을 때의 안전 폴백(배너 OFF). */
    public static LiveStatusResponse off() {
        return new LiveStatusResponse(false, null, null, LiveOverrideMode.FORCE_OFF, null, null);
    }
}
