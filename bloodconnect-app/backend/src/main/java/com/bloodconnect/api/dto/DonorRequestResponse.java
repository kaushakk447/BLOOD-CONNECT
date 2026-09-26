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
public class DonorRequestResponse {
    private Long id;
    private Long bloodRequestId;
    private Long donorId;
    private BigDecimal distanceKm;
    private String status;
    private LocalDateTime notifiedAt;
    private LocalDateTime respondedAt;
    private BloodRequestResponse bloodRequest;
}
