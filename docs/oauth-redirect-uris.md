# OAuth 앱 등록 가이드 — 콘솔별 입력값 전체 (kaii.co.kr)

> 네이버/카카오/구글 개발자 콘솔에서 **폼에 입력할 값을 그대로 옮겨 적을 수 있게** 정리.
> 콜백은 **백엔드(Spring)가 `/api/auth/{provider}/callback`에서 수신** (ADR-0001).
> 개발은 Vite(5173)가 `/api`를 8080으로 프록시 → localhost는 5173 기준.

---

## 빠른 참조 (URL 모음)

| 용도 | 값 |
|---|---|
| 서비스 URL / 사이트 도메인 (운영) | `https://kaii.co.kr` |
| 사이트 도메인 (개발) | `http://localhost:5173` |
| 네이버 콜백 | `https://kaii.co.kr/api/auth/naver/callback`<br>`http://localhost:5173/api/auth/naver/callback` |
| 카카오 콜백 | `https://kaii.co.kr/api/auth/kakao/callback`<br>`http://localhost:5173/api/auth/kakao/callback` |
| 구글 콜백 | `https://kaii.co.kr/api/auth/google/callback`<br>`http://localhost:5173/api/auth/google/callback` |

⚠️ 셋 다 Redirect URI는 **완전 일치(exact match)**. 끝 슬래시·http/https 다르면 실패.
⚠️ `www.kaii.co.kr` 쓸 거면 변형도 전부 추가 등록 필요 → 권장: nginx에서 www → kaii.co.kr 301 통일하고 위 값만 등록.

---

## 1. 네이버 — developers.naver.com → Application → 애플리케이션 등록

| 폼 항목 | 입력값 |
|---|---|
| 애플리케이션 이름 | `카이도쿠 팬카페` (로그인 동의창에 노출되는 이름) |
| 사용 API | **네이버 로그인** |
| 제공 정보 선택 (스코프) | **별명(닉네임)** + **프로필 사진** 만 체크 (이메일 등 나머지 해제 — 최소 수집) |
| 로그인 오픈 API 서비스 환경 | **PC 웹** |
| 서비스 URL | `https://kaii.co.kr` (1개만 입력 가능) |
| 네이버 로그인 Callback URL | `https://kaii.co.kr/api/auth/naver/callback` ⏎ `http://localhost:5173/api/auth/naver/callback` (최대 5개, 줄바꿈으로 둘 다) |

- 발급물: **Client ID + Client Secret**
- ⚠️ 검수: 등록 직후엔 "개발 중" 상태 = **등록된 테스터 계정만 로그인 가능**. 멤버관리 탭에서 테스트 계정(준하님 네이버 계정) 추가하고 개발하다가, 오픈 전에 **"네이버 로그인 검수 요청"** 제출(동의창 캡처 필요 — 이미 동의한 계정은 nid.naver.com → 내정보 → 연결된 서비스에서 연결 해제해야 동의창 다시 뜸).

## 2. 카카오 — developers.kakao.com → 내 애플리케이션 → 애플리케이션 추가

### 2-1. 앱 만들기
| 폼 항목 | 입력값 |
|---|---|
| 앱 이름 | `카이도쿠 팬카페` |
| 회사명 | 개인이면 본인 이름 또는 `카이도쿠 팬카페` |
| 카테고리 | 커뮤니티/소셜 계열 아무거나 |

### 2-2. 플랫폼 등록 (앱 설정 > 플랫폼 > Web)
| 폼 항목 | 입력값 |
|---|---|
| 사이트 도메인 | `https://kaii.co.kr` ⏎ `http://localhost:5173` (둘 다 등록) |

### 2-3. 카카오 로그인 (제품 설정 > 카카오 로그인)
| 폼 항목 | 입력값 |
|---|---|
| 활성화 설정 | **ON** |
| Redirect URI | `https://kaii.co.kr/api/auth/kakao/callback` ⏎ `http://localhost:5173/api/auth/kakao/callback` (최대 10개) |

### 2-4. 동의항목 (제품 설정 > 카카오 로그인 > 동의항목)
| 항목 | 설정 |
|---|---|
| 닉네임 | 필수 동의 |
| 프로필 사진 | 필수 동의 |
| 이메일 등 나머지 | 사용 안 함 (이메일은 검수 필요 → 1차 제외) |

### 2-5. 보안 (제품 설정 > 카카오 로그인 > 보안)
| 항목 | 설정 |
|---|---|
| Client Secret | **발급** 후 활성화 상태 **"사용함"** |

- 발급물: **REST API 키**(앱 설정 > 앱 키 — 이게 client_id 역할) + **Client Secret**
- 검수: 닉네임/프사만 쓰면 **별도 검수 없이 바로 운영 가능** ✅

## 3. 구글 — console.cloud.google.com

### 3-1. 프로젝트 생성
| 폼 항목 | 입력값 |
|---|---|
| 프로젝트 이름 | `kaidoku-fancafe` |

### 3-2. OAuth 동의 화면 (APIs & Services > OAuth consent screen)
| 폼 항목 | 입력값 |
|---|---|
| User Type | **External(외부)** |
| 앱 이름 | `카이도쿠 팬카페` |
| 사용자 지원 이메일 | kjh980816@gmail.com |
| 앱 로고 | **넣지 말 것** (로고 업로드하면 구글 검수 트리거됨) |
| 애플리케이션 홈페이지 | `https://kaii.co.kr` |
| 개인정보처리방침 / 서비스 약관 | 비워둠 (선택 — 나중에 페이지 생기면 추가) |
| 승인된 도메인 (Authorized domains) | `kaii.co.kr` |
| 개발자 연락처 이메일 | kjh980816@gmail.com |
| 스코프 | `openid`, `.../auth/userinfo.profile` 만 (email 제외) |
| 게시 상태 | 테스트로 만들고 → **"앱 게시(프로덕션 전환)"** — 민감 스코프 없으면 검수 없이 즉시 전환됨 ✅ |

### 3-3. 사용자 인증 정보 (Credentials > 사용자 인증 정보 만들기 > OAuth 클라이언트 ID)
| 폼 항목 | 입력값 |
|---|---|
| 애플리케이션 유형 | **웹 애플리케이션** |
| 이름 | `kaidoku-web` |
| 승인된 자바스크립트 원본 | `https://kaii.co.kr` ⏎ `http://localhost:5173` |
| 승인된 리디렉션 URI | `https://kaii.co.kr/api/auth/google/callback` ⏎ `http://localhost:5173/api/auth/google/callback` |

- 발급물: **Client ID + Client Secret**
- ⚠️ 구글은 https 필수 (localhost만 http 허용)

---

## 발급받은 키 → 서버 환경변수 (HARD, 30-constraints-security.md)

```
OAUTH_NAVER_CLIENT_ID=
OAUTH_NAVER_CLIENT_SECRET=
OAUTH_KAKAO_CLIENT_ID=        # = REST API 키
OAUTH_KAKAO_CLIENT_SECRET=
OAUTH_GOOGLE_CLIENT_ID=
OAUTH_GOOGLE_CLIENT_SECRET=
```

- 프론트 번들·git·로그·문서에 평문 금지. `.env*`는 `.gitignore`.
- 콜백 처리 시 `state` 파라미터 CSRF 검증 필수.

## 참고: 콜백 흐름 (구현 시)

```
프론트 "네이버로 로그인" 클릭
→ GET /api/auth/naver/login  (백엔드가 state 생성·저장 후 네이버 인증 페이지로 302)
→ 사용자 동의
→ 네이버가 /api/auth/naver/callback?code=...&state=... 호출
→ 백엔드: state 검증 → code로 토큰 교환 → 프로필 조회 → member 조회/생성 → 자체 세션 발급
→ 프론트로 302 (홈)
```
