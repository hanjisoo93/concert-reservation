package kr.hhplus.be.server.application.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.hhplus.be.server.domain.service.token.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenValidationInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String tokenUuid = request.getHeader("Authorization");

        if (tokenUuid == null || !tokenService.isValidTokenByUuid(tokenUuid)) {
            log.warn("대기열 검증 실패 - Authorization 헤더 없음 또는 유효하지 않은 토큰");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized: 유효한 토큰이 필요합니다.");
            return false;
        }

        log.info("대기열 검증 성공 - tokenUuid={}", tokenUuid);
        return true;
    }
}
