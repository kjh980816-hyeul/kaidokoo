package app.kaidoku.fancafe.post.dto;

import app.kaidoku.fancafe.post.Post;
import app.kaidoku.fancafe.post.PostImage;

import java.time.LocalDateTime;
import java.util.List;

/** 게시글 상세. authorId는 프론트가 "내 글 삭제" 버튼 노출을 판단하는 데 쓴다(권한은 서버 재검증). */
public record PostDetailResponse(
        Long id,
        String boardCode,
        String boardNameKr,
        String title,
        String content,
        String category,
        Long authorId,
        String authorNickname,
        int viewCount,
        int likeCount,
        boolean pinned,
        boolean secret,
        List<String> imageUrls,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostDetailResponse from(Post p) {
        return from(p, p.getViewCount());
    }

    /** 조회수를 DB에서 원자적으로 증가시킨 경우, 응답에 표시할 값을 따로 받는다. */
    public static PostDetailResponse from(Post p, int viewCount) {
        List<String> imageUrls = p.getImages().stream().map(PostImage::getUrl).toList();
        return new PostDetailResponse(
                p.getId(), p.getBoard().getCode(), p.getBoard().getNameKr(),
                p.getTitle(), p.getContent(), p.getCategory(),
                p.getAuthor().getId(), p.getAuthor().getNickname(),
                viewCount, p.getLikeCount(), p.isPinned(), p.isSecret(), imageUrls,
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
