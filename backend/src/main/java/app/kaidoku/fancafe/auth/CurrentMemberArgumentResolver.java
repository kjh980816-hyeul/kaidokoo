package app.kaidoku.fancafe.auth;

import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.member.MemberStatus;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@code @CurrentMember} 파라미터를 해석한다.
 *
 * <p>세션 쿠키({@link AuthController#SESSION_COOKIE}) → member_session → 회원(ADR-0004).
 * 권한(role)·상태(status)는 항상 DB 값으로 서버에서 재검증한다(클라이언트 신뢰 X).
 * (구 X-Member-Id 헤더 임시 신원은 소셜 로그인 도입으로 제거 — ADR-0003 종료)
 */
@Component
public class CurrentMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberSessionRepository sessionRepository;

    public CurrentMemberArgumentResolver(MemberSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentMember.class)
                && Member.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        CurrentMember annotation = parameter.getParameterAnnotation(CurrentMember.class);
        boolean required = annotation == null || annotation.required();

        String token = readSessionToken(webRequest);
        if (token == null || token.isBlank()) {
            return requireOrNull(required);
        }

        MemberSession session = sessionRepository.findById(token).orElse(null);
        if (session == null || session.isExpired()) {
            return requireOrNull(required);
        }

        Member member = session.getMember();
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw ApiException.forbidden("이용이 제한된 계정입니다.");
        }
        return member;
    }

    private static Object requireOrNull(boolean required) {
        if (required) {
            throw ApiException.unauthorized("로그인이 필요합니다.");
        }
        return null;
    }

    private static String readSessionToken(NativeWebRequest webRequest) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null || request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (AuthController.SESSION_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
