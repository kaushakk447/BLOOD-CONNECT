package com.bloodconnect.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "donor_profiles", indexes = {
    @Index(name = "idx_blood_group", columnList = "blood_group"),
    @Index(name = "idx_verification_status", columnList = "verification_status"),
    @Index(name = "idx_availability_status", columnList = "availability_status"),
    @Index(name = "idx_location", columnList = "latitude, longitude")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodGroup bloodGroup;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private BigDecimal weightKg;

    private String location;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.UNAVAILABLE;

    private LocalDate lastDonationDate;

    @Column(nullable = false)
    @Builder.Default
    private Integer donationCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private Boolean allowLocationSharing = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean pushNotificationsEnabled = true;

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

    public enum BloodGroup {
        O_NEGATIVE, O_POSITIVE,
        A_NEGATIVE, A_POSITIVE,
        B_NEGATIVE, B_POSITIVE,
        AB_NEGATIVE, AB_POSITIVE
    }

    public enum AvailabilityStatus {
        AVAILABLE, UNAVAILABLE, ON_COOLDOWN
    }

    public enum VerificationStatus {
        PENDING, VERIFIED, REJECTED
    }
}
