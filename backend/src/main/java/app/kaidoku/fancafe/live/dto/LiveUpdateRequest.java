package app.kaidoku.fancafe.live.dto;

import app.kaidoku.fancafe.live.LiveOverrideMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 라이브 배너 설정(관리자). mode=AUTO면 씨미 폴링이 ON/OFF를 결정하고,
 * FORCE_ON/FORCE_OFF는 수동 오버라이드. channelId는 씨미 채널 식별자(site_setting 저장).
 */
public record LiveUpdateRequest(
        @NotNull(message = "모드는 필수입니다")
        LiveOverrideMode mode,

        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        String title,

        @Size(max = 500, message = "링크가 너무 깁니다")
        String streamUrl,

        @Size(max = 100, message = "채널 ID가 너무 깁니다")
        String channelId
) {
}
