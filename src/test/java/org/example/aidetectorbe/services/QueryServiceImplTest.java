package org.example.aidetectorbe.services;

import org.example.aidetectorbe.entities.Query;
import org.example.aidetectorbe.repository.QueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

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

    @Test
    void deleteAllQueries_GivenExistingQueries_ShouldMarkAsDeleted() {
        // Arrange
        Long queryId1 = 1L;
        Long queryId2 = 2L;
        String userLogin = "testUser";
        Query query1 = new Query();
        query1.setId(queryId1);
        query1.setIsDeleted(false);
        Query query2 = new Query();
        query2.setId(queryId2);
        query2.setIsDeleted(false);

        Set<Query> queries = new HashSet<>();
        queries.add(query1);
        queries.add(query2);
        when(mockQueryRepository.findByUser_Login(userLogin))
                .thenReturn(Optional.of(queries));

        // Act
        queryService.deleteAllQueries(userLogin);

        // Assert
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mockQueryRepository, times(2)).save(queryCaptor.capture());
        List<Query> savedQueries = queryCaptor.getAllValues();
        assertEquals(2, savedQueries.size());
        for (Query query : savedQueries) {
            assertTrue(query.getIsDeleted());
        }

        verify(mockQueryRepository).findByUser_Login(userLogin);
    }

    @Test
    void deleteAllQueries_GivenNonExistingUser_ShouldThrowEntityNotFoundException() {
        // Arrange
        String userLogin = "testUser";

        when(mockQueryRepository.findByUser_Login(userLogin))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                queryService.deleteAllQueries(userLogin));

        assertEquals("Queries were not found", exception.getMessage());
        verify(mockQueryRepository, never()).save(any(Query.class));
    }

}
