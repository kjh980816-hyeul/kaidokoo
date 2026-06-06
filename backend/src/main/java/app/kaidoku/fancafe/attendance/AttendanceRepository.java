package app.kaidoku.fancafe.attendance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByMember_IdAndAttendDate(Long memberId, LocalDate attendDate);

    List<Attendance> findByMember_IdOrderByAttendDateDesc(Long memberId);
}
