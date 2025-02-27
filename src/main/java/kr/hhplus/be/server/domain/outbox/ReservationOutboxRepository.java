package kr.hhplus.be.server.domain.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationOutboxRepository extends JpaRepository<ReservationOutbox, String> {
    ReservationOutbox findByIdAndStatus(Long id, OutboxStatus status);
    List<ReservationOutbox> findByStatusAndEventType(OutboxStatus status, OutboxEventType eventType);
}
