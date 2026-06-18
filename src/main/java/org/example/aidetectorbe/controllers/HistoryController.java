package org.example.aidetectorbe.controllers;

import lombok.AllArgsConstructor;
import org.example.aidetectorbe.dto.ErrorResponse;
import org.example.aidetectorbe.dto.HistoryEntryDTO;
import org.example.aidetectorbe.entities.ModelResult;
import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.repository.ModelResultRepository;
import org.example.aidetectorbe.repository.UserRepository;
import org.example.aidetectorbe.utils.logger.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class HistoryController {

    private final ModelResultRepository modelResultRepository;
    private final UserRepository userRepository;

    @GetMapping("/history")
    public ResponseEntity<?> getUserHistory(HttpServletRequest request) {
        String login = (String) request.getAttribute("login");
        if (login == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        User user = userRepository.findByLogin(login).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        List<ModelResult> results = modelResultRepository
                .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(user.getId());

        List<HistoryEntryDTO> history = results.stream()
                .map(r -> new HistoryEntryDTO(
                        r.getResultId(),
                        r.getPhotoUrl(),
                        r.getModel(),
                        r.getChance(),
                        r.getCreatedAt(),
                        r.getCaption()))
                .toList();

        Log.info("Returning " + history.size() + " history entries for user " + login);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/history/all")
    public ResponseEntity<?> deleteAllHistory(HttpServletRequest request) {
        String login = (String) request.getAttribute("login");
        if (login == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        User user = userRepository.findByLogin(login).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        List<ModelResult> results = modelResultRepository
                .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(user.getId());

        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        results.forEach(r -> r.setIsDeleted(true));
        modelResultRepository.saveAll(results);

        Log.info("Deleted " + results.size() + " history entries for user " + login);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/history/{resultId}")
    public ResponseEntity<?> deleteHistoryEntry(@PathVariable UUID resultId, HttpServletRequest request) {
        String login = (String) request.getAttribute("login");
        if (login == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        User user = userRepository.findByLogin(login).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        var entry = modelResultRepository.findByResultIdAndUserIdAndIsDeletedFalse(resultId, user.getId()).orElse(null);
        if (entry == null) {
            ErrorResponse errorResponse = new ErrorResponse("Not Found", "History entry not found", 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        entry.setIsDeleted(true);
        modelResultRepository.save(entry);

        Log.info("Deleted history entry " + resultId + " for user " + login);
        return ResponseEntity.noContent().build();
    }
}
