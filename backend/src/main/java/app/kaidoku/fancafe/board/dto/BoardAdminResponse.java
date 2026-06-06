package app.kaidoku.fancafe.board.dto;

import app.kaidoku.fancafe.board.Board;
import app.kaidoku.fancafe.board.BoardType;
import app.kaidoku.fancafe.common.Role;

/** 관리자용 게시판 응답. 공개 응답과 달리 노출 여부·작성 권한까지 포함. */
public record BoardAdminResponse(
        Long id,
        String code,
        String nameKr,
        String nameEn,
        String description,
        BoardType type,
        int sortOrder,
        boolean visible,
        Role writeRole
) {
    public static BoardAdminResponse from(Board b) {
        return new BoardAdminResponse(
                b.getId(), b.getCode(), b.getNameKr(), b.getNameEn(), b.getDescription(),
                b.getType(), b.getSortOrder(), b.isVisible(), b.getWriteRole());
    }
}
