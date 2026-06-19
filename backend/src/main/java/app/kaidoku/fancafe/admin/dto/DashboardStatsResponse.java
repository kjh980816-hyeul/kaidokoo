package app.kaidoku.fancafe.admin.dto;

/**
 * 운영 대시보드 통계. 여러 도메인의 집계 수치를 한 번에 내려준다.
 * 개인정보는 포함하지 않는 단순 카운트만 노출한다(30-security).
 */
public record DashboardStatsResponse(
        long totalMembers,
        long activeMembers,
        long suspendedMembers,
        long withdrawnMembers,
        long newMembers7d,
        long totalPosts,
        long newPosts7d,
        long totalComments,
        long totalBoards,
        long totalGrades,
        long attendanceToday
) {
}
