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
    @Index(name = "idx_donor_profiles_blood_group", columnList = "blood_group"),
    @Index(name = "idx_donor_profiles_verification_status", columnList = "verification_status"),
    @Index(name = "idx_donor_profiles_availability_status", columnList = "availability_status"),
    @Index(name = "idx_donor_profiles_location", columnList = "latitude, longitude")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false)
    private BloodGroup bloodGroup;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "weight_kg", nullable = false)
    private BigDecimal weightKg;

    @Column(name = "location")
    private String location;

    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false)
    @Builder.Default
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.UNAVAILABLE;

    @Column(name = "last_donation_date")
    private LocalDate lastDonationDate;

    @Column(name = "donation_count", nullable = false)
    @Builder.Default
    private Integer donationCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "allow_location_sharing", nullable = false)
    @Builder.Default
    private Boolean allowLocationSharing = false;

    @Column(name = "push_notifications_enabled", nullable = false)
    @Builder.Default
    private Boolean pushNotificationsEnabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
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
