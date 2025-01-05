package kr.hhplus.be.server.domain.token.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private Integer id;
    private String uuid;
    private String status;
}
