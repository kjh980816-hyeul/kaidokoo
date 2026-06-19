package app.kaidoku.fancafe.stats.dto;

/**
 * 공개 통계. 비로그인 방문자도 볼 수 있는 비민감 집계만 노출한다(사이드바 "선원 N명" 등).
 * 개인정보·운영 수치는 포함하지 않는다 — 운영 상세는 {@code /api/admin/stats}(ADMIN).
 */
public record PublicStatsResponse(long memberCount) {
}
