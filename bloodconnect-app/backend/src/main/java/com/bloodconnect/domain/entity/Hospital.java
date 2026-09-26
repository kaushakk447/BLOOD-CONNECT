package com.bloodconnect.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "hospitals", indexes = {
    @Index(name = "idx_verification_status", columnList = "verification_status"),
    @Index(name = "idx_location", columnList = "latitude, longitude")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private String hospitalName;

    @Column(nullable = false)
    private String address;

    private String location;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(unique = true)
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    private Long verifiedBy;

    private LocalDateTime verifiedDate;

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

    public enum VerificationStatus {
        PENDING, VERIFIED, REJECTED
    }
}
