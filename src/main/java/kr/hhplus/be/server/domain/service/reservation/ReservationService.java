package kr.hhplus.be.server.domain.service.reservation;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.SystemException;
import kr.hhplus.be.server.domain.entity.reservation.Reservation;
import kr.hhplus.be.server.domain.entity.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.exception.reservation.ReservationException;
import kr.hhplus.be.server.infra.repository.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public void validateSeatReservation(Long seatId) {
        try {
            List<ReservationStatus> activeStatuses = List.of(ReservationStatus.PENDING, ReservationStatus.SUCCESS);
            boolean isReserved = reservationRepository.existsSeatReservation(seatId, activeStatuses);

            if (isReserved) {
                log.warn("좌석 예약 실패 - 이미 예약된 좌석: seatId={}", seatId);
                throw new ReservationException(ErrorCode.SEAT_ALREADY_RESERVED);
            }

            log.info("좌석 예약 가능 - seatId={}", seatId);
        } catch (ReservationException e) {
            log.warn("좌석 예약 검증 실패 - seatId={}, error={}", seatId, e.getErrorCode().getMessage());
            throw e;
        } catch (Exception e) {
            log.error("좌석 예약 검증 중 시스템 오류 발생 - seatId={}", seatId, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public Reservation validateReservation(Long reservationId) {
        try {
            Reservation reservation = reservationRepository.findReservationById(reservationId)
                    .orElseThrow(() -> new ReservationException(ErrorCode.RESERVATION_NOT_FOUND));

            if (reservation.isExpired(reservation.getExpiredAt())) {
                log.warn("예약 시간 만료 - reservationId={}, expiredAt={}", reservationId, reservation.getExpiredAt());
                throw new ReservationException(ErrorCode.RESERVATION_EXPIRED);
            }

            if(ReservationStatus.SUCCESS.equals(reservation.getStatus())) {
                log.warn("이미 결제가 완료된 예약 - reservationId={}, status={}", reservationId, reservation.getStatus());
                throw new ReservationException(ErrorCode.RESERVATION_ALREADY_PAID);
            }

            log.info("유효한 예약 확인 - reservationId={}", reservationId);
            return reservation;
        } catch (ReservationException e) {
            log.warn("예약 검증 실패 - reservationId={}", reservationId, e);
            throw e;
        } catch (Exception e) {
            log.error("예약 검증 중 시스템 오류 발생 - reservationId={}", reservationId, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Retryable(
            value = { DataIntegrityViolationException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    @Transactional
    public void createReservation(Long userId, Long seatId) {
        try {
            List<ReservationStatus> activeStatuses = List.of(ReservationStatus.PENDING, ReservationStatus.SUCCESS);

            Optional<Reservation> existingReservation = reservationRepository.findReservationBySeatIdAndStatues(seatId, activeStatuses);

            if (existingReservation.isPresent()) {
                throw new ReservationException(ErrorCode.SEAT_ALREADY_RESERVED);
            }

            Reservation reservation = Reservation.createReservation(userId, seatId);
            reservationRepository.save(reservation);
        } catch (ReservationException e){
            throw e;
        } catch (DataIntegrityViolationException e) {
            throw new ReservationException(ErrorCode.SEAT_ALREADY_RESERVED);
        } catch (Exception e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional
    public void updateReservationStatus(Long reservationId, ReservationStatus status) {
        try {
            Reservation reservation = reservationRepository.findReservationById(reservationId)
                    .orElseThrow(() -> new ReservationException(ErrorCode.RESERVATION_NOT_FOUND));

            reservation.updateStatus(status);
        } catch (ReservationException e) {
            throw e;
        } catch (Exception e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional
    public void expirePendingReservation(){
        try {
            List<Reservation> expiredReservations = reservationRepository.findAllByExpiredAtBeforeAndStatus(
                    LocalDateTime.now(), ReservationStatus.PENDING);

            if (expiredReservations.isEmpty()) {
                log.info("만료 처리할 예약 없음");
                return;
            }

            expiredReservations.forEach(reservation -> {
                if (reservation.getStatus() == ReservationStatus.PENDING) {
                    reservation.updateStatus(ReservationStatus.FAILED);
                }
            });

            reservationRepository.saveAll(expiredReservations);
            log.info("만료된 예약 상태 업데이트 완료 - count={}", expiredReservations.size());

        } catch (Exception e) {
            log.error("예약 만료 처리 중 시스템 오류 발생", e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
