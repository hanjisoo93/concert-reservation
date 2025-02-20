package kr.hhplus.be.server.interfaces.controller.token;

import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import kr.hhplus.be.server.domain.entity.token.TokenType;
import kr.hhplus.be.server.domain.service.token.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Token API 테스트")
class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenService tokenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("토큰 조회 - 토큰이 존재하는 경우")
    void testGetToken_existing() throws Exception {
        // given
        Long userId = 1L;
        Token existingToken = new Token(TokenStatus.WAIT, (double) System.currentTimeMillis());
        given(tokenService.getToken(userId, TokenType.WAITING)).willReturn(existingToken);

        // when & then
        mockMvc.perform(get("/api/tokens/get")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(existingToken.getStatus().name()))
                .andExpect(jsonPath("$.score").value(existingToken.getScore()));
    }

    @Test
    @DisplayName("토큰 발급 - 신규 토큰 발급되는 경우")
    void testIssueToken_new() throws Exception {
        // given
        Long userId = 2L;
        Token newToken = new Token(TokenStatus.WAIT, (double) System.currentTimeMillis());
        given(tokenService.issueWaitToken(userId)).willReturn(newToken);

        // when & then
        mockMvc.perform(post("/api/tokens/issue")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(newToken.getStatus().name()))
                .andExpect(jsonPath("$.score").value(newToken.getScore()));
    }
}