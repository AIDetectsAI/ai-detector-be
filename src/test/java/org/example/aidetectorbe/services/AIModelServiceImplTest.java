package org.example.aidetectorbe.services;

import org.example.aidetectorbe.dto.AIModelResponse;
import org.example.aidetectorbe.entities.ModelResult;
import org.example.aidetectorbe.entities.User;
import java.util.Collections;
import org.example.aidetectorbe.exceptions.AIServiceException;
import org.example.aidetectorbe.repository.ModelResultRepository;
import org.example.aidetectorbe.repository.UserRepository;
import org.example.aidetectorbe.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;

import org.springframework.http.HttpStatus;

import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.withinPercentage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class AIModelServiceImplTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper;
    private AIModelServiceImpl service;
    private ModelResultRepository modelResultRepository;
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
        restTemplate = new RestTemplate(factory);
        mockServer = MockRestServiceServer.createServer(restTemplate);
        objectMapper = new ObjectMapper();
        service = new AIModelServiceImpl(restTemplate, objectMapper);
        modelResultRepository = Mockito.mock(ModelResultRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        ReflectionTestUtils.setField(service, "modelResultRepository", modelResultRepository);
        ReflectionTestUtils.setField(service, "userRepository", userRepository);

        // override config values via reflection since they're private and injected via @Value in production
        try {
            java.lang.reflect.Field urlField = AIModelServiceImpl.class.getDeclaredField("aiServiceUrl");
            urlField.setAccessible(true);
            urlField.set(service, "http://localhost:9999");

            java.lang.reflect.Field endpointField = AIModelServiceImpl.class.getDeclaredField("aiServiceEndpoint");
            endpointField.setAccessible(true);
            endpointField.set(service, "/verify/image");

            java.lang.reflect.Field fileField = AIModelServiceImpl.class.getDeclaredField("aiServiceFileField");
            fileField.setAccessible(true);
            fileField.set(service, "file");

            java.lang.reflect.Field modelField = AIModelServiceImpl.class.getDeclaredField("modelName");
            modelField.setAccessible(true);
            modelField.set(service, "TestModel");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testProcessImage_WhenSuccessfulResponse_ShouldParsesCorrectly() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "hello".getBytes());
        String aiResponse = "{\"certainty\": 0.95}";
        // when
        mockServer.expect(MockRestRequestMatchers.requestTo("http://localhost:9999/verify/image"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withSuccess(aiResponse, MediaType.APPLICATION_JSON));

        AIModelResponse resp = service.processImage(image);

        mockServer.verify();
        // then
        assertThat(resp).isNotNull();
        assertThat(resp.getCertainty()).isCloseTo(0.95d, withinPercentage(0.1d));
        assertThat(resp.getModelUsed()).isEqualTo("TestModel");
        assertThat(resp.getProcessingTimeMs()).isGreaterThanOrEqualTo(0L);
    }

    @Test
    public void testProcessImage_WhenAiReturns4xx_ShouldThrowAIServiceException() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "hello".getBytes());
        // when
        mockServer.expect(MockRestRequestMatchers.requestTo("http://localhost:9999/verify/image"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.UNPROCESSABLE_ENTITY).body("{\"detail\": [{\"loc\": [\"body\", \"file\"], \"msg\": \"Field required\"}]}").contentType(MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> service.processImage(image)).isInstanceOf(AIServiceException.class);
        // then
        mockServer.verify();
    }

    @Test
    public void testProcessImage_WhenAiReturns5xx_ShouldThrowAIServiceException() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "hello".getBytes());
        // when
        mockServer.expect(MockRestRequestMatchers.requestTo("http://localhost:9999/verify/image"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withServerError().body("Internal Server Error").contentType(MediaType.TEXT_PLAIN));
        assertThatThrownBy(() -> service.processImage(image)).isInstanceOf(AIServiceException.class);
        // then
        mockServer.verify();
    }

    @Test
    public void testProcessImage_WhenAiServiceUnreachable_ShouldThrowAIServiceException() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "hello".getBytes());
        // when
        mockServer.expect(MockRestRequestMatchers.requestTo("http://localhost:9999/verify/image"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(request -> { throw new java.net.ConnectException("Connection refused"); });
        assertThatThrownBy(() -> service.processImage(image)).isInstanceOf(AIServiceException.class);
        // then
        mockServer.verify();
    }

    @Test
    public void testIsServiceHealthy_WhenAiServiceIsUp_ShouldReturnTrue() throws Exception {
        // when
        mockServer.expect(MockRestRequestMatchers.requestTo("http://localhost:9999/health"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess("{\"status\":\"ok\"}", MediaType.APPLICATION_JSON));
        // then
        assertThat(service.isServiceHealthy()).isTrue();
        mockServer.verify();
    }

    @Test
    public void testIsServiceHealthy_WhenAiServiceIsDown_ShouldReturnFalse() throws Exception {
        // when
        mockServer.expect(MockRestRequestMatchers.requestTo("http://localhost:9999/health"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withServerError().body("Service Unavailable").contentType(MediaType.TEXT_PLAIN));
        // then
        assertThat(service.isServiceHealthy()).isFalse();
        mockServer.verify();
    }

    @Test
    public void testParseSuccessfulResponse_WhenInvalidJson_ShouldThrowAIServiceException() throws Exception {
        // given
        String invalidJson = "This is not JSON";
        // when n then
        assertThatThrownBy(() -> {
            java.lang.reflect.Method method = AIModelServiceImpl.class.getDeclaredMethod("parseSuccessfulResponse", String.class, long.class);
            method.setAccessible(true);
            method.invoke(service, invalidJson, 100L);
        }).hasCauseInstanceOf(AIServiceException.class);
    }

        @Test
        public void testProcessImage_WhenPersistenceFails_ShouldReturnSuccessfulAiResponseWithoutImageId() throws Exception {
            // given
            MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "hello".getBytes());
            String aiResponse = "{\"certainty\": 0.95}";
            User user = new User();
            user.setId(java.util.UUID.randomUUID());
            user.setLogin("john");
            user.setProvider(Constants.AI_DETECTOR_API_PROVIDER);

                   SecurityContextHolder.getContext()
                       .setAuthentication(new UsernamePasswordAuthenticationToken("john", null, Collections.emptyList()));

            Mockito.when(userRepository.findByLoginAndProvider("john", Constants.AI_DETECTOR_API_PROVIDER))
                .thenReturn(java.util.Optional.of(user));
            // service no longer persists directly in processImage; ensure response is returned
            mockServer.expect(MockRestRequestMatchers.requestTo("http://localhost:9999/verify/image"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess(aiResponse, MediaType.APPLICATION_JSON));

            // when
            AIModelResponse resp = service.processImage(image);

            // then
            mockServer.verify();
            assertThat(resp).isNotNull();
            assertThat(resp.getCertainty()).isCloseTo(0.95d, withinPercentage(0.1d));
            assertThat(resp.getModelUsed()).isEqualTo("TestModel");
            assertThat(resp.getImageId()).isNull();
            // persistence is handled elsewhere; ensure no direct save was performed here
            Mockito.verify(modelResultRepository, Mockito.never()).save(any(ModelResult.class));
        }

        @Test
        public void testGetPastQueryByImageId_WhenSuccess_ShouldReturnMappedResponse() throws Exception {
            // Arrange
            java.util.UUID userId = java.util.UUID.randomUUID();
            java.util.UUID photoId = java.util.UUID.randomUUID();
            User user = new User();
            user.setId(userId);
            user.setLogin("john");
            user.setProvider(Constants.AI_DETECTOR_API_PROVIDER);


            ModelResult record = new ModelResult();
            record.setPhotoId(photoId);
            record.setUserId(userId);
            record.setModel("AIDetector");
            record.setChance(new java.math.BigDecimal("0.87"));

            Mockito.when(userRepository.findByLoginAndProvider("john", Constants.AI_DETECTOR_API_PROVIDER))
                    .thenReturn(java.util.Optional.of(user));
                Mockito.when(modelResultRepository.findByPhotoIdAndUserId(photoId, userId))
                    .thenReturn(java.util.Optional.of(record));

            // Act
            AIModelResponse resp = service.getPastQueryByImageId(photoId.toString(), "john");

            // Assert
            assertThat(resp).isNotNull();
            assertThat(resp.getCertainty()).isEqualTo(0.87d);
            assertThat(resp.getModelUsed()).isEqualTo("AIDetector");
            assertThat(resp.getImageId()).isEqualTo(photoId.toString());
        }

        @Test
        public void testGetPastQueryByImageId_WhenUsernameMissing_ShouldThrow401() {
            // Arrange
            String someId = java.util.UUID.randomUUID().toString();

            // Act
            org.assertj.core.api.ThrowableAssert.ThrowingCallable action = () -> service.getPastQueryByImageId(someId, null);

            // Assert
            assertThatThrownBy(action)
                .isInstanceOf(AIServiceException.class)
                .hasFieldOrPropertyWithValue("statusCode", 401);
        }

        @Test
        public void testGetPastQueryByImageId_WhenImageIdInvalid_ShouldThrow400() {
            // Arrange
            User user = new User();
            user.setId(java.util.UUID.randomUUID());
            Mockito.when(userRepository.findByLoginAndProvider("john", Constants.AI_DETECTOR_API_PROVIDER))
                .thenReturn(java.util.Optional.of(user));

            // Act
            org.assertj.core.api.ThrowableAssert.ThrowingCallable action = () -> service.getPastQueryByImageId("not-a-uuid", "john");

            // Assert
            assertThatThrownBy(action)
                .isInstanceOf(AIServiceException.class)
                .hasFieldOrPropertyWithValue("statusCode", 400);
        }

        @Test
        public void testGetPastQueryByImageId_WhenUserNotFound_ShouldThrow403() {
            // Arrange
            Mockito.when(userRepository.findByLoginAndProvider("john", Constants.AI_DETECTOR_API_PROVIDER))
                .thenReturn(java.util.Optional.empty());

            // Act
            org.assertj.core.api.ThrowableAssert.ThrowingCallable action = () -> service.getPastQueryByImageId(java.util.UUID.randomUUID().toString(), "john");

            // Assert
            assertThatThrownBy(action)
                .isInstanceOf(AIServiceException.class)
                .hasFieldOrPropertyWithValue("statusCode", 403);
        }

        @Test
        public void testGetPastQueryByImageId_WhenRecordNotFound_ShouldThrow404() {
            // Arrange
            java.util.UUID userId = java.util.UUID.randomUUID();
            java.util.UUID photoId = java.util.UUID.randomUUID();
            User user = new User();
            user.setId(userId);

            Mockito.when(userRepository.findByLoginAndProvider("john", Constants.AI_DETECTOR_API_PROVIDER))
                .thenReturn(java.util.Optional.of(user));
            Mockito.when(modelResultRepository.findByPhotoIdAndUserId(photoId, userId))
                .thenReturn(java.util.Optional.empty());

            // Act
            org.assertj.core.api.ThrowableAssert.ThrowingCallable action = () -> service.getPastQueryByImageId(photoId.toString(), "john");

            // Assert
            assertThatThrownBy(action)
                .isInstanceOf(AIServiceException.class)
                .hasFieldOrPropertyWithValue("statusCode", 404);
        }

        @Test
        public void testGetPastQueryByImageId_WhenRepositoryThrowsUnexpectedError_ShouldThrow500() {
            // Arrange
            java.util.UUID userId = java.util.UUID.randomUUID();
            java.util.UUID photoId = java.util.UUID.randomUUID();
            User user = new User();
            user.setId(userId);

            Mockito.when(userRepository.findByLoginAndProvider("john", Constants.AI_DETECTOR_API_PROVIDER))
                    .thenReturn(java.util.Optional.of(user));
                Mockito.when(modelResultRepository.findByPhotoIdAndUserId(eq(photoId), eq(userId)))
                    .thenThrow(new RuntimeException("db read failure"));

            // Act
            org.assertj.core.api.ThrowableAssert.ThrowingCallable action = () -> service.getPastQueryByImageId(photoId.toString(), "john");

            // Assert
            assertThatThrownBy(action)
                    .isInstanceOf(AIServiceException.class)
                    .hasFieldOrPropertyWithValue("statusCode", 500);
        }
}
