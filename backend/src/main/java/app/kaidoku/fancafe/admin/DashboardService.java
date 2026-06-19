package app.kaidoku.fancafe.admin;

import app.kaidoku.fancafe.admin.dto.DashboardStatsResponse;
import app.kaidoku.fancafe.attendance.AttendanceRepository;
import app.kaidoku.fancafe.board.BoardRepository;
import app.kaidoku.fancafe.comment.CommentRepository;
import app.kaidoku.fancafe.comment.CommentStatus;
import app.kaidoku.fancafe.grade.GradeRepository;
import app.kaidoku.fancafe.member.MemberRepository;
import app.kaidoku.fancafe.member.MemberStatus;
import app.kaidoku.fancafe.post.PostRepository;
import app.kaidoku.fancafe.post.PostStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 운영 대시보드 통계 집계. 각 도메인 리포지토리의 count 쿼리만 호출하는 읽기 전용 서비스. */
@Service
public class DashboardService {

    /** "최근" 통계 기준 일수. 매직 넘버 금지(00-stack). */
    private static final int RECENT_DAYS = 7;

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;

    public DashboardService(MemberRepository memberRepository,
                            PostRepository postRepository,
                            CommentRepository commentRepository,
                            BoardRepository boardRepository,
                            GradeRepository gradeRepository,
                            AttendanceRepository attendanceRepository) {
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.boardRepository = boardRepository;
        this.gradeRepository = gradeRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse stats() {
        LocalDateTime recentThreshold = LocalDateTime.now().minusDays(RECENT_DAYS);
        return new DashboardStatsResponse(
                memberRepository.count(),
                memberRepository.countByStatus(MemberStatus.ACTIVE),
                memberRepository.countByStatus(MemberStatus.SUSPENDED),
                memberRepository.countByStatus(MemberStatus.WITHDRAWN),
                memberRepository.countByCreatedAtAfter(recentThreshold),
                postRepository.countByStatus(PostStatus.PUBLISHED),
                postRepository.countByStatusAndCreatedAtAfter(PostStatus.PUBLISHED, recentThreshold),
                commentRepository.countByStatus(CommentStatus.PUBLISHED),
                boardRepository.count(),
                gradeRepository.count(),
                attendanceRepository.countByAttendDate(LocalDate.now()));
    }
}
