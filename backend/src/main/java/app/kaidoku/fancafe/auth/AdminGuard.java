package app.kaidoku.fancafe.auth;

import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.common.Role;
import app.kaidoku.fancafe.member.Member;

/**
 * 관리자 권한 서버 재검증.
 *
 * <p>HARD(30-security): 모든 관리자 기능은 서버에서 {@code role == ADMIN}을 재확인한다.
 * 프론트의 화면 숨김은 보안이 아니다. 관리자 컨트롤러/서비스는 진입 시 {@link #require(Member)}를 호출한다.
 */
public final class AdminGuard {

    private AdminGuard() {
    }

    public static void require(Member member) {
        if (member == null || member.getRole() != Role.ADMIN) {
            throw ApiException.forbidden("관리자 권한이 필요합니다.");
        }
    }
}
