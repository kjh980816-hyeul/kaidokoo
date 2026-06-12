package app.kaidoku.fancafe.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 파라미터에 "현재 로그인 회원"을 주입한다.
 *
 * <p>{@link CurrentMemberArgumentResolver}가 세션 쿠키로 회원을 해석한다(ADR-0004).
 * 권한·상태는 항상 DB 값으로 재검증된다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentMember {

    /** false면 비로그인(헤더 없음) 시 예외 대신 null을 주입한다(공개+선택적 개인화 엔드포인트용). */
    boolean required() default true;
}
