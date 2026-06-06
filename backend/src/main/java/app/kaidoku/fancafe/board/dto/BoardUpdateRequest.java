package app.kaidoku.fancafe.board.dto;

import app.kaidoku.fancafe.board.BoardType;
import app.kaidoku.fancafe.common.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 게시판 수정 요청(관리자). code는 변경하지 않는다. */
public record BoardUpdateRequest(
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
        Role writeRole,

        boolean visible
) {
}
