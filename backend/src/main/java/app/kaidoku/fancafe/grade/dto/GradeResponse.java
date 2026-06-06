package app.kaidoku.fancafe.grade.dto;

import app.kaidoku.fancafe.grade.Grade;

/** 등급 응답. 닉네임 옆 칭호/뱃지 렌더에 사용. */
public record GradeResponse(
        Long id,
        String name,
        int sortOrder,
        String badgeColor,
        boolean isDefault
) {
    public static GradeResponse from(Grade g) {
        return new GradeResponse(g.getId(), g.getName(), g.getSortOrder(), g.getBadgeColor(), g.isDefault());
    }
}
