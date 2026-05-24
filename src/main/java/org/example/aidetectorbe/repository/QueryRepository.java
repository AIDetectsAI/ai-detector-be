package org.example.aidetectorbe.repository;

import org.example.aidetectorbe.entities.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.Set;

public interface QueryRepository extends JpaRepository<Query, Long> {
    Optional<Query> findByIdAndUser_Login(Long queryId, String login);
    Optional<Set<Query>> findByUser_Login(String login);
}