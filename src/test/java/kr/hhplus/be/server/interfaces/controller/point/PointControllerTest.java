package kr.hhplus.be.server.interfaces.controller.point;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.domain.entity.point.Point;
import kr.hhplus.be.server.domain.entity.point.PointChangeType;
import kr.hhplus.be.server.domain.entity.point.PointHistory;
import kr.hhplus.be.server.domain.exception.point.PointException;
import kr.hhplus.be.server.domain.service.point.PointHistoryService;
import kr.hhplus.be.server.domain.service.point.PointService;
import kr.hhplus.be.server.domain.service.token.TokenService;
import kr.hhplus.be.server.interfaces.controller.point.dto.PointRequest;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Point API + Interceptor 테스트")
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PointService pointService;

    @MockitoBean
    private PointHistoryService pointHistoryService;

    @MockitoBean
    private TokenService tokenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("유효한 토큰으로 포인트 조회 성공")
    void getPoint_Success() throws Exception {
        // given
        given(tokenService.isValidTokenByUuid("valid-token")).willReturn(true);

        Point mockPoint = Point.builder()
                .userId(1L)
                .amount(50000)
                .build();

        given(pointService.getPoint(1L)).willReturn(mockPoint);

        // when & then
        mockMvc.perform(get("/api/point/1")
                        .header("Authorization", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.amount").value(50000));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 포인트 조회 시 404 반환")
    void getPoint_NotFound() throws Exception {
        // given
        given(tokenService.isValidTokenByUuid("valid-token")).willReturn(true);
        given(pointService.getPoint(1L)).willThrow(new PointException(ErrorCode.POINT_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/point/1")
                        .header("Authorization", "valid-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("포인트 정보가 존재하지 않습니다."));
    }
    @Test
    @DisplayName("유효한 토큰으로 포인트 내역 조회 성공")
    void getPointHistory_Success() throws Exception {
        // given
        given(tokenService.isValidTokenByUuid("valid-token")).willReturn(true);

        PointHistory mockPointHistory1 = PointHistory.builder()
                .userId(1L)
                .changeAmount(10000)
                .pointAfterAmount(50000)
                .changeType(PointChangeType.WITHDRAWAL)
                .createdAt(LocalDateTime.now())
                .build();
        PointHistory mockPointHistory2 = PointHistory.builder()
                .userId(1L)
                .changeAmount(20000)
                .pointAfterAmount(60000)
                .changeType(PointChangeType.DEPOSIT)
                .createdAt(LocalDateTime.now())
                .build();

        List<PointHistory> mockHistory = List.of(mockPointHistory1, mockPointHistory2);

        given(pointHistoryService.getPointHistories(1L)).willReturn(mockHistory);

        // when & then
        mockMvc.perform(get("/api/point/history/1")
                        .header("Authorization", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].changeAmount").value(10000))
                .andExpect(jsonPath("$[0].changeType").value("WITHDRAWAL"))
                .andExpect(jsonPath("$[1].changeAmount").value(20000))
                .andExpect(jsonPath("$[1].changeType").value("DEPOSIT"));
    }

    @Test
    @DisplayName("포인트 내역이 없을 때 404 반환")
    void getPointHistory_NotFound() throws Exception {
        // given
        given(tokenService.isValidTokenByUuid("valid-token")).willReturn(true);
        given(pointHistoryService.getPointHistories(1L)).willThrow(new PointException(ErrorCode.POINT_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/point/history/1")
                        .header("Authorization", "valid-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("포인트 정보가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("유효한 토큰으로 포인트 충전 성공")
    void addPoint_Success() throws Exception {
        // given
        given(tokenService.isValidTokenByUuid("valid-token")).willReturn(true);
        PointRequest request = PointRequest.builder()
                .userId(1L)
                .amount(10000)
                .build();

        // when & then
        mockMvc.perform(post("/api/point/add")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("포인트 충전이 완료되었습니다."));
    }

    @Test
    @DisplayName("유효한 토큰으로 포인트 사용 성공")
    void usePoint_Success() throws Exception {
        // given
        given(tokenService.isValidTokenByUuid("valid-token")).willReturn(true);
        PointRequest request = PointRequest.builder()
                .userId(1L)
                .amount(5000)
                .build();

        // when & then
        mockMvc.perform(post("/api/point/use")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("포인트 사용이 완료되었습니다."));
    }

    @Test
    @DisplayName("포인트 잔액 부족 시 400 반환")
    void usePoint_InsufficientBalance() throws Exception {
        // given
        given(tokenService.isValidTokenByUuid("valid-token")).willReturn(true);
        doThrow(new PointException(ErrorCode.INSUFFICIENT_POINT)).when(pointService).usePoint(1L, 50000);
        PointRequest request = PointRequest.builder()
                .userId(1L)
                .amount(50000)
                .build();

        // when & then
        mockMvc.perform(post("/api/point/use")
                        .header("Authorization", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("포인트가 부족합니다."));
    }
}