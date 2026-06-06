-- V3: 회원 등급(별명/칭호) 시스템. 관리자가 CRUD하고 회원에게 수동 배정한다(20-domain-rules).
-- 등급은 데이터다 — 코드에 등급명/서열 하드코딩 금지. MySQL 8 / H2(MySQL 모드) 호환.

CREATE TABLE grade (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50) NOT NULL,                 -- 칭호/별명 (예: 견습 선원 → 갑판원 → 항해사)
    sort_order  INT         NOT NULL DEFAULT 0,       -- 서열 (작을수록 하위 등급)
    badge_color VARCHAR(20),                          -- 뱃지 색 (#RRGGBB)
    is_default  BOOLEAN     NOT NULL DEFAULT FALSE,   -- 신규 회원 기본 등급 (앱에서 단일 보장)
    created_at  DATETIME    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_grade_name UNIQUE (name)
);

-- 회원에 등급 배정(FK). 인증 전 기존 회원은 NULL 허용.
ALTER TABLE member ADD COLUMN grade_id BIGINT NULL;
ALTER TABLE member ADD CONSTRAINT fk_member_grade FOREIGN KEY (grade_id) REFERENCES grade(id);
