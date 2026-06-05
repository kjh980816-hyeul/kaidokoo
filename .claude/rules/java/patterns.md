---
description: Java 21 / Spring Boot 스택 룰 — ECC가 제공하지 않는 Java 보완 룰
globs: ["backend/**/*.java", "**/*.java"]
alwaysApply: true
---

# Java / Spring Boot 패턴 룰 (ECC 미제공 보완)

> ECC는 typescript·python·golang·web 등 스택 룰만 제공하고 Java는 없다. 이 파일이 백엔드의 스택 룰 역할을 한다. ECC `rules/common/`(coding-style·testing·security 등)과 함께 적용된다.

## 구조
- 패키지: `domain` 기준 패키징(예: `member`, `board`, `auth`, `live`) 권장. 레이어별 한 덩어리 패키지보다 도메인 응집.
- `controller` / `service` / `repository` / `dto` / `entity` 역할 분리. controller는 얇게.
- 외부 연동은 `infra/` 어댑터로 격리(씨미 클라이언트 등).

## 엔티티 / JPA
- `ddl-auto=validate` 고정. 스키마는 마이그레이션(Flyway 등)으로 관리.
- 엔티티에 setter 남발 금지. 생성은 정적 팩토리/빌더, 변경은 의미 있는 메서드로.
- 연관관계 기본 `LAZY`. N+1은 fetch join/`@EntityGraph`로 명시 해결.
- 엔티티를 컨트롤러 응답에 직접 노출 금지 → Response DTO 변환.
- 복잡 조회는 QueryDSL/MyBatis 등으로 분리(서비스가 거대 쿼리 문자열을 들고 있지 않게).

## 트랜잭션 / 예외
- 트랜잭션 경계는 service. 조회는 `@Transactional(readOnly = true)`.
- 도메인 예외 → `@RestControllerAdvice` 전역 핸들러에서 표준 에러 응답 매핑. 내부 메시지·스택트레이스 클라이언트 노출 금지.
- `null` 반환 대신 `Optional` 또는 예외.

## 검증 / 보안
- 요청 DTO에 Bean Validation(`@Valid`, `@NotBlank` 등). 비즈니스 검증은 service.
- 권한은 메서드/엔드포인트에서 서버 검증(`@PreAuthorize` 또는 명시 체크). 관리자 API는 `ADMIN` 재확인.
- 시크릿은 `@Value`/설정 바인딩으로 환경변수에서. 코드 상수 금지.

## 테스트 (EDD/TDD)
- 신규 기능은 **테스트 먼저**. service 단위 테스트 + 핵심 플로우 통합 테스트.
- 외부(씨미)는 모킹/스텁으로 격리해 테스트. 실호출 의존 금지.
- 커버리지 목표는 ECC 공통 룰(약 80% TDD)을 따르되, 의미 있는 경로 우선.

## 컨벤션
- Java 21 기능 적극 사용(record DTO, switch 패턴 등) 단, 가독성 우선.
- 불변 우선(`final`, record). 매직 넘버·하드코딩 금지(설정/상수/enum).
- 로깅은 SLF4J, 민감정보 로깅 금지.
