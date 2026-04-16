package org.example.aidetectorbe.controllers;

import org.example.aidetectorbe.dto.AIModelResponse;
import org.example.aidetectorbe.exceptions.AIServiceException;
import org.example.aidetectorbe.security.JwtUtil;
import org.example.aidetectorbe.services.ModelAnalysisFlowService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Base64;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AIModelController.class, properties = "spring.servlet.multipart.max-file-size=10MB")
@AutoConfigureMockMvc(addFilters = false)
public class AIModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ModelAnalysisFlowService modelAnalysisFlowService;

    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {

    }

    private MockMultipartFile validImageFile() {
        byte[] png = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO6z8e8AAAAASUVORK5CYII=");
        return new MockMultipartFile("image", "test.png", "image/png", png);
    }

    @Test
    public void testUseModel_GivenHappyPath_ShouldReturn200AndJson() throws Exception {
        // given
        MockMultipartFile image = validImageFile();
        //when
        AIModelResponse resp = new AIModelResponse( 0.95, "TestModel", 123L);
        Mockito.when(aiModelService.processImage(any())).thenReturn(resp);
        // then
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "hello".getBytes());
        AIModelResponse resp = new AIModelResponse(0.95, "TestModel", 123L);
        Mockito.when(modelAnalysisFlowService.analyzeAndStore(any(), eq("testUser"))).thenReturn(resp);

        // when
        mockMvc.perform(multipart("/api/useModel").file(image)
            .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString("certainty")));
                .requestAttr("login", "testUser")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                // then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("certainty")));
    }

    @Test
    public void testUseModel_GivenNoFile_ShouldReturn400() throws Exception {
        // when n then
        mockMvc.perform(multipart("/api/useModel")
            .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isBadRequest());
                .requestAttr("login", "testUser")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUseModel_WhenAiServiceThrowsException_ShouldReturnAppropriateStatus() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "hello".getBytes());
        Mockito.when(modelAnalysisFlowService.analyzeAndStore(any(), eq("testUser")))
                .thenThrow(new AIServiceException("AI failed", 502));

        // then
        mockMvc.perform(multipart("/api/useModel").file(image)
                .requestAttr("login", "testUser")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadGateway());
    }
            .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isBadGateway());
    }

        @Test
        public void testPastQuery_WhenMissingLoginAttribute_ShouldReturn401() throws Exception {
        // Arrange: no login attr

        // Act
        ResultActions actions = mockMvc.perform(get("/api/pastQuery").param("imageId", "b39dcf78-78de-4b11-b4bc-15b760f266ca"));

        // Assert
        actions.andExpect(status().isUnauthorized());
        verify(aiModelService, never()).getPastQueryByImageId(anyString(), anyString());
        }

        @Test
        public void testPastQuery_WhenSuccessful_ShouldReturn200AndJson() throws Exception {
        // Arrange
        AIModelResponse resp = new AIModelResponse(0.87, "AIDetector", null, "b39dcf78-78de-4b11-b4bc-15b760f266ca");
        Mockito.when(aiModelService.getPastQueryByImageId(eq("b39dcf78-78de-4b11-b4bc-15b760f266ca"), eq("john")))
            .thenReturn(resp);

        // Act
        ResultActions actions = mockMvc.perform(get("/api/pastQuery")
            .param("imageId", "b39dcf78-78de-4b11-b4bc-15b760f266ca")
            .requestAttr("login", "john"));

        // Assert
        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString("imageId")));
        }

        @Test
        public void testPastQuery_WhenServiceThrowsAIServiceException_ShouldPropagateStatus() throws Exception {
        // Arrange
        Mockito.when(aiModelService.getPastQueryByImageId(anyString(), eq("john")))
            .thenThrow(new AIServiceException("Query not found", 404));

        // Act
        ResultActions actions = mockMvc.perform(get("/api/pastQuery")
            .param("imageId", "b39dcf78-78de-4b11-b4bc-15b760f266ca")
            .requestAttr("login", "john"));

        // Assert
        actions.andExpect(status().isNotFound());
        }
}
