package kr.hhplus.be.server.interfaces.controller.token;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import kr.hhplus.be.server.domain.exception.token.TokenException;
import kr.hhplus.be.server.domain.service.token.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
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

//    @Test
//    @DisplayName("토큰 조회 시 존재하는 토큰이면 성공")
//    void getToken_Success() throws Exception {
//        // Given
//        Long userId = 1L;
//        Token mockToken = Token.builder()
//                .userId(1L)
//                .uuid("test-token")
//                .status(TokenStatus.ACTIVE)
//                .expiredAt(LocalDateTime.now().plusMinutes(30))
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        when(tokenService.getToken(userId, TokenStatus.ACTIVE)).thenReturn(mockToken);
//
//        // When & Then
//        mockMvc.perform(get("/api/tokens/get", userId)
//                        .param("userId", "1")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.userId").value(userId))
//                .andExpect(jsonPath("$.uuid").value("test-token"))
//                .andExpect(jsonPath("$.status").value("ACTIVE"));
//    }

//    @Test
//    @DisplayName("토큰 조회 시 유효한 토큰이 없으면 404 반환")
//    void getToken_NotFound() throws Exception {
//        // Given
//        Long userId = 2L;
//        when(tokenService.getToken(userId, TokenStatus.ACTIVE))
//                .thenThrow(new TokenException(ErrorCode.TOKEN_NOT_FOUND));
//
//        // When & Then
//        mockMvc.perform(get("/api/tokens/get", userId)
//                        .param("userId", "2")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.error").value("유효한 토큰을 찾을 수 없습니다."));
//    }

    @Test
    @DisplayName("정상적으로 발급됨")
    void issueToken_Success() throws Exception {
        // Given
        Token mockToken = Token.builder()
                .userId(1L)
                .uuid("new-token")
                .status(TokenStatus.WAIT)
                .expiredAt(LocalDateTime.now().plusMinutes(30))
                .createdAt(LocalDateTime.now())
                .build();

        when(tokenService.issueWaitToken(1L)).thenReturn(mockToken);

        // When & Then
        mockMvc.perform(post("/api/tokens/issue")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.uuid").value("new-token"))
                .andExpect(jsonPath("$.status").value("WAIT"));
    }
}