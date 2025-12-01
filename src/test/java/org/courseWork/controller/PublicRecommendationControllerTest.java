package org.courseWork.controller;

import org.courseWork.model.Client;
import org.courseWork.model.Product;
import org.courseWork.model.ProductOffer;
import org.courseWork.rules.model.RecommendationRule;
import org.courseWork.service.ClientService;
import org.courseWork.service.RecommendationDynamicRuleService;
import org.courseWork.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicRecommendationControllerTest {

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private ClientService clientService;

    @Mock
    private RecommendationDynamicRuleService recommendationDynamicRuleService;

    private PublicRecommendationController controller;

    private UUID testUserId;
    private UUID testRuleId;
    private UUID testProductId;
    private Client testClient;
    private ProductOffer testProductOffer;
    private RecommendationRule testRule;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Создаем экземпляр контроллера с моками сервисов
        controller = new PublicRecommendationController(
                recommendationService,
                recommendationDynamicRuleService,
                clientService
        );

        // Генерируем тестовые UUID
        testUserId = UUID.randomUUID();
        testRuleId = UUID.randomUUID();
        testProductId = UUID.randomUUID();

        // Создаем тестовые объекты с правильными данными
        testClient = new Client(testUserId, "testuser", "Иван", "Иванов");

        testProductOffer = new ProductOffer(testProductId, "Тестовый продукт", "Тестовое описание");

        testRule = new RecommendationRule();
        testRule.setName("Тестовое правило");

        testProduct = new Product(testProductId, "Тестовый продукт", "Тестовое описание");
    }

    @Test
    void testApi_ShouldReturnWelcomeMessage() {
        // Действие
        String result = controller.testApi();

        // Проверка
        assertEquals("Welcome to Demo!", result);
    }

    @Test
    void getClientById_ShouldReturnClient() {
        // Подготовка
        when(clientService.findById(testUserId)).thenReturn(testClient);

        // Действие
        Client result = controller.getClientById(testUserId.toString());

        // Проверка
        assertNotNull(result);
        assertEquals(testClient, result);
        assertEquals(testUserId, result.getId());
        assertEquals("testuser", result.getUserName());
        assertEquals("Иван", result.getFirstName());
        assertEquals("Иванов", result.getLastName());
        verify(clientService, times(1)).findById(testUserId);
    }

    @Test
    void getClientById_WithInvalidUUID_ShouldThrowException() {
        // Подготовка
        String invalidUuid = "неверный-uuid";

        // Действие и проверка
        assertThrows(IllegalArgumentException.class, () -> {
            controller.getClientById(invalidUuid);
        });
        verify(clientService, never()).findById(any());
    }

    @Test
    void getAllClients_ShouldReturnClientsList() {
        // Подготовка
        List<Client> clients = List.of(testClient);
        when(clientService.getAllClients()).thenReturn(clients);

        // Действие
        ResponseEntity<Collection<Client>> response = controller.getAllClients();

        // Проверка
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(clients, response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testClient, response.getBody().iterator().next());
        verify(clientService, times(1)).getAllClients();
    }

    @Test
    void getAllClients_WithEmptyList_ShouldReturnEmptyList() {
        // Подготовка
        when(clientService.getAllClients()).thenReturn(Collections.emptyList());

        // Действие
        ResponseEntity<Collection<Client>> response = controller.getAllClients();

        // Проверка
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getRecommendations_ShouldReturnRecommendations() {
        // Подготовка
        List<ProductOffer> recommendations = List.of(testProductOffer);
        when(recommendationService.getRecommendedProducts(testUserId)).thenReturn(recommendations);

        // Действие
        ResponseEntity<List<ProductOffer>> response = controller.getRecommendations(testUserId);

        // Проверка
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(recommendations, response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testProductOffer, response.getBody().get(0));
        verify(recommendationService, times(1)).getRecommendedProducts(testUserId);
    }

    @Test
    void getRecommendations_WithNullUserId_ShouldReturnBadRequest() {
        // Действие
        ResponseEntity<List<ProductOffer>> response = controller.getRecommendations(null);

        // Проверка
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(recommendationService, never()).getRecommendedProducts(any());
    }

    @Test
    void getRecommendations_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Подготовка
        when(recommendationService.getRecommendedProducts(testUserId))
                .thenThrow(new RuntimeException("Ошибка сервиса"));

        // Действие
        ResponseEntity<List<ProductOffer>> response = controller.getRecommendations(testUserId);

        // Проверка
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void createRule_ShouldReturnCreatedRule() {
        // Подготовка
        // Явно указываем тип параметра с помощью каста
        when(recommendationDynamicRuleService.createRule((RecommendationRule) any(RecommendationRule.class)))
                .thenReturn(testRule);

        // Действие
        ResponseEntity<RecommendationRule> response = controller.createRule(testRule);

        // Проверка
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(testRule, response.getBody());
        verify(recommendationDynamicRuleService, times(1)).createRule((RecommendationRule) any(RecommendationRule.class));
    }

    @Test
    void createRule_WithNullName_ShouldReturnBadRequest() {
        // Подготовка
        RecommendationRule invalidRule = new RecommendationRule();
        invalidRule.setName(null);

        // Действие
        ResponseEntity<RecommendationRule> response = controller.createRule(invalidRule);

        // Проверка
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(recommendationDynamicRuleService, never()).createRule((RecommendationRule) any());
    }

    @Test
    void deleteRule_ShouldReturnNoContent() {
        // Подготовка
        doNothing().when(recommendationDynamicRuleService).deleteRule(testRuleId);

        // Действие
        ResponseEntity<Void> response = controller.deleteRule(testRuleId);

        // Проверка
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(recommendationDynamicRuleService, times(1)).deleteRule(testRuleId);
    }

    @Test
    void deleteRule_WhenRuleNotFound_ShouldReturnNotFound() {
        // Подготовка
        doThrow(new org.courseWork.model.RulesNotFoundException("Правило не найдено"))
                .when(recommendationDynamicRuleService).deleteRule(testRuleId);

        // Действие
        ResponseEntity<Void> response = controller.deleteRule(testRuleId);

        // Проверка
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAllRules_ShouldReturnProductsList() {
        // Подготовка
        List<Product> products = List.of(testProduct);
        when(recommendationDynamicRuleService.getAllAvailableProducts()).thenReturn(products);

        // Действие
        ResponseEntity<List<Product>> response = controller.getAllRules();

        // Проверка
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(products, response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testProduct, response.getBody().get(0));
        verify(recommendationDynamicRuleService, times(1)).getAllAvailableProducts();
    }

    @Test
    void getAllRules_WithEmptyList_ShouldReturnEmptyList() {
        // Подготовка
        when(recommendationDynamicRuleService.getAllAvailableProducts()).thenReturn(Collections.emptyList());

        // Действие
        ResponseEntity<List<Product>> response = controller.getAllRules();

        // Проверка
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }
}