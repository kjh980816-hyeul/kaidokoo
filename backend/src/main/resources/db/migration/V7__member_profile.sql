-- V7: 회원 프로필 — 아바타(프로필 사진) URL.
-- 닉네임은 V1(member.nickname)에 이미 존재. 여기선 프사 컬럼만 추가한다.
ALTER TABLE member ADD COLUMN avatar_url VARCHAR(300) NULL;
