package app.kaidoku.fancafe.post;

/** 글 상태. 물리 삭제 대신 소프트 딜리트(20-domain-rules). */
public enum PostStatus {
    PUBLISHED,
    HIDDEN,
    DELETED
}
