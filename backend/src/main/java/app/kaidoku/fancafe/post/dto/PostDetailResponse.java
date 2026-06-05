package app.kaidoku.fancafe.post.dto;

import app.kaidoku.fancafe.post.Post;

import java.time.LocalDateTime;

/** 게시글 상세. */
public record PostDetailResponse(
        Long id,
        String boardCode,
        String boardNameKr,
        String title,
        String content,
        String authorNickname,
        int viewCount,
        int likeCount,
        boolean pinned,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostDetailResponse from(Post p) {
        return new PostDetailResponse(
                p.getId(), p.getBoard().getCode(), p.getBoard().getNameKr(),
                p.getTitle(), p.getContent(), p.getAuthor().getNickname(),
                p.getViewCount(), p.getLikeCount(), p.isPinned(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
