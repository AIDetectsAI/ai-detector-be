package org.example.aidetectorbe.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Entity
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String login;

    @Column(nullable = false, length = 64)
    private String password;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false)
    private Boolean isDeleted;

    public User(Integer  id, String login, String password, String email) {
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
