package app.kaidoku.fancafe.attendance;

import app.kaidoku.fancafe.attendance.dto.AttendanceResponse;
import app.kaidoku.fancafe.auth.CurrentMember;
import app.kaidoku.fancafe.member.Member;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /** 내 출석 현황(로그인 회원). */
    @GetMapping
    public AttendanceResponse status(@CurrentMember Member member) {
        return attendanceService.getStatus(member);
    }

    /** 오늘 출석 체크(로그인 회원, 멱등). */
    @PostMapping
    public AttendanceResponse checkIn(@CurrentMember Member member) {
        return attendanceService.checkIn(member);
    }
}
