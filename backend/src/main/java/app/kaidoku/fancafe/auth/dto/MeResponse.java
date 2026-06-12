package app.kaidoku.fancafe.auth.dto;

import app.kaidoku.fancafe.common.Role;
import app.kaidoku.fancafe.member.Member;

/** 현재 로그인 회원 정보(/api/auth/me). 비로그인은 authenticated=false. */
public record MeResponse(
        boolean authenticated,
        Long id,
        String nickname,
        Role role
) {
    public static MeResponse of(Member member) {
        return new MeResponse(true, member.getId(), member.getNickname(), member.getRole());
    }

    public static MeResponse guest() {
        return new MeResponse(false, null, null, null);
    }
}
