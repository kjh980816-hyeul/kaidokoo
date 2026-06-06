package app.kaidoku.fancafe.live.dto;

import app.kaidoku.fancafe.live.LiveStatus;

import java.time.LocalDateTime;

/** 라이브 배너 공개 응답. 꺼져 있으면 제목/링크는 노출하지 않는다. */
public record LiveStatusResponse(
        boolean live,
        String title,
        String streamUrl,
        LocalDateTime updatedAt
) {
    public static LiveStatusResponse from(LiveStatus s) {
        boolean live = s.isEffectiveLive();
        return new LiveStatusResponse(
                live,
                live ? s.getTitle() : null,
                live ? s.getStreamUrl() : null,
                s.getUpdatedAt());
    }

    /** 상태 행이 아직 없을 때의 안전 폴백(배너 OFF). */
    public static LiveStatusResponse off() {
        return new LiveStatusResponse(false, null, null, null);
    }
}
