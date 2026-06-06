package app.kaidoku.fancafe.member.dto;

import app.kaidoku.fancafe.common.Role;
import app.kaidoku.fancafe.grade.Grade;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.member.MemberStatus;
import app.kaidoku.fancafe.member.Provider;

import java.time.LocalDateTime;

/** 관리자용 회원 응답. 개인정보 최소화 — provider 식별자 원문은 노출하지 않는다. */
public record MemberAdminResponse(
        Long id,
        Provider provider,
        String nickname,
        Role role,
        MemberStatus status,
        Long gradeId,
        String gradeName,
        String gradeColor,
        LocalDateTime createdAt
) {
    public static MemberAdminResponse from(Member m) {
        Grade grade = m.getGrade();
        return new MemberAdminResponse(
                m.getId(), m.getProvider(), m.getNickname(), m.getRole(), m.getStatus(),
                grade != null ? grade.getId() : null,
                grade != null ? grade.getName() : null,
                grade != null ? grade.getBadgeColor() : null,
                m.getCreatedAt());
    }
}
