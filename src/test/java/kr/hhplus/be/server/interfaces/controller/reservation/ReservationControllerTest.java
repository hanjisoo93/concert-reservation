package kr.hhplus.be.server.interfaces.controller.reservation;

import kr.hhplus.be.server.application.facade.reservation.ReservationFacade;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.domain.exception.reservation.ReservationException;
import kr.hhplus.be.server.domain.service.token.TokenService;
import kr.hhplus.be.server.interfaces.controller.reservation.dto.ReservationReserveRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@DisplayName("ReservationController 통합 테스트")
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationFacade reservationFacade;

    @MockitoBean
    private TokenService tokenService;

    private static final String BASE_URL = "/api/concert/reservation/reserve";

    @Test
    @DisplayName("유효한 요청으로 좌석 예약 성공")
    void reserveSeat_Success() throws Exception {
        // given
        String validToken = "valid-token";
        given(tokenService.isValidTokenByUuid(validToken)).willReturn(true);

        ReservationReserveRequest request = ReservationReserveRequest.builder()
                .userId(1L)
                .seatId(100L)
                .build();

        // when & then
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("좌석이 성공적으로 예약 요청 되었습니다."));
    }

    @Test
    @DisplayName("유효한 토큰이지만 좌석이 존재하지 않으면 404 반환")
    void reserveSeat_SeatNotFound() throws Exception {
        // given
        String validToken = "valid-token";
        given(tokenService.isValidTokenByUuid(validToken)).willReturn(true);

        ReservationReserveRequest request = ReservationReserveRequest.builder()
                .userId(1L)
                .seatId(999L)
                .build();
        doThrow(new ReservationException(ErrorCode.SEAT_NOT_FOUND))
                .when(reservationFacade)
                .reserve(request.getUserId(), request.getSeatId(), validToken);

        // when & then
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("이미 예약된 좌석을 예약하려 하면 400 반환")
    void reserveSeat_AlreadyReserved() throws Exception {
        // given
        String validToken = "valid-token";
        given(tokenService.isValidTokenByUuid(validToken)).willReturn(true);

        ReservationReserveRequest request = ReservationReserveRequest.builder()
                .userId(1L)
                .seatId(100L)
                .build();
        doThrow(new ReservationException(ErrorCode.SEAT_ALREADY_RESERVED))
                .when(reservationFacade)
                .reserve(request.getUserId(), request.getSeatId(), validToken);

        // when & then
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 요청 시 401 Unauthorized 반환")
    void reserveSeat_InvalidToken() throws Exception {
        // given
        String invalidToken = "invalid-token";
        given(tokenService.isValidTokenByUuid(invalidToken)).willReturn(false);

        ReservationReserveRequest request = ReservationReserveRequest.builder()
                .userId(1L)
                .seatId(100L)
                .build();

        // when & then
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", invalidToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}