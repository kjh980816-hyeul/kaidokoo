package app.kaidoku.fancafe.board.dto;

import app.kaidoku.fancafe.board.Board;
import app.kaidoku.fancafe.board.BoardType;

import java.util.List;

/** 공개 게시판 목록 응답. 엔티티 직접 노출 금지 → DTO 변환. */
public record BoardResponse(
        Long id,
        String code,
        String nameKr,
        String nameEn,
        String description,
        BoardType type,
        int sortOrder,
        List<String> categories,
        boolean hasNew
) {
    /** hasNew를 계산할 수 없는 경우(단건 변환 등)는 false. */
    public static BoardResponse from(Board b) {
        return from(b, false);
    }

    public static BoardResponse from(Board b, boolean hasNew) {
        return new BoardResponse(
                b.getId(), b.getCode(), b.getNameKr(), b.getNameEn(),
                b.getDescription(), b.getType(), b.getSortOrder(),
                b.categoryList(), hasNew);
    }
}
