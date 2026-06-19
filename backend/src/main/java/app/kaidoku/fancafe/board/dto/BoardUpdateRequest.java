package app.kaidoku.fancafe.board.dto;

import app.kaidoku.fancafe.board.BoardType;
import app.kaidoku.fancafe.common.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

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

        boolean visible,

        /** 말머리 라벨 목록(선택). 각 20자 이하, 최대 12개. 서비스에서 쉼표로 합쳐 저장. */
        List<String> categories
) {
}
