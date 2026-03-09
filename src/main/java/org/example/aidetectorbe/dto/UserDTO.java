package org.example.aidetectorbe.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
public class UserDTO {

    @NotNull(message = "login cannot be null")
    @NotBlank(message = "login cannot be blank")
    @Pattern(regexp = "^[A-Za-z0-9_]{3,20}$", message = "login must be 3-20 chars and contain only letters, digits, or underscore")
    private String login;

    @NotNull(message = "password cannot be null")
    @NotBlank(message = "password cannot be blank")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=[\\]{}|;:'\",.<>/?~]).{8,}$", message = "password must be at least 8 chars, include an uppercase letter, a digit and a special character")
    private String password;

    @NotBlank
    @Email(message = "email invalid or blank")
    private String email;

    public UserDTO(String login, String password, String email) {
        this.login = login;
        this.password = password;
        this.email = email;
    }
}
