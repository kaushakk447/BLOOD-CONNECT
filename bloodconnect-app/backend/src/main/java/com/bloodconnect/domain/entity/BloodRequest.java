package com.bloodconnect.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_requests", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_blood_group", columnList = "blood_group"),
    @Index(name = "idx_location", columnList = "latitude, longitude"),
    @Index(name = "idx_urgency", columnList = "urgency"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long requesterId;

    private Long hospitalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodGroup bloodGroup;

    @Column(nullable = false)
    private Integer unitsRequired;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Urgency urgency = Urgency.HIGH;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private BigDecimal latitude;

    @Column(nullable = false)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.OPEN;

    private String description;

    @Column(nullable = false)
    private String contactNumber;

    @Column(nullable = false)
    private LocalDateTime requiredDatetime;

    @Column(nullable = false)
    @Builder.Default
    private Integer searchRadiusKm = 50;

    @Column(nullable = false)
    @Builder.Default
    private Integer unitsFulfilled = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer donorCountNotified = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer donorCountResponded = 0;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime expiresAt;

    private LocalDateTime completedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum BloodGroup {
        O_NEGATIVE, O_POSITIVE,
        A_NEGATIVE, A_POSITIVE,
        B_NEGATIVE, B_POSITIVE,
        AB_NEGATIVE, AB_POSITIVE
    }

    public enum Urgency {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Status {
        OPEN, SEARCHING, DONORS_NOTIFIED, DONOR_RESPONDED,
        CONNECTED, FULFILLED, CANCELLED, EXPIRED
    }
}
