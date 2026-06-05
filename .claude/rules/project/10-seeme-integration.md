---
description: 씨미(CIME) OpenAPI 연동 규칙 — OAuth 로그인, 라이브 상태, 스코프, 보안
globs: ["**/infra/seeme/**", "**/auth/**", "**/live/**"]
alwaysApply: true
---

# 10 · 씨미(CIME) OpenAPI 연동 규칙

> 출처: developers.ci.me 공식 문서 기준. 엔드포인트는 베타이며 변경될 수 있으니, 구현 전 현재 문서를 재확인할 것(search-first).

## 인증 방식 (2종)
| 방식 | 헤더 | 용도 |
|---|---|---|
| Client ID/Secret | `Client-Id`, `Client-Secret` | 공개 정보(채널·라이브 목록) — **서버에서만** |
| Access Token | `Authorization: Bearer {token}` | 사용자 권한 API(예: users/me) |

## 로그인 = OAuth 2.0 Authorization Code Flow
씨미는 **신원 제공자(IdP)** 로만 쓴다. 로그인 성공 후 우리 서비스가 자체 세션을 발급한다.

1. 인증 페이지로 리디렉트:
   `https://ci.me/auth/openapi/account-interlock?clientId={CLIENT_ID}&redirectUri={REDIRECT_URI}&state={STATE}`
2. 사용자 승인 → `redirectUri?code={CODE}&state={STATE}` 로 콜백.
3. 토큰 교환: `POST /api/openapi/auth/v1/token`
   ```json
   { "grantType":"authorization_code", "clientId":"...", "clientSecret":"...", "code":"..." }
   ```
4. 사용자 식별: `GET /api/openapi/open/v1/users/me` (Bearer, scope `READ:USER`)
   → 받은 씨미 user 식별자로 우리 `member` 조회/생성 → **우리 세션 토큰 발급**.
5. 갱신: `POST /api/openapi/auth/v1/token` (`grantType:"refresh_token"`)
6. 취소: `POST /api/openapi/auth/v1/token/revoke`

### 토큰 수명 (씨미)
- Authorization Code: **10분** (즉시 교환)
- Access Token: **1시간(3600초)**
- Refresh Token: **무제한** (취소 전까지)

### 규칙
- `state`는 CSRF 방지용 난수. 콜백에서 **반드시 검증**, 불일치 시 거부.
- 로그인에 필요한 스코프는 **`READ:USER` 최소만** 요청. (구독 기반 자동 등급 같은 확장 전엔 `READ:CHANNEL`/`READ:SUBSCRIPTION` 요청 금지.)
- 씨미 Access/Refresh 토큰은 **우리 세션 발급에만 사용**. 팬 단위로 씨미를 계속 호출할 필요 없으면 refresh 토큰은 저장하지 않거나 암호화 저장 후 최소 보관.
- 우리 세션 토큰(자체 발급)과 씨미 토큰을 혼동하지 말 것. 프론트엔 우리 세션만 노출.

## 라이브 배너 = 공개 라이브 상태 API (인증 불필요)
`GET /api/openapi/v1/{channelId}/live-status` → `{ isLive, title, openedAt }`

- 서버가 카이도쿠 채널 ID로 **주기 폴링(예: 30~60초)** 하고 결과를 캐시. 프론트는 우리 서버의 `/api/live-status`만 호출(CORS·키 노출 회피).
- 채널 ID는 **사이트 설정(`site_setting`)** 에 저장하고 관리자가 입력. 코드 하드코딩 금지.
- 관리자 **수동 오버라이드** 가능: 강제 ON/OFF·제목·링크. 오버라이드가 있으면 폴링 결과보다 우선.

## 공개 조회 API (필요 시, Client ID/Secret · 서버)
- 채널 정보: `GET /api/openapi/open/v1/channels?channelIds={id}`
- 라이브 목록: `GET /api/openapi/open/v1/lives` (커서 페이지네이션)

## 보안 (HARD)
- `Client-Secret`은 **서버 전용**. 프론트·저장소·로그·git 노출 금지(환경변수/시크릿 매니저).
- 씨미 호출 실패·만료에 대한 폴백(배너 OFF 표시 등) 정의. 외부 의존 장애가 사이트 전체를 막지 않게.
