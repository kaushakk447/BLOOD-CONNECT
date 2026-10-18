package com.bloodconnect.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSmsConsentDTO {
    @NotNull(message = "SMS consent is required")
    private Boolean consent;
}
