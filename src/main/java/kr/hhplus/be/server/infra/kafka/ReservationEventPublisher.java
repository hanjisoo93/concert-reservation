package kr.hhplus.be.server.infra.kafka;

import kr.hhplus.be.server.domain.outbox.OutboxEventType;
import kr.hhplus.be.server.domain.outbox.OutboxStatus;
import kr.hhplus.be.server.domain.outbox.ReservationOutbox;
import kr.hhplus.be.server.domain.outbox.ReservationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ReservationOutboxRepository outboxRepository;

    private static final String TOPIC_RESERVATION = "reservation-complete";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishPendingReservationCompletedEvent() {
        List<ReservationOutbox> pendingEvents = outboxRepository.findByStatusAndEventType(
                OutboxStatus.PENDING, OutboxEventType.RESERVATION_COMPLETED);

        if (pendingEvents.isEmpty()) {
            return;
        }

        for (ReservationOutbox event : pendingEvents) {
            try {
                kafkaTemplate.send(TOPIC_RESERVATION, event.getId())
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                event.setStatus(OutboxStatus.FAILED);
                                outboxRepository.save(event);
                            }
                        });
            } catch (Exception e) {
                event.setStatus(OutboxStatus.FAILED);
                outboxRepository.save(event);
            }
        }

    }
}
