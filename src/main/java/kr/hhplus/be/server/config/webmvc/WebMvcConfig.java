package kr.hhplus.be.server.config.webmvc;

import kr.hhplus.be.server.application.interceptor.TokenValidationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TokenValidationInterceptor tokenValidationInterceptor;

    public WebMvcConfig(TokenValidationInterceptor tokenValidationInterceptor){
        this.tokenValidationInterceptor = tokenValidationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(tokenValidationInterceptor)
                .addPathPatterns("/api/**") // 모든 API에 대기열 검증 적용
                .excludePathPatterns("/swagger-ui.html")
                .excludePathPatterns("/api/tokens/**")
                // K6 테스트 용
                .excludePathPatterns("/reservation/optimistic")
                .excludePathPatterns("/reservation/pessimistic")
                .excludePathPatterns("/reservation/distributed")
                .excludePathPatterns("/point/optimistic")
                .excludePathPatterns("/point/pessimistic")
                .excludePathPatterns("/point/distributed");
    }

}
