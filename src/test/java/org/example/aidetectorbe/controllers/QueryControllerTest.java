package org.example.aidetectorbe.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.aidetectorbe.dto.ErrorResponse;
import org.example.aidetectorbe.services.QueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.persistence.EntityNotFoundException;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class QueryControllerTest {

    private MockMvc mockMvc;
    private QueryService mockQueryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockQueryService = mock(QueryService.class);
        QueryController queryController = new QueryController(mockQueryService);
        mockMvc = MockMvcBuilders.standaloneSetup(queryController).build();
    }

    @Test
    void testDeleteQuery_GivenExistingQuery_ShouldReturnNoContent() throws Exception {
        // given
        Long queryId = 1L;
        String userLogin = "testUser";

        // when & then
        mockMvc.perform(delete("/api/users/{userLogin}/queries/{queryId}", userLogin, queryId))
                .andExpect(status().isNoContent());

        verify(mockQueryService).deleteQuery(queryId, userLogin);
    }

    @Test
    void testDeleteQuery_GivenNonExistingQuery_ShouldReturnNotFound() throws Exception {
        // given
        Long queryId = 1L;
        String userLogin = "testUser";
        String exceptionMessage = "Query was not found";

        // when
        doThrow(new EntityNotFoundException(exceptionMessage)).when(mockQueryService).deleteQuery(anyLong(), anyString());

        ErrorResponse expectedError = new ErrorResponse("Delete query error", exceptionMessage, 404);

        // then
        mockMvc.perform(delete("/api/users/{userLogin}/queries/{queryId}", userLogin, queryId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedError)));

        verify(mockQueryService).deleteQuery(queryId, userLogin);
    }

    @Test
    void testDeleteQuery_GivenDifferentQueryIds_ShouldCallServiceWithCorrectIds() throws Exception {
        // given
        Long queryId1 = 5L;
        Long queryId2 = 10L;
        String userLogin = "testUser";

        // when & then
        mockMvc.perform(delete("/api/users/{userLogin}/queries/{queryId}", userLogin, queryId1))
                .andExpect(status().isNoContent());
        verify(mockQueryService).deleteQuery(queryId1, userLogin);

        mockMvc.perform(delete("/api/users/{userLogin}/queries/{queryId}", userLogin, queryId2))
                .andExpect(status().isNoContent());
        verify(mockQueryService).deleteQuery(queryId2, userLogin);
    }

    @Test
    void testDeleteAllQueries_GivenExistingUser_ShouldReturnNoContent() throws Exception {
        // given
        String userLogin = "testUser";

        // when & then

        mockMvc.perform(delete("/api/users/{userLogin}/queries/all", userLogin))
                .andExpect(status().isNoContent());

        verify(mockQueryService).deleteAllQueries(userLogin);
    }

    @Test
    void testDeleteAllQueries_GivenNonExistingUser_ShouldReturnNotFound() throws Exception {
        // given
        String userLogin = "testUser";
        String exceptionMessage = "Queries were not found";

        // when
        doThrow(new EntityNotFoundException(exceptionMessage)).when(mockQueryService).deleteAllQueries(anyString());

        ErrorResponse expectedError = new ErrorResponse("Delete query error", exceptionMessage, 404);

        // then
        mockMvc.perform(delete("/api/users/{userLogin}/queries/all", userLogin))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedError)));

        verify(mockQueryService).deleteAllQueries(userLogin);
    }

}
