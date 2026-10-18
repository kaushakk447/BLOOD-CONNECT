package com.bloodconnect.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBloodRequestDTO {
    @NotBlank(message = "Blood group is required")
    private String bloodGroup;

    @NotNull(message = "Units required is required")
    @Min(value = 1, message = "Units must be at least 1")
    private Integer unitsRequired;

    @NotBlank(message = "Urgency is required")
    private String urgency;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Latitude is required")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    private BigDecimal longitude;

    @NotBlank(message = "Contact number is required")
    private String contactNumber;

    @NotNull(message = "Required datetime is required")
    private LocalDateTime requiredDatetime;

    private Long hospitalId;
    private String description;
    private Integer searchRadiusKm;
}
