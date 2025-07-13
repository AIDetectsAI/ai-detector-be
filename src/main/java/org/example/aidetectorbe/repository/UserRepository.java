package org.example.aidetectorbe.repository;


import org.example.aidetectorbe.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {
}