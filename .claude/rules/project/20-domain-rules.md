---
description: 회원·등급(별명)·관리자 도메인 규칙
globs: ["**/member/**", "**/grade/**", "**/admin/**", "**/board/**"]
alwaysApply: true
---

# 20 · 도메인 규칙 (회원 / 등급 / 관리자 / 게시판)

## 권한 모델 (ADMIN 단일)
- 역할: `GUEST`(비로그인) / `MEMBER`(로그인 팬) / `ADMIN`(운영자).
- SUPER_ADMIN 등 추가 분리 **하지 않는다**.
- 모든 관리자 기능은 서버에서 `role == ADMIN` 재검증. 프론트 노출 제어만으로 보호 금지.

## 회원 등급(별명) 시스템 — 관리자 정의
요구사항: **관리자가 팬에게 줄 칭호(별명)를 직접 만들고, 그것이 곧 등급으로 동작**한다.

- 등급(`grade`)은 **관리자가 CRUD**: 등급명(칭호/별명), 정렬 순서(=서열), 뱃지 색, 기본 등급 여부.
  - 예: "견습 선원" → "갑판원" → "항해사" → "갑판장" 같은 서열형 칭호. 이름·개수·순서 전부 관리자가 자유 지정.
- 회원에는 `grade_id` 배정. 관리자가 회원별로 등급을 **수동 배정/변경**.
- 신규 회원은 `is_default = true` 등급으로 자동 시작.
- 표시: 닉네임 옆에 등급 칭호/뱃지 노출(게시글·댓글·프로필).
- (선택·확장) 회원별 **커스텀 칭호 오버라이드** 한 줄을 허용할 수 있음(등급과 별개의 1인 전용 별명). 1차 범위에선 등급 기반으로 충분.
- (선택·확장) 출석 streak·씨미 구독 티어 기반 **자동 등급 규칙**은 후순위. 1차는 수동 배정만.

### 규칙
- 등급은 **데이터**다. 코드에 등급명·서열을 하드코딩 금지(전부 `grade` 테이블에서).
- 등급 삭제 시, 그 등급을 쓰던 회원은 기본 등급으로 안전하게 이전(또는 삭제 차단). 고아 FK 금지.
- 닉네임/칭호에 대한 입력 검증(길이·금칙어·중복 정책)을 둔다.

## 게시판 (동적)
- 게시판은 고정 아님. 관리자가 추가/삭제/이름변경/순서/노출/타입(일반·갤러리)을 관리.
- 게시판명·구성은 코드 하드코딩 금지. `board` 테이블 기반 동적 렌더.
- 글/댓글 상태는 소프트 딜리트(`PUBLISHED/HIDDEN/DELETED`). 물리 삭제 지양.

## 데이터 모델 (논리 — 구현 시 확정)
| 테이블 | 핵심 컬럼 | 비고 |
|---|---|---|
| `member` | id, seeme_user_id, nickname, grade_id(FK), role, status, created_at | seeme_user_id 유니크 |
| `grade` | id, name, sort_order, badge_color, is_default, created_at | 관리자 CRUD |
| `board` | id, code, name_kr, name_en, icon, description, sort_order, type, is_visible, write_role | type: GENERAL/GALLERY |
| `post` | id, board_id, author_id, title, content, view_count, like_count, status, is_pinned, created_at | 소프트 딜리트 |
| `post_image` | id, post_id, url, thumb_url, sort_order | 갤러리·첨부 |
| `comment` | id, post_id, author_id, parent_id, content, status, created_at | 대댓글 parent_id |
| `post_like` | member_id, post_id, created_at | (회원,글) 유니크 |
| `attendance` | id, member_id, attend_date, created_at | (회원,날짜) 유니크 · 서버 저장 |
| `live_status` | id, is_live, title, stream_url, override_mode, updated_at | 캐시 + 수동 오버라이드 |
| `report` | id, target_type, target_id, reporter_id, reason, status, handled_by, created_at | POST/COMMENT |
| `site_setting` | key, value | 테마·카피·링크·씨미 channelId |
| `admin_log` | id, admin_id, action, target, detail, created_at | 운영 감사 로그 |
