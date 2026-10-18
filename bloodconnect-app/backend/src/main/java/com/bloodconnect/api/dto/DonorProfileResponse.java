package com.bloodconnect.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonorProfileResponse {
    private Long id;
    private Long userId;
    private String bloodGroup;
    private LocalDate dateOfBirth;
    private BigDecimal weightKg;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String availabilityStatus;
    private LocalDate lastDonationDate;
    private Integer donationCount;
    private String verificationStatus;
    private Boolean allowLocationSharing;
    private Boolean pushNotificationsEnabled;
    private LocalDateTime createdAt;
    private UserResponse user;
}
