package app.kaidoku.fancafe.like.dto;

/** 좋아요 토글/조회 결과. liked=현재 회원의 좋아요 여부, likeCount=글 총 좋아요 수. */
public record LikeResponse(boolean liked, int likeCount) {
}
