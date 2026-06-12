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
        return from(p, p.getViewCount());
    }

    /** 조회수를 DB에서 원자적으로 증가시킨 경우, 응답에 표시할 값을 따로 받는다. */
    public static PostDetailResponse from(Post p, int viewCount) {
        return new PostDetailResponse(
                p.getId(), p.getBoard().getCode(), p.getBoard().getNameKr(),
                p.getTitle(), p.getContent(), p.getAuthor().getNickname(),
                viewCount, p.getLikeCount(), p.isPinned(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
