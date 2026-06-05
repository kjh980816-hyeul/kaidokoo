package app.kaidoku.fancafe.post.dto;

import app.kaidoku.fancafe.post.Post;

import java.time.LocalDateTime;

/** 게시판 목록 행. */
public record PostSummaryResponse(
        Long id,
        String title,
        String authorNickname,
        int viewCount,
        int likeCount,
        boolean pinned,
        LocalDateTime createdAt
) {
    public static PostSummaryResponse from(Post p) {
        return new PostSummaryResponse(
                p.getId(), p.getTitle(), p.getAuthor().getNickname(),
                p.getViewCount(), p.getLikeCount(), p.isPinned(), p.getCreatedAt());
    }
}
