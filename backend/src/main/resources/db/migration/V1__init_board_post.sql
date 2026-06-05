-- V1: 회원/게시판/게시글 초기 스키마 (수직 슬라이스 범위)
-- MySQL 8 / H2(MySQL 모드) 양쪽 호환 SQL. 전체 11테이블 중 슬라이스에 필요한 3개만.

CREATE TABLE member (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    provider         VARCHAR(20)  NOT NULL,                 -- NAVER/KAKAO/GOOGLE (소셜 3종)
    provider_user_id VARCHAR(190) NOT NULL,                 -- 소셜 제공자 측 식별자
    nickname         VARCHAR(50)  NOT NULL,
    role             VARCHAR(20)  NOT NULL DEFAULT 'MEMBER', -- GUEST/MEMBER/ADMIN
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE', -- ACTIVE/SUSPENDED/WITHDRAWN
    created_at       DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_member_provider UNIQUE (provider, provider_user_id)
);

CREATE TABLE board (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    code        VARCHAR(50)  NOT NULL,                       -- URL 슬러그 (동적, 관리자 CRUD)
    name_kr     VARCHAR(100) NOT NULL,
    name_en     VARCHAR(100),
    description VARCHAR(500),
    sort_order  INT          NOT NULL DEFAULT 0,
    type        VARCHAR(20)  NOT NULL DEFAULT 'GENERAL',     -- GENERAL/GALLERY
    is_visible  BOOLEAN      NOT NULL DEFAULT TRUE,
    write_role  VARCHAR(20)  NOT NULL DEFAULT 'MEMBER',      -- 작성 가능 최소 권한
    created_at  DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_board_code UNIQUE (code)
);

CREATE TABLE post (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    board_id   BIGINT       NOT NULL,
    author_id  BIGINT       NOT NULL,
    title      VARCHAR(200) NOT NULL,
    content    MEDIUMTEXT   NOT NULL,                        -- 긴 한글 본문 (tinytext 함정 회피)
    view_count INT          NOT NULL DEFAULT 0,
    like_count INT          NOT NULL DEFAULT 0,
    status     VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED',    -- PUBLISHED/HIDDEN/DELETED (소프트딜리트)
    is_pinned  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_post_board  FOREIGN KEY (board_id)  REFERENCES board(id),
    CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES member(id)
);

CREATE INDEX idx_post_board_created ON post (board_id, created_at);
