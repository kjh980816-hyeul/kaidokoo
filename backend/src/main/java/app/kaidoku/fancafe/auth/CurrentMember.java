package app.kaidoku.fancafe.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 파라미터에 "현재 로그인 회원"을 주입한다.
 *
 * <p>TODO(P2-auth): 지금은 소셜 로그인(네이버/카카오/구글) 도입 전이라 식별 출처가 임시다.
 * {@link CurrentMemberArgumentResolver}가 {@code X-Member-Id} 헤더로 회원을 해석한다.
 * 세션/JWT 인증이 들어오면 <b>리졸버 한 곳만</b> 세션 기반으로 교체하면 된다(ADR-0003).
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentMember {

    /** false면 비로그인(헤더 없음) 시 예외 대신 null을 주입한다(공개+선택적 개인화 엔드포인트용). */
    boolean required() default true;
}
