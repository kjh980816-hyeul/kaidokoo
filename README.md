# 유령선장 카이도쿠 공식 팬카페

씨미(CIME) 스트리머 **유령선장 카이도쿠**의 공식 팬카페. 천문 항해 × 유령선, 골드 × 미드나잇 네이비.
풀스택 웹(공개 사이트 + 단일 ADMIN 운영 화면).

> 가드레일은 [`CLAUDE.md`](./CLAUDE.md), 상세 룰은 [`.claude/rules/`](./.claude/rules/), 설계 결정은 [`docs/adr/`](./docs/adr/).

## 스택
- **backend/** — Spring Boot 3.5 · Java 21 · JPA · Flyway(`ddl-auto=validate`) · MySQL 8(운영) / H2(로컬·테스트)
- **frontend/** — Vue 3 `<script setup ts>` · Vite · vue-router · `api/` 레이어

## 현재 진행 (2026-06-05)
**P1 — 풀스택 수직 슬라이스 완료.** 게시판 1개를 목록·상세·작성까지 backend↔frontend 관통, DB 저장. 테스트 4종 통과, end-to-end(vite 프록시 → 8080) 검증 완료.
- API: `GET /api/boards`, `GET /api/boards/{code}/posts`, `GET /api/posts/{id}`, `POST /api/posts`
- ⚠️ 로그인(P2) 전이라 글 작성은 임시 작성자(시드 회원)로 저장. `PostCreateRequest.authorId`는 인증 도입 후 제거 예정.

## 로컬 실행
```bash
# 1) 백엔드 (H2, 설치 0. 첫 실행 시 Gradle·JDK21 자동 다운로드)
cd backend && ./gradlew bootRun        # → http://localhost:8080

# 2) 프론트엔드 (다른 터미널)
cd frontend && npm install && npm run dev   # → http://localhost:5173 (/api 는 8080으로 프록시)
```
테스트: `cd backend && ./gradlew test` · 프론트 빌드/타입체크: `cd frontend && npm run build`

## 다음 (로드맵)
- P2: 소셜 로그인(네이버/카카오/구글, [ADR-0001](./docs/adr/ADR-0001-social-login.md)) + 세션 + 회원/등급 + 댓글·좋아요
- P3: 라이브 배너(씨미) + 출석 + 갤러리형 게시판
- P4: 관리자 화면(게시판/게시물/댓글/신고/회원/라이브/설정)
- P5~6: 검색·신고·제재·마이페이지 / 성능·접근성·SEO·배포
