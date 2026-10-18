package com.bloodconnect.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sms_deliveries", uniqueConstraints = {
    @UniqueConstraint(name = "uk_sms_deliveries_idempotency_key", columnNames = "idempotency_key")
}, indexes = {
    @Index(name = "idx_sms_deliveries_recipient", columnList = "recipient_user_id"),
    @Index(name = "idx_sms_deliveries_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsDelivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_user_id", nullable = false)
    private Long recipientUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "related_entity_type", nullable = false)
    private String relatedEntityType;

    @Column(name = "related_entity_id", nullable = false)
    private Long relatedEntityId;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum EventType { DONOR_MATCH, DONOR_RESPONSE }
    public enum Status { PENDING, SENT, MOCKED, SKIPPED, FAILED }
}
