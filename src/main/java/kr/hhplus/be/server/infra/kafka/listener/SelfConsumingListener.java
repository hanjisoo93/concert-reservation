package kr.hhplus.be.server.infra.kafka.listener;

import kr.hhplus.be.server.domain.outbox.OutboxStatus;
import kr.hhplus.be.server.domain.outbox.ReservationOutbox;
import kr.hhplus.be.server.domain.outbox.ReservationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SelfConsumingListener {

    private final ReservationOutboxRepository outboxRepository;

    @KafkaListener(topics = "reservation-complete", groupId = "reservation-complete-self")
    public void updateReservationCompleteMessage(@Payload Long id) {
        ReservationOutbox outbox = outboxRepository.findByIdAndStatus(id, OutboxStatus.PENDING);
        if (outbox != null) {
            outbox.setStatus(OutboxStatus.SENT);
            outboxRepository.save(outbox);
        }
    }
}