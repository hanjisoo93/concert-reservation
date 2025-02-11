package kr.hhplus.be.server.interfaces.controller.token;

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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
//        // given
//        Long userId = 1L;
//        when(tokenService.issueWaitToken(userId)).thenReturn("EXISTING_TOKEN");
//
//        // when & then
//        mockMvc.perform(get("/api/tokens/get")
//                        .param("userId", userId.toString())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string("EXISTING_TOKEN"));
//    }
//
//    @Test
//    @DisplayName("토큰 조회 시 유효한 토큰이 없으면 404 반환")
//    void getToken_NotFound() throws Exception {
//        // given
//        Long userId = 2L;
//
//        // when & then
//        mockMvc.perform(get("/api/tokens/get")
//                        .param("userId", userId.toString())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound())
//                .andExpect(content().string("유효한 토큰을 찾을 수 없습니다."));
//    }

    @Test
    @DisplayName("정상적으로 발급됨")
    void issueToken_Success() throws Exception {
        // given
        Long userId = 1L;
        when(tokenService.issueWaitToken(userId)).thenReturn("NEW_WAIT_TOKEN");

        // when & then
        mockMvc.perform(post("/api/tokens/issue")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("NEW_WAIT_TOKEN"));
    }
}