-- V10: 댓글 좋아요 + site_setting 값 길이 확장(배너 JSON 수용). MySQL 8 / H2(MySQL 모드) 호환.

CREATE TABLE comment_like (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    comment_id BIGINT   NOT NULL,
    member_id  BIGINT   NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_comment_like UNIQUE (comment_id, member_id),                  -- (댓글,회원) 1회만
    CONSTRAINT fk_clike_comment FOREIGN KEY (comment_id) REFERENCES comment(id) ON DELETE CASCADE,
    CONSTRAINT fk_clike_member  FOREIGN KEY (member_id)  REFERENCES member(id)  ON DELETE CASCADE
);

CREATE INDEX idx_comment_like_comment ON comment_like (comment_id);

-- 배너를 자유 추가(이름+링크 리스트 JSON)로 저장하기 위해 설정값 길이 확장.
ALTER TABLE site_setting MODIFY setting_value VARCHAR(4000);
