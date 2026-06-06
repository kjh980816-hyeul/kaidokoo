package app.kaidoku.fancafe.attendance.dto;

import java.time.LocalDate;

/** 출석 현황. checkedToday=오늘 출석 여부, streak=연속 출석일, totalDays=누적 출석일. */
public record AttendanceResponse(
        boolean checkedToday,
        int streak,
        long totalDays,
        LocalDate today
) {
}
