package kr.hhplus.be.server.interfaces.controller.concert.schedule;

import kr.hhplus.be.server.domain.service.concert.schedule.ConcertScheduleService;
import kr.hhplus.be.server.domain.service.token.TokenService;
import kr.hhplus.be.server.interfaces.controller.concert.schedule.dto.ConcertScheduleResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Concert Schedule API + Interceptor 테스트")
class ConcertScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConcertScheduleService concertScheduleService;

    @MockitoBean
    private TokenService tokenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("콘서트 스케줄 조회 - 유효한 토큰이면 성공")
    void getConcertSchedules_Success() throws Exception {
        // given
        Long concertId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        ConcertScheduleResponse mockResponse1 = ConcertScheduleResponse.builder()
                .id(1L)
                .concertId(1L)
                .concertDate(LocalDate.of(2025, 5, 10))
                .build();
        ConcertScheduleResponse mockResponse2 = ConcertScheduleResponse.builder()
                .id(2L)
                .concertId(3L)
                .concertDate(LocalDate.of(2025, 6, 15))
                .build();

        List<ConcertScheduleResponse> schedules = List.of(mockResponse1, mockResponse2);
        Page<ConcertScheduleResponse> pageResponse = new PageImpl<>(schedules, pageable, schedules.size());

        // when
        when(tokenService.isValidToken(1L)).thenReturn(true);
        when(concertScheduleService.getConcertSchedules(concertId, pageable)).thenReturn(pageResponse);

        // then
        mockMvc.perform(get("/api/concert/schedule/{concertId}", concertId)
                        .header("Authorization", "1")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].concertId").value(1L))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].concertId").value(3L));
    }

    @Test
    @DisplayName("콘서트 스케줄 조회 - 스케줄이 없으면 빈 목록 반환")
    void getConcertSchedules_EmptyList() throws Exception {
        // Given
        Long concertId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<ConcertScheduleResponse> emptyPage = Page.empty(pageable);

        when(tokenService.isValidToken(1L)).thenReturn(true);
        when(concertScheduleService.getConcertSchedules(concertId, pageable)).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/concert/schedule/{concertId}", concertId)
                        .header("Authorization", "1")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("콘서트 스케줄 조회 - 토큰이 없으면 401 반환")
    void getConcertSchedules_Fail_Unauthorized_NoToken() throws Exception {
        // Given
        Long concertId = 1L;

        // When & Then
        mockMvc.perform(get("/api/concert/schedule/{concertId}", concertId)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("콘서트 스케줄 조회 - 유효하지 않은 토큰이면 401 반환")
    void getConcertSchedules_Fail_Unauthorized_InvalidToken() throws Exception {
        // Given
        Long concertId = 1L;
        when(tokenService.isValidToken(1L)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/concert/schedule/{concertId}", concertId)
                        .header("Authorization", "1")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}