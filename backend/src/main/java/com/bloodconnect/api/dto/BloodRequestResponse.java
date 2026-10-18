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
public class BloodRequestResponse {
    private Long id;
    private String bloodGroup;
    private Integer unitsRequired;
    private Integer unitsFulfilled;
    private String urgency;
    private String status;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private String contactNumber;
    private LocalDateTime requiredDatetime;
    private Integer donorCountNotified;
    private Integer donorCountResponded;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserResponse requester;
}
