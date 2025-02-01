package kr.hhplus.be.server.interfaces.controller.reservation;

import kr.hhplus.be.server.domain.service.reservation.ReservationConcurrencyService;
import kr.hhplus.be.server.interfaces.controller.reservation.dto.ReservationReserveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reservation")
public class ReservationConcurrencyController {

    private final ReservationConcurrencyService reservationConcurrencyService;

    @PostMapping("/optimistic")
    public ResponseEntity<String> reserveSeatWithOptimisticLock(@RequestBody ReservationReserveRequest request) {
        reservationConcurrencyService.createReservationWithOptimisticLock(request.getUserId(), request.getSeatId());
        return ResponseEntity.status(HttpStatus.CREATED).body("Reservation with Optimistic Lock successful");
    }

    @PostMapping("/pessimistic")
    public ResponseEntity<String> reserveSeatWithPessimisticLock(@RequestBody ReservationReserveRequest request) {
        reservationConcurrencyService.createReservation(request.getUserId(), request.getSeatId());
        return ResponseEntity.status(HttpStatus.CREATED).body("Reservation with Pessimistic Lock successful");
    }

    @PostMapping("/distributed")
    public ResponseEntity<String> reserveSeatWithDistributedLock(@RequestBody ReservationReserveRequest request) {
        reservationConcurrencyService.createReservationWithRedissonLock(request.getUserId(), request.getSeatId());
        return ResponseEntity.status(HttpStatus.CREATED).body("Reservation with Distributed Lock successful");
    }
}
