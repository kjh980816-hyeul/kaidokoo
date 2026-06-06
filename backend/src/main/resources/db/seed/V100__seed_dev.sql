-- V100: 로컬 개발 전용 시드 (local 프로파일에서만 로드. 운영엔 미적용)
-- 게시판/등급은 데이터로 주입(코드 하드코딩 금지 규칙 준수). 인증(P2) 전이라 개발용 회원도 시드.

-- 등급(별명/칭호). 견습 선원 = 기본 등급(신규 회원 자동 배정 대상).
INSERT INTO grade (name, sort_order, badge_color, is_default, created_at) VALUES
 ('견습 선원', 1, '#8896B0', TRUE,  CURRENT_TIMESTAMP),
 ('갑판원',   2, '#5BC8AF', FALSE, CURRENT_TIMESTAMP),
 ('항해사',   3, '#D4AF6A', FALSE, CURRENT_TIMESTAMP);

-- 개발용 일반 회원(기본 등급) + 관리자(항해사). X-Member-Id 헤더로 이 id를 사용해 임시 로그인.
INSERT INTO member (provider, provider_user_id, nickname, role, status, grade_id, created_at)
SELECT 'GOOGLE', 'dev-001', '개발선원', 'MEMBER', 'ACTIVE', g.id, CURRENT_TIMESTAMP
FROM grade g WHERE g.name = '견습 선원';

INSERT INTO member (provider, provider_user_id, nickname, role, status, grade_id, created_at)
SELECT 'GOOGLE', 'dev-admin', '선장', 'ADMIN', 'ACTIVE', g.id, CURRENT_TIMESTAMP
FROM grade g WHERE g.name = '항해사';

INSERT INTO board (code, name_kr, name_en, description, sort_order, type, is_visible, write_role, created_at) VALUES
 ('notice', '항해일지', 'Captain''s Log', '선장의 공지', 1, 'GENERAL', TRUE, 'ADMIN',  CURRENT_TIMESTAMP),
 ('free',   '정박지',   'Harbor',         '선원들의 자유 게시판', 2, 'GENERAL', TRUE, 'MEMBER', CURRENT_TIMESTAMP),
 ('fanart', '별빛 갤러리', 'Starlight Gallery', '팬아트 갤러리', 3, 'GALLERY', TRUE, 'MEMBER', CURRENT_TIMESTAMP);

INSERT INTO post (board_id, author_id, title, content, status, created_at, updated_at)
SELECT b.id, m.id,
       '안개 너머, 별을 좇는 유령선에 오신 것을 환영합니다',
       '여기는 유령선장 카이도쿠의 정박지입니다. 출항 준비가 되면 함께 별바다를 항해해요.',
       'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM board b, member m
WHERE b.code = 'notice' AND m.provider_user_id = 'dev-001';
