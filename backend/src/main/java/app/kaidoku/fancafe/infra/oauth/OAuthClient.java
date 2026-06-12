package app.kaidoku.fancafe.infra.oauth;

import app.kaidoku.fancafe.member.Provider;

/**
 * 소셜 OAuth 2.0 어댑터(Authorization Code Flow). 제공자별 URL/파라미터 차이를 격리한다.
 * 도메인 서비스는 이 인터페이스만 의존한다(00-stack: infra 어댑터 격리).
 */
public interface OAuthClient {

    Provider provider();

    /** 키가 주입되지 않은 제공자는 비활성(예: 카카오는 후속). */
    boolean isConfigured();

    /** 사용자를 보낼 제공자 인증 페이지 URL. state는 CSRF 방지 난수(콜백에서 검증). */
    String authorizeUrl(String redirectUri, String state);

    /** 콜백으로 받은 code를 토큰으로 교환하고 프로필을 조회한다. 실패 시 예외. */
    OAuthProfile fetchProfile(String code, String redirectUri);
}
