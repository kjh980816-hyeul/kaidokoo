package app.kaidoku.fancafe.auth;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/** {@code @CurrentMember} 리졸버를 MVC에 등록한다. */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final CurrentMemberArgumentResolver currentMemberArgumentResolver;

    public WebMvcConfig(CurrentMemberArgumentResolver currentMemberArgumentResolver) {
        this.currentMemberArgumentResolver = currentMemberArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentMemberArgumentResolver);
    }
}
