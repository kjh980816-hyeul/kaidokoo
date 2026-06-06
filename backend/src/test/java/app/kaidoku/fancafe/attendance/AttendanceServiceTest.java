package app.kaidoku.fancafe.attendance;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 연속 출석(streak) 순수 계산 검증. */
class AttendanceServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 7);

    @Test
    void streak_countsConsecutiveDaysEndingToday() {
        List<LocalDate> dates = List.of(TODAY, TODAY.minusDays(1), TODAY.minusDays(2));

        assertThat(AttendanceService.calculateStreak(dates, TODAY)).isEqualTo(3);
    }

    @Test
    void streak_breaksOnGap() {
        // 오늘, 어제 출석했으나 그제는 빠짐 → streak 2
        List<LocalDate> dates = List.of(TODAY, TODAY.minusDays(1), TODAY.minusDays(3));

        assertThat(AttendanceService.calculateStreak(dates, TODAY)).isEqualTo(2);
    }

    @Test
    void streak_continuesFromYesterdayWhenNotCheckedToday() {
        // 오늘은 아직 미출석이지만 어제·그제 연속 → streak 2 (오늘 체크 전 표시)
        List<LocalDate> dates = List.of(TODAY.minusDays(1), TODAY.minusDays(2));

        assertThat(AttendanceService.calculateStreak(dates, TODAY)).isEqualTo(2);
    }

    @Test
    void streak_zeroWhenLastAttendanceTooOld() {
        List<LocalDate> dates = List.of(TODAY.minusDays(3));

        assertThat(AttendanceService.calculateStreak(dates, TODAY)).isZero();
    }

    @Test
    void streak_zeroWhenNoAttendance() {
        assertThat(AttendanceService.calculateStreak(List.of(), TODAY)).isZero();
    }
}
