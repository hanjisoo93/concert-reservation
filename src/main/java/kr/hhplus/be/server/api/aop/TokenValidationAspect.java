package kr.hhplus.be.server.api.aop;

import jakarta.servlet.http.HttpServletRequest;
import kr.hhplus.be.server.api.service.token.TokenService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class TokenValidationAspect {

    private final TokenService tokenService;

    @Before("@annotation(ValidateToken)")
    public void validateToken(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String tokenUuid = request.getHeader("Authorization");

        if (tokenUuid == null || !tokenService.isValidTokenByUuid(tokenUuid)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
    }
}
