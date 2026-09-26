package com.bloodconnect.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "donor_requests", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_notified_at", columnList = "notified_at"),
    @Index(name = "idx_donor_id", columnList = "donor_id")
},
uniqueConstraints = @UniqueConstraint(columnNames = {"blood_request_id", "donor_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonorRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blood_request_id", nullable = false)
    private Long bloodRequestId;

    @Column(name = "donor_id", nullable = false)
    private Long donorId;

    private BigDecimal distanceKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.NOTIFIED;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime notifiedAt = LocalDateTime.now();

    private LocalDateTime respondedAt;

    private String connectionDetails;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Status {
        NOTIFIED, ACCEPTED, DECLINED, EXPIRED, CONNECTED, COMPLETED
    }
}
