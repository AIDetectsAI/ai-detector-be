package org.example.aidetectorbe.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 50)
    private String login;

    @Column(nullable = false, length = 64)
    private String password;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false)
    private Boolean isDeleted;

    public User(UUID  id, String login, String password, String email) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.email = email;
        this.isDeleted = false;
    }

    public User(String login, String password, String email) {
        this.login = login;
        this.password = password;
        this.email = email;
        this.isDeleted = false;
    }

    public User() {
    }
}
