package app.kaidoku.fancafe.board.dto;

import app.kaidoku.fancafe.board.BoardType;
import app.kaidoku.fancafe.common.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 게시판 생성 요청(관리자). code는 URL 슬러그라 생성 후 변경 불가. */
public record BoardCreateRequest(
        @NotBlank(message = "게시판 코드는 필수입니다")
        @Pattern(regexp = "[a-z0-9-]{2,50}", message = "코드는 영소문자/숫자/하이픈 2~50자여야 합니다")
        String code,

        @NotBlank(message = "한글 게시판명은 필수입니다")
        @Size(max = 100)
        String nameKr,

        @Size(max = 100)
        String nameEn,

        @Size(max = 500)
        String description,

        int sortOrder,

        @NotNull(message = "게시판 타입은 필수입니다")
        BoardType type,

        @NotNull(message = "작성 권한은 필수입니다")
        Role writeRole
) {
}
