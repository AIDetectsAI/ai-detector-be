package org.example.aidetectorbe.dto;

import lombok.Data;

@Data
public class UserDTO {

    private String login;
    private String password;
    private String email;
    private Boolean isDeleted;

    public UserDTO(String login, String password, String email) {
        this.login = login;
        this.password = password;
        this.email = email;
    }
}
