package app.kaidoku.fancafe.comment.dto;

import app.kaidoku.fancafe.comment.Comment;
import app.kaidoku.fancafe.grade.Grade;

import java.time.LocalDateTime;

/** 댓글 응답. 삭제는 하드 삭제(행 제거)라 자리표시가 없다. likeCount/liked는 서비스가 일괄 계산해 주입. */
public record CommentResponse(
        Long id,
        Long parentId,
        String content,
        String authorNickname,
        String authorGradeName,
        String authorGradeColor,
        long likeCount,
        boolean liked,
        LocalDateTime createdAt
) {
    public static CommentResponse from(Comment c, long likeCount, boolean liked) {
        Grade grade = c.getAuthor().getGrade();
        return new CommentResponse(
                c.getId(),
                c.getParentId(),
                c.getContent(),
                c.getAuthor().getNickname(),
                grade != null ? grade.getName() : null,
                grade != null ? grade.getBadgeColor() : null,
                likeCount,
                liked,
                c.getCreatedAt());
    }
}
