-- V6: 자체 세션 (소셜 로그인 후 발급, ADR-0004).
-- 토큰은 서버 생성 난수. HttpOnly 쿠키로만 전달되고 DB에서 검증·폐기한다.

CREATE TABLE member_session (
    token      VARCHAR(64) NOT NULL,
    member_id  BIGINT      NOT NULL,
    created_at DATETIME    NOT NULL,
    expires_at DATETIME    NOT NULL,
    PRIMARY KEY (token),
    CONSTRAINT fk_session_member FOREIGN KEY (member_id) REFERENCES member(id)
);

CREATE INDEX idx_session_expires ON member_session (expires_at);
