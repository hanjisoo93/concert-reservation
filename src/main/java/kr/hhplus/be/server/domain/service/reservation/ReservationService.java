package kr.hhplus.be.server.domain.service.reservation;

import kr.hhplus.be.server.infra.repository.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

}
