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
    @Index(name = "idx_blood_requests_status", columnList = "status"),
    @Index(name = "idx_blood_requests_blood_group", columnList = "blood_group"),
    @Index(name = "idx_blood_requests_location", columnList = "latitude, longitude"),
    @Index(name = "idx_blood_requests_urgency", columnList = "urgency"),
    @Index(name = "idx_blood_requests_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "hospital_id")
    private Long hospitalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false)
    private BloodGroup bloodGroup;

    @Column(name = "units_required", nullable = false)
    private Integer unitsRequired;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency", nullable = false)
    @Builder.Default
    private Urgency urgency = Urgency.HIGH;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "latitude", nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Status status = Status.OPEN;

    @Column(name = "description")
    private String description;

    @Column(name = "contact_number", nullable = false)
    private String contactNumber;

    @Column(name = "required_datetime", nullable = false)
    private LocalDateTime requiredDatetime;

    @Column(name = "search_radius_km", nullable = false)
    @Builder.Default
    private Integer searchRadiusKm = 50;

    @Column(name = "units_fulfilled", nullable = false)
    @Builder.Default
    private Integer unitsFulfilled = 0;

    @Column(name = "donor_count_notified", nullable = false)
    @Builder.Default
    private Integer donorCountNotified = 0;

    @Column(name = "donor_count_responded", nullable = false)
    @Builder.Default
    private Integer donorCountResponded = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "completed_at")
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
