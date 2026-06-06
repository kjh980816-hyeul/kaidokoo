package app.kaidoku.fancafe.live.dto;

import jakarta.validation.constraints.Size;

/** 라이브 배너 수동 설정(관리자). live=false면 title/streamUrl은 무시된다. */
public record LiveUpdateRequest(
        boolean live,

        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        String title,

        @Size(max = 500, message = "링크가 너무 깁니다")
        String streamUrl
) {
}
