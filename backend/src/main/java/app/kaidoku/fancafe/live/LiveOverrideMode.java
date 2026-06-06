package app.kaidoku.fancafe.live;

/**
 * 라이브 상태 결정 방식.
 * <ul>
 *   <li>AUTO — (향후) 씨미 공개 라이브 폴링 결과를 따른다. 폴링은 Client-Id/Secret 필요(미구현).</li>
 *   <li>FORCE_ON / FORCE_OFF — 관리자 수동 오버라이드. 폴링보다 우선.</li>
 * </ul>
 */
public enum LiveOverrideMode {
    AUTO,
    FORCE_ON,
    FORCE_OFF
}
