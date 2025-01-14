package kr.hhplus.be.server.domain.service.reservation;

import kr.hhplus.be.server.domain.entity.reservation.Reservation;
import kr.hhplus.be.server.domain.entity.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.exception.reservation.ReservationException;
import kr.hhplus.be.server.infra.repository.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public void validateSeatReservation(Long seatId) {
        List<ReservationStatus> activeStatuses = List.of(ReservationStatus.PENDING, ReservationStatus.SUCCESS);
        boolean isReserved = reservationRepository.existsSeatReservation(seatId, activeStatuses);

        if (isReserved) {
            throw new ReservationException("좌석 ["+ seatId + "]은 이미 예약된 좌석입니다.");
        }
    }

    @Transactional
    public void createReservation(Long userId, Long seatId) {
        Reservation reservation = Reservation.createReservation(userId, seatId);
        reservationRepository.save(reservation);
    }
}
