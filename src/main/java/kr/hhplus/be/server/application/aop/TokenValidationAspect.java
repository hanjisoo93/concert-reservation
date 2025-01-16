package kr.hhplus.be.server.application.aop;

import jakarta.servlet.http.HttpServletRequest;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.domain.exception.token.TokenException;
import kr.hhplus.be.server.domain.service.token.TokenService;
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

    @Before("@annotation(kr.hhplus.be.server.application.aop.ValidateToken)")
    public void validateToken(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String tokenUuid = request.getHeader("Authorization");

        if (tokenUuid == null || !tokenService.isValidTokenByUuid(tokenUuid)) {
            throw new TokenException(ErrorCode.TOKEN_NOT_FOUND);
        }
    }
}
