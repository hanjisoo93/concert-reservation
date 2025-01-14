package kr.hhplus.be.server.domain.service.token;

import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import kr.hhplus.be.server.infra.repository.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class TokenService {

    private final TokenRepository tokenRepository;

    public boolean isValidTokenByUuid(String tokenUuid) {
        return tokenRepository.findByUuid(tokenUuid)
                .filter(token -> !token.isExpired()) // 만료되지 않은 토큰
                .filter(token -> token.getStatus() == TokenStatus.WAIT || token.getStatus() == TokenStatus.ACTIVE) // 유효 상태
                .isPresent();
    }

    @Transactional(readOnly = true)
    public Token getToken(Long userId, TokenStatus tokenStatus) {
        return tokenRepository.findAllByUserIdAndStatus(userId, tokenStatus);
    }

    @Transactional
    public Token issueWaitToken(Long userId) {
        // 1. 기존 토큰 조회 (락 없이 진행)
        Optional<Token> existingToken = tokenRepository.findFirstByUserIdAndStatusAndNotExpired(
                userId,
                Arrays.asList(TokenStatus.WAIT, TokenStatus.ACTIVE),
                LocalDateTime.now()
        );

        if (existingToken.isPresent()) {
            Token token = existingToken.get();
            if (TokenStatus.ACTIVE.equals(token.getStatus()) ||
                    (TokenStatus.WAIT.equals(token.getStatus()) && !token.isExpired())) {
                return token;
            }
        }

        // 2. 토큰 생성 단계 (락 사용)
        return createTokenWithLock(userId);
    }

    @Transactional
    public Token createTokenWithLock(Long userId) {
        synchronized (userId.toString().intern()) {
            // 1. 중복 생성 방지용 다시 조회
            Optional<Token> latestToken = tokenRepository.findFirstByUserIdAndStatusAndNotExpired(
                    userId,
                    Arrays.asList(TokenStatus.WAIT, TokenStatus.ACTIVE),
                    LocalDateTime.now()
            );
            if (latestToken.isPresent()) {
                return latestToken.get();
            }

            // 2. 새로운 토큰 생성
            Token newToken = Token.createToken(userId);
            tokenRepository.save(newToken);
            return newToken;
        }
    }

    @Transactional
    public void updateTokenExpiredAt(String uuid) {
        tokenRepository.findByUuid(uuid)
                .ifPresent(token -> {
                    token.updateExpiredAt(LocalDateTime.now().plusMinutes(30));
                    tokenRepository.save(token);
                });
    }
}
