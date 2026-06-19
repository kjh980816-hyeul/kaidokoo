package app.kaidoku.fancafe.attendance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByMember_IdAndAttendDate(Long memberId, LocalDate attendDate);

    List<Attendance> findByMember_IdOrderByAttendDateDesc(Long memberId);

    /** 특정 날짜 출석 회원 수(대시보드 오늘 출석 통계). */
    long countByAttendDate(LocalDate attendDate);
}
