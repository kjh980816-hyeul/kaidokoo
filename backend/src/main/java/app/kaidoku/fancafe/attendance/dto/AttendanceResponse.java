package app.kaidoku.fancafe.attendance.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 출석 현황. checkedToday=오늘 출석 여부, streak=연속 출석일, totalDays=누적 출석일.
 * 달력 위젯용: year/month(이달) + daysInMonth + monthAttendedDays(이달 출석한 '일' 목록).
 */
public record AttendanceResponse(
        boolean checkedToday,
        int streak,
        long totalDays,
        LocalDate today,
        int year,
        int month,
        int daysInMonth,
        List<Integer> monthAttendedDays
) {
}
