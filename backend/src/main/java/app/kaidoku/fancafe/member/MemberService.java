package app.kaidoku.fancafe.member;

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

    public MemberService(MemberRepository memberRepository, GradeRepository gradeRepository) {
        this.memberRepository = memberRepository;
        this.gradeRepository = gradeRepository;
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

    @Transactional
    public void changeStatus(Long memberId, MemberStatus status) {
        getMember(memberId).changeStatus(status);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> ApiException.notFound("회원을 찾을 수 없습니다: " + memberId));
    }
}
