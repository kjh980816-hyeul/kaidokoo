# ADR-0002 · 로컬 DB = H2, 스택 베이스라인 (Spring Boot 3.5 / Java 21 toolchain)

- 상태: 확정 (2026-06-05)
- 맥락: P1 스캐폴드 착수 시점에 개발 머신에 MySQL 서버·Docker·JDK 21이 모두 없었다. 스펙(CLAUDE.md §2)은 MySQL 8 · Java 21 · Spring Boot 3.x.

## 결정
1. **로컬/테스트 DB = H2 (MySQL 호환 모드), 운영 = MySQL 8.**
   - `local` 프로파일: `jdbc:h2:mem:...;MODE=MySQL`. `prod` 프로파일: MySQL 8(접속정보 환경변수).
   - Flyway 마이그레이션은 **H2/MySQL 양쪽 호환 SQL**로 작성. `ddl-auto=validate` 고정 유지.
   - 로컬 전용 시드는 `db/seed` 위치를 local 프로파일에서만 로드(운영 미적용).
2. **Java 21**은 Gradle **toolchain 자동 프로비저닝**(foojay resolver)으로 충당. PATH의 JDK 17은 Gradle 런처용으로만 사용 → 수동 설치 불필요, 스펙(21) 준수.
3. **Spring Boot는 3.5.0으로 고정.** Initializr 기본값이 4.0.x였으나 스펙이 "3.x"이고 고추밭 이식 코드가 Boot 3 기반이라 메이저 점프(Spring Framework 7) 리스크를 피한다.

## 근거
- 설치 0으로 즉시 빌드·테스트·실행이 가능해 수직 슬라이스를 바로 검증할 수 있다(준하님 선택).
- 운영은 스펙대로 MySQL 8 유지 → 프로파일 전환만으로 승격.

## 리스크 / 완화
- H2↔MySQL SQL 방언 차이(예: `MEDIUMTEXT`, `AUTO_INCREMENT`)로 "로컬 통과·운영 실패"가 숨을 수 있음(고추밭의 `@Lob`/tinytext 함정 선례).
  - 완화: 마이그레이션 SQL을 보수적 호환 표기로 작성. MySQL 도입 시 동일 마이그레이션을 MySQL에서 재검증. 가능해지면 운영-동등 DB(MySQL/Testcontainers)로 통합 테스트 승격.
- Boot 4 전환은 추후 별도 ADR로 결정.
