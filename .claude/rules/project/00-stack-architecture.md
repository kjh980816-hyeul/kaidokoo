---
description: 카이도쿠 팬카페 아키텍처·레이어·코딩 컨벤션 (프로젝트 전용)
globs: ["**/*"]
alwaysApply: true
---

# 00 · 아키텍처 & 스택 규칙

## 레이어 (백엔드)
- `controller` → `service` → `repository` 단방향. controller에 비즈니스 로직 금지.
- 엔티티(Entity)와 DTO를 분리한다. API 응답에 엔티티를 직접 노출하지 않는다(Request/Response DTO 사용).
- 외부 연동(씨미)은 `infra/seeme/` 어댑터로 격리. 도메인 서비스가 씨미 SDK/HTTP를 직접 호출하지 않는다.
- 트랜잭션 경계는 service 메서드. `@Transactional(readOnly=true)`를 조회 기본값으로.

## 프론트
- Vue 3 Composition API + `<script setup lang="ts">`. 타입 명시(`any` 지양).
- API 호출은 `api/` 레이어로 모으고 컴포넌트에서 직접 fetch 금지.
- 상태는 필요한 범위에서만 전역화. 라이브 상태·로그인 상태 정도만 전역 스토어.
- 기존 시안의 캔버스 별하늘·유성·로딩 엠블럼 모션은 그대로 이식하되, `prefers-reduced-motion` 분기 유지.

## 공통 컨벤션
- 네이밍: 백엔드 `camelCase`(메서드/변수), `PascalCase`(클래스). 프론트 컴포넌트 `PascalCase`, 파일 kebab/Pascal 일관 유지.
- 매직 넘버·하드코딩 금지. 게시판 목록, 등급, 라이브 채널 ID 등은 데이터/설정에서 읽는다(코드 박제 금지).
- 에러는 도메인 예외 → 전역 핸들러에서 표준 에러 응답으로 매핑. 클라이언트에 스택트레이스·내부 메시지 노출 금지.
- 시간은 서버 UTC 저장, 표시 시 KST 변환.

## 환경 분리
- `local / dev / prod` 프로파일 분리. 시크릿은 환경변수/시크릿 매니저. 저장소에 `.env`·키 커밋 금지(`.gitignore`에 명시).
