package org.example.aidetectorbe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "currentPassword cannot be blank")
    private String currentPassword;

    @NotBlank(message = "newPassword cannot be blank")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-\\[\\]{}|;:'\\\",.<>/?~]).{8,}$", message = "password must be at least 8 chars, include an uppercase letter, a digit and a special character")
    private String newPassword;
}
