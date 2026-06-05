# ECC 세팅 & 개발 워크플로 가이드 — 카이도쿠 팬카페

이 프로젝트는 **ECC(everything-claude-code)** 를 Claude Code에 얹어 개발한다. ECC는 agents·skills·commands·hooks·rules·mcp-configs로 구성된 설정 모음이고, 여기에 이 저장소의 **프로젝트 전용 룰**(`CLAUDE.md` + `.claude/rules/project/` + `.claude/rules/java/`)을 더해 쓴다.

---

## 1. ECC 설치

### (1) 플러그인 — commands/agents/skills/hooks
Claude Code 안에서:
```
/plugin marketplace add https://github.com/affaan-m/ECC
/plugin install ecc@ecc
```

### (2) 룰 — 수동 설치 (플러그인이 룰은 자동 배포 못 함)
이 프로젝트는 **프론트=TypeScript/web**, **백엔드=Java**다. Java 룰은 ECC에 없으니 공통+ts+web만 가져오고 Java는 이 저장소 룰로 채운다.
```
git clone https://github.com/affaan-m/ECC.git
cd ECC
# 프로젝트 레벨로 설치 (이 저장소 루트에서)
mkdir -p <repo>/.claude/rules/ecc
cp -r rules/common      <repo>/.claude/rules/ecc/
cp -r rules/typescript  <repo>/.claude/rules/ecc/
cp -r rules/web         <repo>/.claude/rules/ecc/
```
> `configure-ecc` 스킬을 쓰면 스택 선택 마법사로 위 과정을 대신할 수 있다. 단, Java는 목록에 없으므로 백엔드는 `rules/java/patterns.md`(이 저장소)가 담당한다.

### (3) 적용 우선순위
`rules/ecc/common` (일반) < `rules/ecc/{typescript,web}` (스택) < **`rules/project/*` + `rules/java/*` + `CLAUDE.md`** (프로젝트, 최우선). 충돌 시 구체적·프로젝트 룰이 이긴다.

---

## 2. 디렉터리 배치
```
<repo>/
├── CLAUDE.md                      # 최상위 가드레일
├── .claude/
│   └── rules/
│       ├── ecc/                   # ECC 공통+ts+web (수동 설치, git 제외 가능)
│       ├── project/               # 이 프로젝트 룰 (이미 포함)
│       │   ├── 00-stack-architecture.md
│       │   ├── 10-seeme-integration.md
│       │   ├── 20-domain-rules.md
│       │   └── 30-constraints-security.md
│       └── java/
│           └── patterns.md        # Java 스택 룰 (ECC 미제공 보완)
├── docs/adr/                      # 설계 결정 기록
├── backend/                       # Spring Boot
└── frontend/                      # Vue 3 + TS
```
> `.claude/rules/ecc/`는 업스트림 복사본이라 `.gitignore`에 넣고 설치 스크립트로 재현해도 된다. `project/`·`java/`·`CLAUDE.md`는 반드시 버전관리.

---

## 3. EDD / TDD 개발 루프
ECC의 핵심은 **리서치 우선 → 테스트/eval 먼저 → 구현 → 리뷰**다. 이 프로젝트도 그대로 따른다.

1. **리서치(search-first)**: 새 기능 전 기존 코드·라이브러리·씨미 문서부터 확인. 특히 씨미 엔드포인트는 베타라 구현 직전 재확인.
2. **계획(`/plan`)**: 기능을 작은 단위로 쪼갠다. 결정 분류 A(트레이드오프)는 사람에게 질문.
3. **테스트/eval 먼저(`/tdd`)**: 기대 동작을 테스트로 먼저 정의. 외부(씨미)는 모킹.
4. **구현**: 테스트를 통과시키는 최소 구현. 룰 위반 금지.
5. **리뷰(code-reviewer / security-review)**: 셀프 리뷰 후 커밋. 테스트 없이 "완성" 금지.
6. **박제**: 비자명한 결정은 `docs/adr/`에. 막힌 지점·실패도 기록.

활용하면 좋은 ECC 컴포넌트: `planner`(설계 분해), `code-reviewer`(리뷰), `tdd-workflow`/`/tdd`(테스트 우선), `search-first`(리서치), `security-review`(보안 점검), `configure-ecc`(룰 설치), `skill-creator`(반복 작업을 스킬로).

---

## 4. 첫 스프린트 제안 (EDD로)
1. 프로젝트 스캐폴드(backend/frontend) + `CLAUDE.md`·룰 배치 + CI 테스트 골격.
2. 씨미 OAuth 로그인 → 자체 세션 발급 (모킹 테스트 → 실연동).
3. 회원 + 등급(별명) 도메인 + 관리자 등급 CRUD/배정.
4. 게시판 동적 CRUD(목록/상세/작성/댓글).
5. 라이브 배너(공개 라이브 상태 폴링+캐시, 관리자 오버라이드).
6. 출석(서버 저장) + 디자인 시안 이식(로딩·별하늘).
7. 관리자 화면 전반 + 신고/제재/로그.

---

## 5. 직접 만들면 좋은 프로젝트 스킬
ECC `skill-creator`로 이 프로젝트 반복 작업을 스킬화하면 효율이 오른다. 예:
- **seeme-endpoint-check**: 씨미 엔드포인트를 구현 전 공식 문서로 재확인하는 절차.
- **board-feature**: 게시판류 기능(엔티티→DTO→service→controller→테스트→프론트) 표준 절차.
- **admin-crud**: 관리자 CRUD 화면 한 세트를 일관 패턴으로 찍어내는 절차.

---

## 6. 주의
- ECC 식별자는 서로 다르다: 소스 repo `affaan-m/everything-claude-code`(=`affaan-m/ECC`), 플러그인 `ecc@ecc`, npm `ecc-universal`. 헷갈리지 말 것.
- 룰은 "항상 적용", 스킬은 "필요 시 호출", 커맨드는 "단발 실행". 프로젝트 제약은 룰(이 저장소)에, 반복 절차는 스킬에 둔다.
- ECC 룰은 본인 워크플로에 안 맞는 게 섞여 있을 수 있다. 필요한 것만 두고 충돌 부분은 프로젝트 룰로 덮거나 제거.
