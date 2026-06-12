# ADR-0004 · 인증: 자체 세션(DB) + HttpOnly 쿠키, 최초 가입자 ADMIN 부트스트랩

- 상태: 확정 (2026-06-13)
- 선행: ADR-0001(소셜 3종), ADR-0003(임시 X-Member-Id seam — 본 ADR로 종료)

## 결정
1. **소셜 로그인은 서버사이드 Authorization Code Flow.** 콜백 `/api/auth/{provider}/callback`은 백엔드가 받는다(dev는 Vite 5173 프록시 경유). state는 HttpOnly 쿠키(10분) + 콜백 파라미터 비교로 검증(CSRF).
2. **자체 세션 = DB 테이블(member_session) + HttpOnly 쿠키(KAIDOKU_SESSION).** 토큰은 SecureRandom 32바이트, 수명 30일. JWT 대신 DB 세션을 쓴다 — 즉시 폐기(로그아웃/제재) 가능, HttpOnly라 XSS 토큰 탈취 면역, 회원 규모상 조회 비용 무시 가능.
3. **CurrentMemberArgumentResolver만 교체**(ADR-0003 설계 의도대로). 신원 출처가 헤더→세션 쿠키로 바뀌었고, 권한·상태의 DB 재검증은 유지.
4. **최초 가입자 = ADMIN 부트스트랩.** member가 0명일 때 가입한 회원은 ADMIN. 공개 전 준하님이 먼저 로그인하면 되고, 이후 권한은 관리자 화면에서 배정. (로컬 시드의 dev 회원 2명은 제거 — 첫 로그인이 곧 관리자)
5. **신규 회원은 기본 등급(is_default) 자동 배정.** 닉네임은 가입 시 1회만 제공자 값으로 저장(재로그인 시 덮어쓰지 않음 — OAuth sync 가드 패턴).
6. **제공자 토큰은 저장하지 않는다.** 프로필 1회 조회에만 사용(개인정보·시크릿 최소).
7. **카카오는 후속.** 어댑터(OAuthClient) 구조는 동일하고 키만 비어 있음 — 키 주입 시 활성화.

## 시크릿 운반
- 로컬: `backend/secrets/local.yml` (gitignored, `spring.config.import: optional`)
- 운영: 환경변수 `OAUTH_*`, `SEEME_*`, `APP_BASE_URL=https://kaii.co.kr`, `COOKIE_SECURE=true`

## 함께 구현된 것 (10-seeme)
- 씨미 라이브 자동 폴링: `infra/seeme/SeemeClient`(Client-Id/Secret 서버 전용) + `SeemeLivePoller`(@Scheduled 60초). AUTO 모드 + 채널 ID(site_setting) 설정 시에만 동작, 실패 시 이전 캐시 유지. 관리자 화면에서 모드(AUTO/FORCE_ON/FORCE_OFF)·채널 ID 관리.
- ⚠️ 씨미 API는 베타 — base-url(`SEEME_BASE_URL`, 기본 https://ci.me)·응답 형태는 실호출로 확인 필요. 응답은 content 래핑 유무 모두 대응.
