package app.kaidoku.fancafe.member;

import app.kaidoku.fancafe.auth.MemberSessionRepository;
import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.common.Role;
import app.kaidoku.fancafe.grade.Grade;
import app.kaidoku.fancafe.grade.GradeRepository;
import app.kaidoku.fancafe.member.dto.MemberAdminResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 회원 관리(관리자). 등급 배정·권한·상태 변경. 권한 검증(ADMIN)은 컨트롤러에서 수행. */
@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final GradeRepository gradeRepository;
    private final MemberSessionRepository sessionRepository;

    public MemberService(MemberRepository memberRepository, GradeRepository gradeRepository,
                         MemberSessionRepository sessionRepository) {
        this.memberRepository = memberRepository;
        this.gradeRepository = gradeRepository;
        this.sessionRepository = sessionRepository;
    }

    public List<MemberAdminResponse> listMembers() {
        return memberRepository.findAllWithGrade().stream()
                .map(MemberAdminResponse::from)
                .toList();
    }

    /** 등급 배정/해제. gradeId가 null이면 등급 해제. */
    @Transactional
    public void assignGrade(Long memberId, Long gradeId) {
        Member member = getMember(memberId);
        Grade grade = gradeId == null ? null : gradeRepository.findById(gradeId)
                .orElseThrow(() -> ApiException.badRequest("등급을 찾을 수 없습니다: " + gradeId));
        member.assignGrade(grade);
    }

    @Transactional
    public void changeRole(Long memberId, Role role) {
        getMember(memberId).changeRole(role);
    }

    /**
     * 회원 상태 변경(활동정지/강제탈퇴/복구). 자물쇠 방지:
     * 본인·다른 관리자는 정지/탈퇴 불가(먼저 일반 회원으로 강등). 비ACTIVE 전환 시 기존 세션 즉시 무효화.
     */
    @Transactional
    public void changeStatus(Long actingAdminId, Long memberId, MemberStatus status) {
        if (actingAdminId.equals(memberId)) {
            throw ApiException.badRequest("본인 계정의 상태는 변경할 수 없습니다.");
        }
        Member target = getMember(memberId);
        if (status != MemberStatus.ACTIVE && target.getRole() == Role.ADMIN) {
            throw ApiException.badRequest("관리자 계정은 정지/탈퇴할 수 없습니다. 먼저 일반 회원으로 변경하세요.");
        }
        target.changeStatus(status);
        if (status != MemberStatus.ACTIVE) {
            sessionRepository.deleteByMember_Id(memberId); // 강제 로그아웃
        }
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> ApiException.notFound("회원을 찾을 수 없습니다: " + memberId));
    }
}
