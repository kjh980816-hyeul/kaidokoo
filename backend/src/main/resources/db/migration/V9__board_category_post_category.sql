-- V9: 게시판 말머리(카테고리) + 글 말머리 선택.
-- board.categories = 쉼표로 구분된 말머리 라벨 목록(예: "공지,자랑,질문"). 관리자가 게시판별로 정의.
-- post.category = 글 작성 시 선택한 말머리 1개(없으면 NULL). 게시판의 categories 중 하나여야 한다(서버 검증).
ALTER TABLE board ADD COLUMN categories VARCHAR(500) NULL;
ALTER TABLE post  ADD COLUMN category   VARCHAR(50)  NULL;
