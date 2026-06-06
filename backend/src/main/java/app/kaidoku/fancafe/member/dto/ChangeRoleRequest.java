package app.kaidoku.fancafe.member.dto;

import app.kaidoku.fancafe.common.Role;
import jakarta.validation.constraints.NotNull;

/** 회원 권한 변경(관리자). */
public record ChangeRoleRequest(
        @NotNull(message = "권한은 필수입니다")
        Role role
) {
}
