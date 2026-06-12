package app.kaidoku.fancafe.auth;

import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.common.Role;
import app.kaidoku.fancafe.grade.GradeRepository;
import app.kaidoku.fancafe.infra.oauth.OAuthClients;
import app.kaidoku.fancafe.infra.oauth.OAuthProfile;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.member.MemberRepository;
import app.kaidoku.fancafe.member.MemberStatus;
import app.kaidoku.fancafe.member.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * 소셜 로그인(ADR-0001) → 자체 세션 발급(ADR-0004).
 * 제공자 토큰은 프로필 1회 조회에만 쓰고 저장하지 않는다(개인정보·시크릿 최소).
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    /** 세션 수명. 만료 시 재로그인. */
    public static final Duration SESSION_TTL = Duration.ofDays(30);

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OAuthClients oauthClients;
    private final MemberRepository memberRepository;
    private final GradeRepository gradeRepository;
    private final MemberSessionRepository sessionRepository;
    private final String baseUrl;

    public AuthService(OAuthClients oauthClients,
                       MemberRepository memberRepository,
                       GradeRepository gradeRepository,
                       MemberSessionRepository sessionRepository,
                       @Value("${app.auth.base-url:http://localhost:5173}") String baseUrl) {
        this.oauthClients = oauthClients;
        this.memberRepository = memberRepository;
        this.gradeRepository = gradeRepository;
        this.sessionRepository = sessionRepository;
        this.baseUrl = baseUrl;
    }

    /** 제공자 인증 페이지 URL(로그인 시작). */
    public String authorizeUrl(Provider provider, String state) {
        return oauthClients.get(provider).authorizeUrl(redirectUri(provider), state);
    }

    /** 콜백 code → 프로필 조회 → 회원 조회/생성 → 세션 발급. 반환값은 세션 토큰. */
    @Transactional
    public String loginWithCode(Provider provider, String code) {
        OAuthProfile profile = oauthClients.get(provider).fetchProfile(code, redirectUri(provider));
        Member member = findOrCreateMember(provider, profile);
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw ApiException.forbidden("이용이 제한된 계정입니다.");
        }
        MemberSession session = MemberSession.create(
                generateToken(), member, LocalDateTime.now().plus(SESSION_TTL));
        sessionRepository.save(session);
        return session.getToken();
    }

    @Transactional
    public void logout(String token) {
        sessionRepository.deleteById(token);
    }

    private Member findOrCreateMember(Provider provider, OAuthProfile profile) {
        return memberRepository.findByProviderAndProviderUserId(provider, profile.providerUserId())
                .orElseGet(() -> {
                    Member member = Member.create(provider, profile.providerUserId(), profile.nickname());
                    // 신규 회원은 기본 등급으로 시작(20-domain-rules).
                    gradeRepository.findFirstByIsDefaultTrue().ifPresent(member::assignGrade);
                    // 부트스트랩: 최초 가입자 = 운영자(ADR-0004). 이후 권한은 관리자 화면에서 배정.
                    if (memberRepository.count() == 0) {
                        member.changeRole(Role.ADMIN);
                        log.info("최초 가입자를 ADMIN으로 부트스트랩: provider={}", provider);
                    }
                    return memberRepository.save(member);
                });
    }

    private String redirectUri(Provider provider) {
        return baseUrl + "/api/auth/" + provider.name().toLowerCase() + "/callback";
    }

    /** URL-safe 난수 토큰(32바이트). 세션·state 공용. */
    public static String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
