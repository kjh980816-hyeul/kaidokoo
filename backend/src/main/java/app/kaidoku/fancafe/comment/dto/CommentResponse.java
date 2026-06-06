package app.kaidoku.fancafe.comment.dto;

import app.kaidoku.fancafe.comment.Comment;
import app.kaidoku.fancafe.grade.Grade;

import java.time.LocalDateTime;

/** 댓글 응답. 삭제된 댓글은 본문을 가리고 자리표시만 남긴다(스레드 보존). */
public record CommentResponse(
        Long id,
        Long parentId,
        String content,
        String authorNickname,
        String authorGradeName,
        String authorGradeColor,
        boolean deleted,
        LocalDateTime createdAt
) {
    public static CommentResponse from(Comment c) {
        boolean deleted = c.isDeleted();
        Grade grade = c.getAuthor().getGrade();
        return new CommentResponse(
                c.getId(),
                c.getParentId(),
                deleted ? "삭제된 댓글입니다." : c.getContent(),
                c.getAuthor().getNickname(),
                grade != null ? grade.getName() : null,
                grade != null ? grade.getBadgeColor() : null,
                deleted,
                c.getCreatedAt());
    }
}
