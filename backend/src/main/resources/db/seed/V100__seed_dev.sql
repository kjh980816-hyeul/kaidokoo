-- V100: 로컬 개발 전용 시드 (local 프로파일에서만 로드. 운영엔 미적용)
-- 게시판/등급은 데이터로 주입(코드 하드코딩 금지 규칙 준수).
-- 회원 시드는 제거(소셜 로그인 도입, ADR-0004) — 최초 로그인 회원이 ADMIN으로 부트스트랩된다.

-- 등급(별명/칭호). 견습 선원 = 기본 등급(신규 회원 자동 배정 대상).
INSERT INTO grade (name, sort_order, badge_color, is_default, created_at) VALUES
 ('견습 선원', 1, '#8896B0', TRUE,  CURRENT_TIMESTAMP),
 ('갑판원',   2, '#5BC8AF', FALSE, CURRENT_TIMESTAMP),
 ('항해사',   3, '#D4AF6A', FALSE, CURRENT_TIMESTAMP);

INSERT INTO board (code, name_kr, name_en, description, sort_order, type, is_visible, write_role, created_at) VALUES
 ('notice', '항해일지', 'Captain''s Log', '선장의 공지', 1, 'GENERAL', TRUE, 'ADMIN',  CURRENT_TIMESTAMP),
 ('free',   '정박지',   'Harbor',         '선원들의 자유 게시판', 2, 'GENERAL', TRUE, 'MEMBER', CURRENT_TIMESTAMP),
 ('fanart', '별빛 갤러리', 'Starlight Gallery', '팬아트 갤러리', 3, 'GALLERY', TRUE, 'MEMBER', CURRENT_TIMESTAMP);
