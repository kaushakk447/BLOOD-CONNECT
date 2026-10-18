package com.bloodconnect.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalResponse {
    private Long id;
    private Long userId;
    private String hospitalName;
    private String address;
    private String phoneNumber;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String licenseNumber;
    private String verificationStatus;
    private LocalDateTime createdAt;
}
