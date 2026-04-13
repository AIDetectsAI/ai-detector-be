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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AIModelController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AIModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ModelAnalysisFlowService modelAnalysisFlowService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    public void testUseModel_GivenHappyPath_ShouldReturn200AndJson() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "hello".getBytes());
        AIModelResponse resp = new AIModelResponse(0.95, "TestModel", 123L);
        Mockito.when(modelAnalysisFlowService.analyzeAndStore(any(), eq("testUser"))).thenReturn(resp);

        // when
        mockMvc.perform(multipart("/api/useModel").file(image)
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
}
