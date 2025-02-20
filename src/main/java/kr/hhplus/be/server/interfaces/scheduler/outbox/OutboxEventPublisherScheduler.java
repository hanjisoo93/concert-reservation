package kr.hhplus.be.server.interfaces.scheduler.outbox;

import kr.hhplus.be.server.domain.outbox.OutboxEventType;
import kr.hhplus.be.server.domain.outbox.OutboxStatus;
import kr.hhplus.be.server.domain.outbox.ReservationOutbox;
import kr.hhplus.be.server.domain.outbox.ReservationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisherScheduler {
    private final ReservationOutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_RESERVATION = "reservation-notification";

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void republishReservationKafkaEvents() {

        List<ReservationOutbox> targetEvents = new ArrayList<>();

        targetEvents.addAll(outboxRepository.findByStatusAndEventType(OutboxStatus.PENDING, OutboxEventType.RESERVATION_COMPLETED));
        targetEvents.addAll(outboxRepository.findByStatusAndEventType(OutboxStatus.FAILED, OutboxEventType.RESERVATION_COMPLETED));

        if(targetEvents.isEmpty()) return;

        for (ReservationOutbox event : targetEvents) {
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
