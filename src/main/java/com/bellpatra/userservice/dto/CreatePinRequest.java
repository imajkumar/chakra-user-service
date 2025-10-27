package com.bellpatra.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePinRequest {
    
    @NotBlank(message = "PIN is required")
    @Size(min = 4, max = 4, message = "PIN must be exactly 4 digits")
    @Pattern(regexp = "\\d{4}", message = "PIN must contain only numbers")
    private String pin;
    
    @NotBlank(message = "Confirm PIN is required")
    @Size(min = 4, max = 4, message = "Confirm PIN must be exactly 4 digits")
    @Pattern(regexp = "\\d{4}", message = "Confirm PIN must contain only numbers")
    private String confirmPin;
}

