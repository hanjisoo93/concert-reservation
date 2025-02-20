package kr.hhplus.be.server.interfaces.api.controller.token.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TokenResponse {
    private TokenStatus status;
    private Double score;
}
