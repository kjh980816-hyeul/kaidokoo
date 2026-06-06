package app.kaidoku.fancafe.attendance;

import app.kaidoku.fancafe.attendance.dto.AttendanceResponse;
import app.kaidoku.fancafe.member.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** 출석 체크. 날짜 기준은 KST. 연속 출석(streak)은 기록으로부터 계산한다. */
@Service
@Transactional(readOnly = true)
public class AttendanceService {

    /** 팬카페 기준 시간대(한국). 출석 '오늘'의 경계를 KST 자정으로 둔다. */
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public AttendanceResponse getStatus(Member member) {
        return buildStatus(member.getId(), today());
    }

    /** 오늘 출석 체크(멱등). 이미 했으면 그대로, 아니면 기록 추가. */
    @Transactional
    public AttendanceResponse checkIn(Member member) {
        LocalDate today = today();
        if (!attendanceRepository.existsByMember_IdAndAttendDate(member.getId(), today)) {
            attendanceRepository.save(Attendance.create(member, today));
        }
        return buildStatus(member.getId(), today);
    }

    private AttendanceResponse buildStatus(Long memberId, LocalDate today) {
        List<LocalDate> dates = attendanceRepository.findByMember_IdOrderByAttendDateDesc(memberId).stream()
                .map(Attendance::getAttendDate)
                .toList();
        boolean checkedToday = !dates.isEmpty() && dates.get(0).equals(today);
        int streak = calculateStreak(dates, today);
        return new AttendanceResponse(checkedToday, streak, dates.size(), today);
    }

    /**
     * 연속 출석일 계산. 오늘 출석했으면 오늘부터, 아니면 어제부터(아직 오늘 체크 전) 거꾸로 센다.
     * 기준일이 비어 있으면(어제·오늘 모두 미출석) 0.
     */
    static int calculateStreak(List<LocalDate> attendDatesDesc, LocalDate today) {
        Set<LocalDate> dates = attendDatesDesc.stream().collect(Collectors.toUnmodifiableSet());
        LocalDate anchor;
        if (dates.contains(today)) {
            anchor = today;
        } else if (dates.contains(today.minusDays(1))) {
            anchor = today.minusDays(1);
        } else {
            return 0;
        }
        int streak = 0;
        for (LocalDate d = anchor; dates.contains(d); d = d.minusDays(1)) {
            streak++;
        }
        return streak;
    }

    private LocalDate today() {
        return LocalDate.now(KST);
    }
}
