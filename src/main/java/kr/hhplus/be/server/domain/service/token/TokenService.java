package kr.hhplus.be.server.domain.service.token;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.SystemException;
import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import kr.hhplus.be.server.domain.exception.token.TokenException;
import kr.hhplus.be.server.infra.repository.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class TokenService {

    private final TokenRepository tokenRepository;

    @Transactional(readOnly = true)
    public boolean isValidTokenByUuid(String tokenUuid) {
        try {
            return tokenRepository.findByUuid(tokenUuid)
                    .filter(token -> !token.isExpired()) // 만료되지 않은 토큰
                    .filter(token -> token.getStatus() == TokenStatus.WAIT || token.getStatus() == TokenStatus.ACTIVE) // 유효 상태
                    .isPresent();
        } catch (Exception e) {
            log.error("토큰 유효성 검사 중 시스템 오류 발생 - tokenUuid={}", tokenUuid, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public Token getToken(Long userId, TokenStatus tokenStatus) {
        try {
            return tokenRepository.findAllByUserIdAndStatus(userId, tokenStatus);
        } catch (Exception e) {
            log.error("토큰 조회 중 시스템 오류 발생 - userId={}, status={}", userId, tokenStatus, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }    }

    @Transactional
    public Token issueWaitToken(Long userId) {
        try {
            log.info("토큰 발급 시도 - userId={}", userId);

            // 1. 기존 토큰 조회
            Optional<Token> existingToken = tokenRepository.findFirstByUserIdAndStatusAndNotExpired(
                    userId,
                    Arrays.asList(TokenStatus.WAIT, TokenStatus.ACTIVE),
                    LocalDateTime.now()
            );

            if (existingToken.isPresent()) {
                Token token = existingToken.get();
                if (TokenStatus.ACTIVE.equals(token.getStatus()) ||
                        (TokenStatus.WAIT.equals(token.getStatus()) && !token.isExpired())) {
                    log.info("기존 유효한 토큰 반환 - tokenUuid={}, userId={}", token.getUuid(), userId);
                    return token;
                }
            }

            // 2. 새로운 토큰 발급
            Token newToken = createTokenWithLock(userId);
            log.info("새로운 토큰 발급 완료 - tokenUuid={}, userId={}", newToken.getUuid(), userId);
            return newToken;

        } catch (Exception e) {
            log.error("토큰 발급 중 시스템 오류 발생 - userId={}", userId, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    public Token createTokenWithLock(Long userId) {
        synchronized (userId.toString().intern()) {
            try {
                log.info("토큰 생성 시도 - userId={}", userId);

                // 1. 중복 생성 방지용 다시 조회
                Optional<Token> latestToken = tokenRepository.findFirstByUserIdAndStatusAndNotExpired(
                        userId,
                        Arrays.asList(TokenStatus.WAIT, TokenStatus.ACTIVE),
                        LocalDateTime.now()
                );

                if (latestToken.isPresent()) {
                    log.info("토큰 생성 불필요 - 기존 유효 토큰 존재 - tokenUuid={}, userId={}", latestToken.get().getUuid(), userId);
                    return latestToken.get();
                }

                // 2. 새로운 토큰 생성
                Token newToken = Token.createToken(userId);
                tokenRepository.save(newToken);
                log.info("새로운 토큰 생성 완료 - tokenUuid={}, userId={}", newToken.getUuid(), userId);
                return newToken;
            } catch (Exception e) {
                log.error("토큰 생성 중 시스템 오류 발생 - userId={}", userId, e);
                throw new SystemException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }

    @Transactional
    public void updateTokenExpiredAt(String uuid, int minutes) {
        try {
            tokenRepository.findByUuid(uuid)
                    .ifPresentOrElse(token -> {
                        token.updateExpiredAt(LocalDateTime.now().plusMinutes(minutes));
                        tokenRepository.save(token);
                        log.info("토큰 만료 시간 연장 완료 - tokenUuid={}, newExpiredAt={}", uuid, token.getExpiredAt());
                    }, () -> {
                        log.warn("토큰 만료 시간 연장 실패 - 존재하지 않는 토큰: tokenUuid={}", uuid);
                        throw new TokenException(ErrorCode.TOKEN_NOT_FOUND);
                    });
        } catch (TokenException e) {
            log.warn("토큰 만료 시간 연장 실패 - tokenUuid={}", uuid, e);
            throw e;
        } catch (Exception e) {
            log.error("토큰 만료 시간 연장 중 시스템 오류 발생 - tokenUuid={}", uuid, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
