package kr.hhplus.be.server.interfaces.controller.token;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import kr.hhplus.be.server.domain.entity.token.TokenType;
import kr.hhplus.be.server.interfaces.controller.token.dto.TokenResponse;
import kr.hhplus.be.server.common.error.ErrorResponse;
import kr.hhplus.be.server.domain.service.token.TokenService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<String> getToken(@RequestParam Long userId) {
        String token = tokenService.getToken(userId, TokenType.WAITING);
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "토큰 발급", description = "", tags={ "Token API" })
    @ApiResponse(responseCode = "200", description = "정상적으로 토큰을 발급 했습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class)))
    @PostMapping("/issue")
    public ResponseEntity<String> issueToken(@RequestParam Long userId) {
        String token = tokenService.issueWaitToken(userId);
        return ResponseEntity.ok(token);
    }
}
