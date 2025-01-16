package kr.hhplus.be.server.interfaces.scheduler.reservation;

import kr.hhplus.be.server.domain.service.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReservationScheduler {

    private final ReservationService reservationService;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void processExpiredReservations() {
        reservationService.expirePendingReservation();
    }
}
