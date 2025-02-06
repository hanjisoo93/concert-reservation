package kr.hhplus.be.server.interfaces.controller.token;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenType;
import kr.hhplus.be.server.interfaces.controller.token.dto.TokenResponse;
import kr.hhplus.be.server.common.error.ErrorResponse;
import kr.hhplus.be.server.domain.service.token.TokenService;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    private final TokenService tokenService;

    @Operation(summary = "토큰 조회", description = "", tags={ "Token API" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상적으로 토큰을 조회 했습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "404", description = "유효한 토큰을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @GetMapping(value = "/get")
    public ResponseEntity<Object> getToken(@RequestParam Long userId) {
        Token token = tokenService.getToken(userId, TokenStatus.WAIT);
        return ResponseEntity.ok(TokenResponse.of(token));
    }

    @Operation(summary = "Redis - 토큰 조회", description = "", tags={ "Token API" })
    @GetMapping(value = "/get/redis")
    public ResponseEntity<Object> getTokenWithRedis(@RequestParam Long userId) {
        String token = tokenService.getTokenWithRedis(userId, TokenType.WAITING);
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "토큰 발급", description = "", tags={ "Token API" })
    @ApiResponse(responseCode = "200", description = "정상적으로 토큰을 발급 했습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class)))
    @PostMapping("/issue")
    public TokenResponse issueToken(@RequestParam Long userId) {
        Token token = tokenService.issueWaitToken(userId);
        return TokenResponse.of(token);
    }

    @Operation(summary = "Redis - 토큰 발급", description = "", tags={ "Token API" })
    @ApiResponse(responseCode = "200", description = "정상적으로 토큰을 발급 했습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class)))
    @PostMapping("/issue/redis")
    public ResponseEntity<String> issueTokenWithRedis(@RequestParam Long userId) {
        String token = tokenService.issueWaitTokenWithRedis(userId);
        return ResponseEntity.ok(token);
    }
}
