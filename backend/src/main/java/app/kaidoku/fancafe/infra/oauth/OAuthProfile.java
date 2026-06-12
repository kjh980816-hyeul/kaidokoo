package app.kaidoku.fancafe.infra.oauth;

/** 소셜 제공자에서 받아온 최소 프로필(식별자 + 닉네임). 개인정보 최소 수집(HARD). */
public record OAuthProfile(String providerUserId, String nickname) {
}
