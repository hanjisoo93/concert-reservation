package kr.hhplus.be.server.interfaces.api.controller.token;

import io.swagger.v3.oas.annotations.Operation;
import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenType;
import kr.hhplus.be.server.interfaces.api.controller.token.dto.TokenResponse;
import kr.hhplus.be.server.domain.service.token.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    private final TokenService tokenService;

    @Operation(summary = "토큰 조회", description = "", tags={ "Token API" })
    @GetMapping(value = "/get")
    public TokenResponse getToken(@RequestParam Long userId) {
        Token token = tokenService.getToken(userId, TokenType.WAITING);
        return new TokenResponse(token.getStatus(), token.getScore());
    }

    @Operation(summary = "토큰 발급", description = "", tags={ "Token API" })
    @PostMapping("/issue")
    public TokenResponse issueToken(@RequestParam Long userId) {
        Token token = tokenService.issueWaitToken(userId);
        return new TokenResponse(token.getStatus(), token.getScore());
    }
}
