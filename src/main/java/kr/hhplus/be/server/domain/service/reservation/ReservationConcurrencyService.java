package kr.hhplus.be.server.domain.service.reservation;

import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.domain.entity.reservation.Reservation;
import kr.hhplus.be.server.domain.entity.reservation.ReservationOptimistic;
import kr.hhplus.be.server.domain.entity.reservation.ReservationStatus;
import kr.hhplus.be.server.infra.repository.reservation.ReservationOptimisticRepository;
import kr.hhplus.be.server.infra.repository.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReservationConcurrencyService {

    private final ReservationRepository reservationRepository;
    private final ReservationOptimisticRepository reservationOptimisticRepository;

    /**
     * 비관적 락 기반
     */
    @Transactional
    public void createReservation(Long userId, Long seatId) {
        List<ReservationStatus> activeStatuses = List.of(ReservationStatus.PENDING, ReservationStatus.SUCCESS);

        // 1. 좌석 예약 여부 조회
        Optional<Reservation> existingReservation = reservationRepository.findReservationBySeatIdForUpdate(seatId, activeStatuses);

        if (existingReservation.isEmpty()) {
            // 2. 예약 생성
            Reservation reservation = Reservation.createReservation(userId, seatId);
            reservationRepository.save(reservation);

            log.info("예약 생성 완료 - reservationId={}, userId={}, seatId={}", reservation.getId(), userId, seatId);
        }
    }

    /**
     * 낙관적 락 기반
     */
    @Retryable(
            value = { DataIntegrityViolationException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    @Transactional
    public void createReservationWithOptimisticLock(Long userId, Long seatId) {
        List<ReservationStatus> activeStatuses = List.of(ReservationStatus.PENDING, ReservationStatus.SUCCESS);

        // 1. 좌석 예약 여부 조회
        Optional<ReservationOptimistic> existingReservation = reservationOptimisticRepository.findReservationBySeatIdAndStatues(seatId, activeStatuses);

        if (existingReservation.isEmpty()) {
            // 2. 예약 생성
            ReservationOptimistic reservation = ReservationOptimistic.createReservation(userId, seatId);
            reservationOptimisticRepository.save(reservation);

            log.info("예약 생성 완료 - reservationId={}, userId={}, seatId={}", reservation.getId(), userId, seatId);
        }
    }

    /**
     * 분산 락 기반 : Redis > Redisson
     */
    @DistributedLock(key = "#seatId")
    public void createReservationWithRedissonLock(Long userId, Long seatId) {
        List<ReservationStatus> activeStatuses = List.of(ReservationStatus.PENDING, ReservationStatus.SUCCESS);

        // 좌석 예약 여부 조회
        Optional<Reservation> existingReservation = reservationRepository.findReservation(seatId, activeStatuses);

        if (existingReservation.isEmpty()) {
            // 예약 생성
            Reservation reservation = Reservation.createReservation(userId, seatId);
            reservationRepository.save(reservation);

            log.info("예약 생성 완료 - reservationId={}, userId={}, seatId={}", reservation.getId(), userId, seatId);
        }
    }
}
