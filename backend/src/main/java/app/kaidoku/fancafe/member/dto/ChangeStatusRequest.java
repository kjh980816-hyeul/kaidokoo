package app.kaidoku.fancafe.member.dto;

import app.kaidoku.fancafe.member.MemberStatus;
import jakarta.validation.constraints.NotNull;

/** 회원 상태 변경(관리자). 정지/탈퇴 처리 등. */
public record ChangeStatusRequest(
        @NotNull(message = "상태는 필수입니다")
        MemberStatus status
) {
}
