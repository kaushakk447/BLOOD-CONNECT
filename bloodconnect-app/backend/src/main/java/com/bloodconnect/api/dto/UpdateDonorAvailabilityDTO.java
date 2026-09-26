package com.bloodconnect.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDonorAvailabilityDTO {
    @NotBlank(message = "Availability status is required")
    private String availabilityStatus;
}
