-- V5: 라이브 배너(관리자 수동 토글) + 사이트 설정.
-- 씨미 공개 라이브 폴링은 Client-Id/Secret 키가 필요 → 후속(10-seeme). 지금은 수동 오버라이드만.

CREATE TABLE live_status (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    is_live       BOOLEAN     NOT NULL DEFAULT FALSE,   -- (향후) 씨미 폴링 결과 캐시
    title         VARCHAR(200),
    stream_url    VARCHAR(500),
    override_mode VARCHAR(20) NOT NULL DEFAULT 'AUTO',  -- AUTO/FORCE_ON/FORCE_OFF (수동 오버라이드 우선)
    updated_at    DATETIME    NOT NULL,
    PRIMARY KEY (id)
);

-- key/value는 MySQL 예약어 → setting_key/setting_value.
CREATE TABLE site_setting (
    setting_key   VARCHAR(100)  NOT NULL,
    setting_value VARCHAR(1000),
    PRIMARY KEY (setting_key)
);

-- 단일 라이브 상태 행(베이스라인, 전 프로파일). 관리자가 토글/수정한다.
INSERT INTO live_status (is_live, title, stream_url, override_mode, updated_at)
VALUES (FALSE, NULL, NULL, 'FORCE_OFF', CURRENT_TIMESTAMP);
