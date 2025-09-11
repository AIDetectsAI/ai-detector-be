package org.example.aidetectorbe.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class UserDTO {

    @NotNull(message = "login cannot be null")
    @NotBlank(message = "login cannot be blank")
    private String login;

    @NotNull(message = "password cannot be null")
    @NotBlank(message = "login cannot be blank")
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
