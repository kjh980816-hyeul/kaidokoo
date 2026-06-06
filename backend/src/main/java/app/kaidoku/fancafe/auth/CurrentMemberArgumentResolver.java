package app.kaidoku.fancafe.auth;

import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.member.MemberRepository;
import app.kaidoku.fancafe.member.MemberStatus;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@code @CurrentMember} 파라미터를 해석한다.
 *
 * <p><b>임시 인증(dev stand-in).</b> 소셜 로그인 도입 전까지 {@code X-Member-Id} 헤더의 회원 id로
 * 신원을 해석한다. 헤더는 위조 가능하므로 이는 운영 보안이 아니라 인증 전 개발용 발판이다.
 * 단, 권한(role)·상태(status)는 <b>항상 DB 값으로 서버에서 재검증</b>하므로(클라이언트 신뢰 X)
 * 인증이 들어와도 이 검증 로직은 그대로 두고 신원 출처만 세션으로 바꾸면 된다. (ADR-0003)
 */
@Component
public class CurrentMemberArgumentResolver implements HandlerMethodArgumentResolver {

    /** 임시 신원 헤더. 인증 도입 시 제거 대상(TODO P2-auth). */
    public static final String MEMBER_ID_HEADER = "X-Member-Id";

    private final MemberRepository memberRepository;

    public CurrentMemberArgumentResolver(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
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

        String raw = webRequest.getHeader(MEMBER_ID_HEADER);
        if (raw == null || raw.isBlank()) {
            if (required) {
                throw ApiException.unauthorized("로그인이 필요합니다.");
            }
            return null;
        }

        long memberId;
        try {
            memberId = Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            throw ApiException.unauthorized("로그인이 필요합니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> ApiException.unauthorized("로그인이 필요합니다."));
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw ApiException.forbidden("이용이 제한된 계정입니다.");
        }
        return member;
    }
}
