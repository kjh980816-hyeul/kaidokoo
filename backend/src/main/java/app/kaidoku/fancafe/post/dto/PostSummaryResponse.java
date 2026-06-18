package app.kaidoku.fancafe.post.dto;

import app.kaidoku.fancafe.post.Post;

import java.time.LocalDateTime;

/** 게시판 목록 행. thumbnailUrl은 갤러리·카드 등 형식에서 대표 이미지로 쓰인다(없으면 null). */
public record PostSummaryResponse(
        Long id,
        String title,
        String authorNickname,
        int viewCount,
        int likeCount,
        boolean pinned,
        String thumbnailUrl,
        LocalDateTime createdAt
) {
    public static PostSummaryResponse from(Post p) {
        return from(p, null);
    }

    public static PostSummaryResponse from(Post p, String thumbnailUrl) {
        return new PostSummaryResponse(
                p.getId(), p.getTitle(), p.getAuthor().getNickname(),
                p.getViewCount(), p.getLikeCount(), p.isPinned(), thumbnailUrl, p.getCreatedAt());
    }
}
