package kr.hhplus.be.server.domain.event.listener;

import kr.hhplus.be.server.domain.event.ReservationCompletedEvent;
import kr.hhplus.be.server.domain.outbox.OutboxEventType;
import kr.hhplus.be.server.domain.outbox.OutboxStatus;
import kr.hhplus.be.server.domain.outbox.ReservationOutbox;
import kr.hhplus.be.server.domain.outbox.ReservationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventListener {

    private final ReservationOutboxRepository outboxRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleReservationCompletedEventBeforeCommit() {
        ReservationOutbox outbox = new ReservationOutbox();
        outbox.setEventType(OutboxEventType.RESERVATION_COMPLETED);

        try {
            outbox.setStatus(OutboxStatus.PENDING);
            outboxRepository.save(outbox);
        } catch (Exception e) {
            outbox.setStatus(OutboxStatus.FAILED);
            outboxRepository.save(outbox);
        }
    }
}
