package com.bloodconnect.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDonorProfileDTO {
    @NotBlank(message = "Blood group is required")
    private String bloodGroup;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotNull(message = "Weight is required")
    @Min(value = 40, message = "Weight must be at least 40 kg")
    private BigDecimal weightKg;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Latitude is required")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    private BigDecimal longitude;

    private LocalDate lastDonationDate;
    private Boolean allowLocationSharing;
    private Boolean pushNotificationsEnabled;
}
