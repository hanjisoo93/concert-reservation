package kr.hhplus.be.server.api.service.token;

import kr.hhplus.be.server.api.controller.token.dto.TokenResponse;
import kr.hhplus.be.server.domain.token.entity.Token;
import kr.hhplus.be.server.domain.token.entity.TokenStatus;
import kr.hhplus.be.server.domain.token.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class TokenService {

    private final TokenRepository tokenRepository;

    public TokenResponse getToken(String userId, TokenStatus tokenStatus) {
        Token token = tokenRepository.findAllByUserIdAndStatus(userId, tokenStatus);
        return TokenResponse.of(token);
    }
}
