-- V100: 로컬 개발 전용 시드 (local 프로파일에서만 로드. 운영엔 미적용)
-- 게시판은 데이터로 주입 (코드 하드코딩 금지 규칙 준수). 인증(P2) 전이라 개발용 회원 1명 시드.

INSERT INTO member (provider, provider_user_id, nickname, role, status, created_at)
VALUES ('GOOGLE', 'dev-001', '개발선원', 'MEMBER', 'ACTIVE', CURRENT_TIMESTAMP);

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
