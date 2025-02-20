package kr.hhplus.be.server.interfaces.controller.payment;

import kr.hhplus.be.server.application.facade.payment.PaymentFacade;
import kr.hhplus.be.server.domain.service.token.TokenService;
import kr.hhplus.be.server.interfaces.api.controller.payment.dto.PaymentRequest;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Payment API + Interceptor 테스트")
class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentFacade paymentFacade;

    @MockitoBean
    private TokenService tokenService;

    private static final String BASE_URL = "/api/payment";

    @Test
    @DisplayName("유효한 토큰이면 결제 성공")
    void processPayment_Success_WithValidToken() throws Exception {
        // given
        PaymentRequest request = PaymentRequest.builder()
                .userId(1L)
                .reservationId(1L)
                .amount(50000)
                .build();
        String requestBody = new ObjectMapper().writeValueAsString(request);

        // when
        when(tokenService.isValidToken(1L)).thenReturn(true);

        // then
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("결제가 성공적으로 처리되었습니다."));

        verify(paymentFacade, times(1)).paymentProcess(1L);
    }

    @Test
    @DisplayName("토큰이 없으면 401 반환")
    void processPayment_Fail_Unauthorized_NoToken() throws Exception {
        // given
        PaymentRequest request = PaymentRequest.builder()
                .userId(1L)
                .reservationId(1L)
                .amount(50000)
                .build();
        String requestBody = new ObjectMapper().writeValueAsString(request);

        // when & then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 401 반환")
    void processPayment_Fail_Unauthorized_InvalidToken() throws Exception {
        // given
        PaymentRequest request = PaymentRequest.builder()
                .userId(1L)
                .reservationId(1L)
                .amount(50000)
                .build();
        String requestBody = new ObjectMapper().writeValueAsString(request);

        // when
        when(tokenService.isValidToken(1L)).thenReturn(false);

        // then
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }
}