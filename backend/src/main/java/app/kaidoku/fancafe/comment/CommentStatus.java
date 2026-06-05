package app.kaidoku.fancafe.comment;

/** 댓글 상태. 물리 삭제 대신 소프트 딜리트. */
public enum CommentStatus {
    PUBLISHED,
    HIDDEN,
    DELETED
}
