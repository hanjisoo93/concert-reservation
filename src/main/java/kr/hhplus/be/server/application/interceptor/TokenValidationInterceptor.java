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
        String userIdStr = request.getHeader("Authorization");
        if (userIdStr == null) {
            log.warn("대기열 검증 실패 - Authorization 헤더 없음");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized: 유효한 토큰이 필요합니다.");
            return false;
        }

        Long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            log.warn("대기열 검증 실패 - Authorization 헤더의 userId 형식 오류: {}", userIdStr);
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized: 올바른 userId 형식이 필요합니다.");
            return false;
        }

        boolean valid = tokenService.isValidToken(userId);
        if (!valid) {
            log.warn("대기열 검증 실패 - 유효한 토큰이 존재하지 않음: userId={}", userId);
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized: 유효한 토큰이 필요합니다.");
            return false;
        }

        log.info("대기열 검증 성공 - userId={}", userId);
        return true;
    }
}

