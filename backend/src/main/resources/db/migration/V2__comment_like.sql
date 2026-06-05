-- V2: 댓글 + 좋아요 (P2 첫 조각). MySQL 8 / H2(MySQL 모드) 호환.

CREATE TABLE comment (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    post_id    BIGINT        NOT NULL,
    author_id  BIGINT        NOT NULL,
    parent_id  BIGINT        NULL,                         -- 대댓글(자기참조). NULL이면 최상위
    content    VARCHAR(1000) NOT NULL,
    status     VARCHAR(20)   NOT NULL DEFAULT 'PUBLISHED', -- PUBLISHED/HIDDEN/DELETED (소프트딜리트)
    created_at DATETIME      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_comment_post   FOREIGN KEY (post_id)   REFERENCES post(id),
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES member(id),
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comment(id)
);

CREATE INDEX idx_comment_post ON comment (post_id, created_at);

CREATE TABLE post_like (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    member_id  BIGINT   NOT NULL,
    post_id    BIGINT   NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_post_like UNIQUE (member_id, post_id),    -- (회원,글) 1회만
    CONSTRAINT fk_like_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT fk_like_post   FOREIGN KEY (post_id)   REFERENCES post(id)
);
