package kr.hhplus.be.server.domain.event;

import kr.hhplus.be.server.domain.entity.reservation.Reservation;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReservationCompletedEvent extends ApplicationEvent {
    private final Reservation reservation;

    public ReservationCompletedEvent(Reservation reservation) {
        super(reservation);
        this.reservation = reservation;
    }
}
