package app.kaidoku.fancafe.auth;

import app.kaidoku.fancafe.auth.dto.MeResponse;
import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.member.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * 소셜 로그인 플로우(서버사이드 code flow, ADR-0004).
 * state는 HttpOnly 쿠키 + 콜백 파라미터 비교로 검증(CSRF, 30-constraints).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** 자체 세션 쿠키. 리졸버({@link CurrentMemberArgumentResolver})가 읽는다. */
    public static final String SESSION_COOKIE = "KAIDOKU_SESSION";
    private static final String STATE_COOKIE = "OAUTH_STATE";
    private static final Duration STATE_TTL = Duration.ofMinutes(10);

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final boolean cookieSecure;

    public AuthController(AuthService authService,
                          @Value("${app.auth.cookie-secure:false}") boolean cookieSecure) {
        this.authService = authService;
        this.cookieSecure = cookieSecure;
    }

    /** 로그인 시작: state 쿠키 굽고 제공자 인증 페이지로 302. */
    @GetMapping("/{provider}/login")
    public ResponseEntity<Void> login(@PathVariable String provider) {
        Provider p = parseProvider(provider);
        String state = AuthService.generateToken();
        ResponseCookie stateCookie = ResponseCookie.from(STATE_COOKIE, state)
                .httpOnly(true).secure(cookieSecure).path("/api/auth")
                .maxAge(STATE_TTL).sameSite("Lax").build();
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, stateCookie.toString())
                .header(HttpHeaders.LOCATION, authService.authorizeUrl(p, state))
                .build();
    }

    /** 제공자 콜백: state 검증 → 토큰 교환/프로필 → 세션 쿠키 발급 → 홈으로 302. */
    @GetMapping("/{provider}/callback")
    public ResponseEntity<Void> callback(@PathVariable String provider,
                                         @RequestParam(required = false) String code,
                                         @RequestParam(required = false) String state,
                                         @RequestParam(required = false) String error,
                                         @CookieValue(name = STATE_COOKIE, required = false) String stateCookie) {
        Provider p = parseProvider(provider);
        if (error != null || code == null
                || state == null || stateCookie == null || !stateCookie.equals(state)) {
            log.warn("OAuth 콜백 거부: provider={}, error={}, stateMatch={}",
                    p, error, stateCookie != null && stateCookie.equals(state));
            return redirect("/?login=error", expireCookie(STATE_COOKIE, "/api/auth"));
        }
        try {
            String token = authService.loginWithCode(p, code);
            ResponseCookie sessionCookie = ResponseCookie.from(SESSION_COOKIE, token)
                    .httpOnly(true).secure(cookieSecure).path("/")
                    .maxAge(AuthService.SESSION_TTL).sameSite("Lax").build();
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.SET_COOKIE, sessionCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, expireCookie(STATE_COOKIE, "/api/auth").toString())
                    .header(HttpHeaders.LOCATION, "/")
                    .build();
        } catch (ApiException e) {
            throw e; // 제한 계정 등 도메인 예외는 표준 에러 응답으로
        } catch (Exception e) {
            // 외부(제공자) 호출 실패: 상세는 서버 로그로만, 사용자는 홈에서 안내.
            log.warn("소셜 로그인 실패: provider={}", p, e);
            return redirect("/?login=error", expireCookie(STATE_COOKIE, "/api/auth"));
        }
    }

    /** 로그아웃: 세션 삭제 + 쿠키 만료. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = SESSION_COOKIE, required = false) String token) {
        if (token != null && !token.isBlank()) {
            authService.logout(token);
        }
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expireCookie(SESSION_COOKIE, "/").toString())
                .build();
    }

    /** 현재 로그인 회원. 비로그인도 200(authenticated=false) — 프론트 초기화용. */
    @GetMapping("/me")
    public MeResponse me(@CurrentMember(required = false) Member member) {
        return member == null ? MeResponse.guest() : MeResponse.of(member);
    }

    private static Provider parseProvider(String raw) {
        try {
            return Provider.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.notFound("지원하지 않는 로그인 방식입니다: " + raw);
        }
    }

    private ResponseCookie expireCookie(String name, String path) {
        return ResponseCookie.from(name, "")
                .httpOnly(true).secure(cookieSecure).path(path)
                .maxAge(0).sameSite("Lax").build();
    }

    private static ResponseEntity<Void> redirect(String location, ResponseCookie cookie) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.LOCATION, location)
                .build();
    }
}
