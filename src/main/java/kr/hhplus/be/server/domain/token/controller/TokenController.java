package kr.hhplus.be.server.domain.token.controller;

import kr.hhplus.be.server.domain.token.dto.TokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tokens")
public class TokenController {
    private final List<TokenResponse> tokens = new ArrayList<>();

    public TokenController() {
        // 공통 Mock 데이터 초기화
        tokens.add(new TokenResponse(1, "mock-uuid-12345", "ACTIVE"));
        tokens.add(new TokenResponse(2, "mock-uuid-67890", "EXPIRED"));
    }

    /**
     * 토큰 생성
     */
    @PostMapping
    public ResponseEntity<TokenResponse> createToken() {
        // 새로운 토큰 생성
        TokenResponse newToken = new TokenResponse(tokens.size() + 1, "mock-uuid-" + (tokens.size() + 1), "ACTIVE");
        tokens.add(newToken);

        return ResponseEntity.ok(newToken);
    }

    /**
     * 토큰 조회
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<TokenResponse> getToken(@PathVariable String uuid) {
        // UUID로 토큰 조회
        Optional<TokenResponse> token = tokens.stream()
                .filter(t -> t.getUuid().equals(uuid))
                .findFirst();

        return token.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 토큰 만료 처리
     */
    @PostMapping("/{uuid}/expire")
    public ResponseEntity<TokenResponse> expireToken(@PathVariable String uuid) {
        for (TokenResponse token : tokens) {
            if (token.getUuid().equals(uuid) && token.getStatus().equals("ACTIVE")) {
                // 상태 변경
                token.setStatus("EXPIRED");
                return ResponseEntity.ok(token);
            }
        }
        return ResponseEntity.badRequest().build();
    }
}
