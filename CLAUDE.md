# CLAUDE.md — 유령선장 카이도쿠 공식 팬카페

> 이 파일은 프로젝트 루트의 최상위 가드레일이다. ECC(everything-claude-code)의 공통/스택 룰 위에 얹히는 **프로젝트 전용 규칙**이며, 충돌 시 이 문서와 `.claude/rules/project/`가 ECC 일반 룰보다 우선한다.

---

## 1. 프로젝트 한 줄 정의
씨미(CIME) 스트리머 **유령선장 카이도쿠**의 공식 팬카페. 천문 항해 × 유령선 컨셉, 골드 × 미드나잇 네이비. 공개 사이트 + 단일 관리자(ADMIN) 운영 화면을 갖춘 풀스택 웹 서비스.

## 2. 기술 스택 (확정)
- 백엔드: Spring Boot 3.x · Java 21 · JPA(+ 필요 시 MyBatis) · MySQL 8 · REST API
- 프론트: Vue 3 (Composition API) · TypeScript · Vite
- 인프라: Nginx + TLS
- 외부 연동: 씨미 OpenAPI (OAuth 2.0 로그인 + 공개 라이브 상태)

## 3. 확정된 핵심 결정 (변경 시 ADR 기록)
1. **로그인 = 네이버 + 카카오 + 구글 소셜 3종 OAuth 2.0.** (2026-06-05 A안 확정 — 핸드오프 원안 "씨미 OAuth 단독"을 변경. 상세·근거: `docs/adr/ADR-0001-social-login.md`) 소셜 인증 후 우리 서비스가 자체 세션을 발급한다. 자체 회원가입 폼 없음. `member`는 `provider`(NAVER/KAKAO/GOOGLE) + `provider_user_id`로 식별.
2. **씨미 = 라이브 배너 전용.** 공개 라이브 상태 API를 서버가 폴링·캐시(키 필요 시 관리자 수동 토글 폴백). 로그인에서는 씨미 제외. 관리자는 채널 ID와 수동 오버라이드 설정. (상세: `rules/project/10-seeme-integration.md`)
3. **관리자 권한은 ADMIN 단일.** SUPER_ADMIN 분리하지 않는다. 권한 등급은 `GUEST / MEMBER / ADMIN` 3단계.
4. **회원 등급(별명) 시스템.** 관리자가 등급(별명/칭호)을 직접 정의·이름변경·순서지정하고 회원에게 배정한다. 등급은 닉네임 옆 칭호/뱃지로 표시된다. (상세: `rules/project/20-domain-rules.md`)
5. **개발 방식 = Claude Code + ECC, EDD/TDD 우선.** 구현 전 리서치(search-first), 테스트/eval 먼저, 그다음 구현. (상세: `ECC_세팅_가이드.md`)

## 4. 절대 제약 (HARD CONSTRAINTS)
- **씨미 Client Secret·토큰은 서버 사이드 전용.** 프론트 번들·저장소·로그·git에 절대 노출 금지. (상세: `rules/project/30-constraints-security.md`)
- **DB 스키마는 마이그레이션으로만 변경.** 운영 DB에 손으로 컬럼 추가 금지. JPA `ddl-auto`는 `validate` 고정.
- **모든 권한 검증은 서버에서.** 프론트의 화면 숨김은 보안이 아니다. 관리자 API는 서버에서 role 재검증.
- **회원 개인정보 최소 수집.** 씨미에서 받은 식별자·닉네임·프로필 외 불필요한 데이터 저장 금지.
- 시크릿·키·자격증명을 응답·코드·문서에 출력하지 않는다.

## 5. 작업 워크플로
- **결정 분류**: A(트레이드오프 있는 설계 선택 → 사람이 결정) / B(형식·네이밍 → 에이전트 자율) / C(외부 제약, 협상 불가 → 그대로 따름). A는 임의로 결정하지 말고 질문한다.
- **변경 단위**: 한 번에 하나의 기능/모듈. 큰 변경은 `/plan`으로 쪼갠 뒤 진행.
- **검증 루프**: 구현 → 테스트/eval 통과 → 셀프 리뷰(code-reviewer) → 커밋. 테스트 없이 "완성" 선언 금지.
- **기록**: 비자명한 설계 결정은 ADR(`docs/adr/`)로 박제. 막힌 지점·실패 사례도 남긴다.

## 6. 디렉터리 (요약)
```
backend/   Spring Boot (Java)
frontend/  Vue 3 + TS
.claude/
  rules/
    ecc/        # ECC 공통+스택 룰 (수동 설치, git 제외 가능)
    project/    # 이 프로젝트 전용 룰 (아래 파일들)
    java/       # ECC에 없는 Java 룰 (직접 관리)
docs/adr/  설계 결정 기록
```

## 7. 더 읽을 것
- `.claude/rules/project/00-stack-architecture.md` — 아키텍처·레이어·컨벤션
- `.claude/rules/project/10-seeme-integration.md` — 씨미 API 연동 규칙(엔드포인트·스코프·보안)
- `.claude/rules/project/20-domain-rules.md` — 회원/등급/관리자 도메인 규칙
- `.claude/rules/project/30-constraints-security.md` — 보안·제약사항
- `.claude/rules/java/patterns.md` — Java/Spring 스택 룰(ECC 미제공 보완)
- `ECC_세팅_가이드.md` — ECC 설치·EDD 루프·활용 컴포넌트
