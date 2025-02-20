package kr.hhplus.be.server.domain.outbox;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservation_outbox")
@Getter
@Setter
@NoArgsConstructor
public class ReservationOutbox {

    @Id
    private String id = UUID.randomUUID().toString();

//    @Lob
//    @Column(nullable = false, columnDefinition = "TEXT")
//    private String payload;

    @Column(nullable = false)
    private OutboxEventType eventType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OutboxStatus status = OutboxStatus.PENDING;

    private int retryCount = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
