package kr.hhplus.be.server.application.facade.reservation.concurrency.distributed.redis;

import kr.hhplus.be.server.application.facade.reservation.ReservationFacade;
import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.entity.reservation.Reservation;
import kr.hhplus.be.server.domain.entity.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import kr.hhplus.be.server.infra.repository.concert.seat.ConcertSeatRepository;
import kr.hhplus.be.server.infra.repository.reservation.ReservationRepository;
import kr.hhplus.be.server.infra.repository.token.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("좌석 예약 요청 - Redisson AOP 기반 분산 락 동시성 테스트")
public class ReservationFacadeRedissonLockTest {

    @Autowired
    private ReservationFacade reservationFacade;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @BeforeEach
    void tearDown() {
        reservationRepository.deleteAllInBatch();
        concertSeatRepository.deleteAllInBatch();
        tokenRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("단일 사용자가 정상적으로 좌석을 예약할 수 있어야 한다.")
    void testSingleUserReservationSuccess() {
        // given
        ConcertSeat mockConcertSeat = ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(25)
                .price(50000)
                .build();
        ConcertSeat savedConcertSeat = concertSeatRepository.save(mockConcertSeat);

        Token mockToken = Token.builder()
                .userId(1L)
                .status(TokenStatus.ACTIVE)
                .expiredAt(LocalDateTime.now().plusMinutes(30))
                .createdAt(LocalDateTime.now())
                .build();
        Token savedToken = tokenRepository.save(mockToken);

        Long userId = 1L;
        Long seatId = savedConcertSeat.getId();

        // when & then
        assertDoesNotThrow(() -> reservationFacade.reserveWithRedissonLock(userId, mockConcertSeat.getId(), savedToken.getUuid()));

        Reservation reservation = reservationRepository.findAll().get(0);
        assertThat(reservation).isNotNull();
        assertThat(reservation.getUserId()).isEqualTo(userId);
        assertThat(reservation.getSeatId()).isEqualTo(seatId);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);

        Token updatedToken = tokenRepository.findAllByUserIdAndStatus(userId, TokenStatus.ACTIVE);
        assertThat(updatedToken.getExpiredAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("여러 사용자가 서로 다른 좌석을 동시에 예약할 경우 모두 성공해야 한다.")
    void testMultipleUsersReserveDifferentSeats() throws InterruptedException {
        // given
        ConcertSeat mockConcertSeat1 = createAndSaveConcertSeat(1L, 25, 50000);
        ConcertSeat mockConcertSeat2 = createAndSaveConcertSeat(1L, 38, 50000);

        Token mockToken1 = createAndSaveToken(1L);
        Token mockToken2 = createAndSaveToken(2L);

        Long seatId1 = mockConcertSeat1.getId();
        Long seatId2 = mockConcertSeat2.getId();

        // when
        List<Callable<Boolean>> tasks = List.of(
                () -> tryReserveSeat(1L, seatId1, mockToken1.getUuid()),
                () -> tryReserveSeat(2L, seatId2, mockToken2.getUuid())
        );

        int successCount = executeConcurrentTasks(tasks);

        // then
        assertEquals(2, successCount); // 모든 예약이 성공해야 함

        List<Reservation> reservations = reservationRepository.findAll();
        assertEquals(2, reservations.size());

        assertReservation(reservations, seatId1, 1L);
        assertReservation(reservations, seatId2, 2L);
    }

    @Test
    @DisplayName("여러 사용자가 동일한 좌석을 동시에 예약할 경우 단 하나만 성공해야 한다.")
    void testMultipleUsersReserveSameSeat() throws InterruptedException, ExecutionException {
        // given
        ConcertSeat mockConcertSeat = createAndSaveConcertSeat(1L, 58, 100000);

        Token mockToken1 = createAndSaveToken(1L);
        Token mockToken2 = createAndSaveToken(2L);
        Token mockToken3 = createAndSaveToken(3L);

        Long seatId = mockConcertSeat.getId();

        List<Callable<Boolean>> tasks = createConcurrentTasks(
                () -> tryReserveSeat(1L, seatId, mockToken1.getUuid()),
                () -> tryReserveSeat(2L, seatId, mockToken2.getUuid()),
                () -> tryReserveSeat(3L, seatId, mockToken3.getUuid())
        );

        // When
        long successCount = executeConcurrentTasks(tasks);

        // Then
        assertEquals(1, successCount); // 단 하나의 예약만 성공해야 함
    }


    /////// Helper Method ///////
    private ConcertSeat createAndSaveConcertSeat(Long scheduleId, int seatNumber, int price) {
        ConcertSeat seat = ConcertSeat.builder()
                .concertScheduleId(scheduleId)
                .seatNumber(seatNumber)
                .price(price)
                .build();
        return concertSeatRepository.save(seat);
    }

    @SafeVarargs
    private final List<Callable<Boolean>> createConcurrentTasks(Callable<Boolean>... tasks) {
        return Arrays.asList(tasks);
    }

    private Token createAndSaveToken(Long userId) {
        Token token = Token.builder()
                .userId(userId)
                .uuid(java.util.UUID.randomUUID().toString())
                .status(TokenStatus.ACTIVE)
                .expiredAt(LocalDateTime.now().plusMinutes(30))
                .createdAt(LocalDateTime.now())
                .build();
        return tokenRepository.save(token);
    }

    private void assertReservation(List<Reservation> reservations, Long seatId, Long userId) {
        Reservation reservation = reservations.stream()
                .filter(r -> r.getSeatId().equals(seatId))
                .findFirst()
                .orElse(null);

        assertNotNull(reservation);
        assertEquals(userId, reservation.getUserId());
        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
    }

    private int executeConcurrentTasks(List<Callable<Boolean>> tasks) throws InterruptedException {
        int threadCount = tasks.size();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (Callable<Boolean> task : tasks) {
            executorService.execute(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    if (task.call()) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        finishLatch.await();
        executorService.shutdown();

        return successCount.get();
    }

    private boolean tryReserveSeat(Long userId, Long seatId, String uuid) {
        try {
            reservationFacade.reserveWithRedissonLock(userId, seatId, uuid);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
