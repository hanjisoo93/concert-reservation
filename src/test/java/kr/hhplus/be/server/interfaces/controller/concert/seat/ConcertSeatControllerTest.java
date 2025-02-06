package kr.hhplus.be.server.interfaces.controller.concert.seat;

import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.service.concert.seat.ConcertSeatService;
import kr.hhplus.be.server.domain.service.token.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ConcertSeat API + Interceptor 테스트")
class ConcertSeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConcertSeatService concertSeatService;

    @MockitoBean
    private TokenService tokenService;

    @Test
    @DisplayName("유효한 토큰으로 좌석 조회 성공")
    void getConcertSeats_Success() throws Exception {
        given(tokenService.isValidToken(1L)).willReturn(true);
        ConcertSeat mockConcertSeat1 = ConcertSeat.builder()
                .id((1L))
                .concertScheduleId(1L)
                .seatNumber(25)
                .price(50000)
                .build();

        ConcertSeat mockConcertSeat2 = ConcertSeat.builder()
                .id((2L))
                .concertScheduleId(1L)
                .seatNumber(26)
                .price(50000)
                .build();

        List<ConcertSeat> mockSeats = List.of(mockConcertSeat1, mockConcertSeat2);

        given(concertSeatService.getConcertSeats(1L)).willReturn(mockSeats);

        mockMvc.perform(get("/api/concert/seats")
                        .param("concertScheduleId", "1")
                        .header("Authorization", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("유효한 토큰이지만 조회된 좌석이 없을 때 404 반환")
    void getConcertSeats_NotFound() throws Exception {
        given(tokenService.isValidToken(1L)).willReturn(true);
        given(concertSeatService.getConcertSeats(1L)).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/concert/seats")
                        .param("concertScheduleId", "1")
                        .header("Authorization", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("해당 날짜에 콘서트 좌석이 없습니다."));
    }

    @Test
    @DisplayName("유효한 토큰으로 콘서트 좌석 상세 조회 성공")
    void getConcertSeat_Success() throws Exception {
        given(tokenService.isValidToken(1L)).willReturn(true);
        ConcertSeat mockSeat = ConcertSeat.builder()
                .id((1L))
                .concertScheduleId(1L)
                .seatNumber(10)
                .price(50000)
                .build();
        given(concertSeatService.getConcertSeat(1L)).willReturn(mockSeat);

        mockMvc.perform(get("/api/concert/seats/1").header("Authorization", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seatNumber").value(10))
                .andExpect(jsonPath("$.price").value(50000));
    }

    @Test
    @DisplayName("좌석 상세 조회 시 존재하지 않는 좌석일 때 404 반환")
    void getConcertSeat_NotFound() throws Exception {
        given(tokenService.isValidToken(1L)).willReturn(true);
        given(concertSeatService.getConcertSeat(1L)).willReturn(null);

        mockMvc.perform(get("/api/concert/seats/1").header("Authorization", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("해당 좌석을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 좌석 목록 조회 시 401 Unauthorized 반환")
    void getConcertSeats_Unauthorized() throws Exception {
        given(tokenService.isValidToken(1L)).willReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/concert/seats")
                        .param("concertScheduleId", "1")
                        .header("Authorization", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("토큰 없이 요청 시 401 Unauthorized 반환")
    void getConcertSeats_NoToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/concert/seats")
                        .param("concertScheduleId", "1"))
                .andExpect(status().isUnauthorized());
    }
}