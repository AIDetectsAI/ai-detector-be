package org.example.aidetectorbe.services;

import org.example.aidetectorbe.entities.Query;
import org.example.aidetectorbe.repository.QueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QueryServiceImplTest {

    private QueryRepository mockQueryRepository;
    private QueryServiceImpl queryService;

    @BeforeEach
    void setUp() {
        mockQueryRepository = mock(QueryRepository.class);
        queryService = new QueryServiceImpl(mockQueryRepository);
    }

    @Test
    void deleteQuery_GivenExistingQuery_ShouldMarkAsDeleted() {
        // Arrange
        Long queryId = 1L;
        String userLogin = "testUser";
        Query query = new Query();
        query.setId(queryId);
        query.setIsDeleted(false);

        when(mockQueryRepository.findByIdAndUser_Login(queryId, userLogin))
                .thenReturn(Optional.of(query));

        // Act
        queryService.deleteQuery(queryId, userLogin);

        // Assert
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mockQueryRepository).save(queryCaptor.capture());
        Query savedQuery = queryCaptor.getValue();

        assertTrue(savedQuery.getIsDeleted());
        verify(mockQueryRepository).findByIdAndUser_Login(queryId, userLogin);
    }

    @Test
    void deleteQuery_GivenNonExistingQuery_ShouldThrowEntityNotFoundException() {
        // Arrange
        Long queryId = 1L;
        String userLogin = "testUser";

        when(mockQueryRepository.findByIdAndUser_Login(queryId, userLogin))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                queryService.deleteQuery(queryId, userLogin));

        assertEquals("Query was not found", exception.getMessage());
        verify(mockQueryRepository, never()).save(any(Query.class));
    }

    @Test
    void deleteQuery_GivenMismatchingUserLogin_ShouldThrowEntityNotFoundException() {
        // Arrange
        Long queryId = 999L;
        String attackerLogin = "attacker";

        when(mockQueryRepository.findByIdAndUser_Login(queryId, attackerLogin))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                queryService.deleteQuery(queryId, attackerLogin));

        assertEquals("Query was not found", exception.getMessage());
        verify(mockQueryRepository, never()).save(any(Query.class));
    }

}
