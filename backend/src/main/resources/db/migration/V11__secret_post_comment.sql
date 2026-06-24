-- V11: 비밀글 / 비밀댓글. 작성자 본인과 운영자(ADMIN)만 열람 가능(서버에서 강제).
-- is_secret = true면 목록/상세/댓글 목록에서 비인가 열람자에게 노출하지 않는다.
ALTER TABLE post    ADD COLUMN is_secret BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE comment ADD COLUMN is_secret BOOLEAN NOT NULL DEFAULT FALSE;
