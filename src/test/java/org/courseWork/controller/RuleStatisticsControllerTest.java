package org.courseWork.controller;

import org.courseWork.service.RuleStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleStatisticsControllerTest {

    @Mock
    private RuleStatisticsService statisticsService;

    private RuleStatisticsController controller;

    private UUID testRuleId;
    private Long testUserId;

    @BeforeEach
    void setUp() {
        // Создаем экземпляр контроллера с моком сервиса
        controller = new RuleStatisticsController(statisticsService);

        // Генерируем тестовые ID
        testRuleId = UUID.randomUUID();
        testUserId = 12345L;
    }

    @Test
    void getRuleStats_ShouldReturnOverallStatistics() {
        // Подготовка
        Map<String, Object> expectedStats = Map.of(
                "totalRules", 10,
                "activeRules", 5,
                "totalExecutions", 1000,
                "successRate", 0.85
        );
        when(statisticsService.getOverallStatistics()).thenReturn(expectedStats);

        // Действие
        ResponseEntity<Map<String, Object>> response = controller.getRuleStats();

        // Проверка
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedStats, response.getBody());
        verify(statisticsService, times(1)).getOverallStatistics();
    }

    @Test
    void getRuleStats_WithRuleId_ShouldReturnRuleStatistics() {
        // Подготовка
        Map<String, Object> expectedStats = Map.of(
                "ruleId", testRuleId.toString(),
                "executions", 150,
                "successes", 120,
                "failures", 30,
                "successRate", 0.8
        );
        when(statisticsService.getRuleStatistics(testRuleId)).thenReturn(expectedStats);

        // Действие
        ResponseEntity<Map<String, Object>> response = controller.getRuleStats(testRuleId);

        // Проверка
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedStats, response.getBody());
        verify(statisticsService, times(1)).getRuleStatistics(testRuleId);
    }

    @Test
    void getRuleStats_WithRuleId_WhenServiceReturnsEmpty_ShouldReturnEmptyMap() {
        // Подготовка
        Map<String, Object> emptyStats = Map.of();
        when(statisticsService.getRuleStatistics(testRuleId)).thenReturn(emptyStats);

        // Действие
        ResponseEntity<Map<String, Object>> response = controller.getRuleStats(testRuleId);

        // Проверка
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emptyStats, response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getUserStats_ShouldReturnUserStatistics() {
        // Подготовка
        Map<String, Object> expectedStats = Map.of(
                "userId", testUserId,
                "rulesEvaluated", 25,
                "eligibleRules", 15,
                "ineligibleRules", 10,
                "eligibilityRate", 0.6
        );
        when(statisticsService.getUserStatistics(testUserId)).thenReturn(expectedStats);

        // Действие
        ResponseEntity<Map<String, Object>> response = controller.getUserStats(testUserId);

        // Проверка
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedStats, response.getBody());
        verify(statisticsService, times(1)).getUserStatistics(testUserId);
    }

    @Test
    void getUserStats_WithNonExistentUser_ShouldReturnEmptyStatistics() {
        // Подготовка
        Long nonExistentUserId = 99999L;
        Map<String, Object> emptyStats = Map.of();
        when(statisticsService.getUserStatistics(nonExistentUserId)).thenReturn(emptyStats);

        // Действие
        ResponseEntity<Map<String, Object>> response = controller.getUserStats(nonExistentUserId);

        // Проверка
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emptyStats, response.getBody());
    }

    @Test
    void clearStats_ShouldClearStatisticsAndReturnSuccess() {
        // Подготовка
        doNothing().when(statisticsService).clearStatistics();

        // Действие
        ResponseEntity<Map<String, Object>> response = controller.clearStats();

        // Проверка
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.get("status"));
        assertEquals("Statistics cleared successfully", responseBody.get("message"));

        verify(statisticsService, times(1)).clearStatistics();
    }

    @Test
    void clearStats_WhenServiceCompletes_ShouldReturnSuccessMessage() {
        // Подготовка
        doNothing().when(statisticsService).clearStatistics();

        // Действие
        ResponseEntity<Map<String, Object>> response = controller.clearStats();

        // Проверка
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertEquals("success", body.get("status"));
        assertEquals("Statistics cleared successfully", body.get("message"));
    }

    @Test
    void constructor_ShouldInitializeService() {
        // Проверка, что сервис правильно инициализирован в конструкторе
        assertNotNull(controller);
    }

    @Test
    void getRuleStats_WithSpecificRuleId_ShouldCallServiceWithCorrectId() {
        // Подготовка
        Map<String, Object> stats = Map.of("executions", 50);
        when(statisticsService.getRuleStatistics(testRuleId)).thenReturn(stats);

        // Действие
        controller.getRuleStats(testRuleId);

        // Проверка
        verify(statisticsService, times(1)).getRuleStatistics(testRuleId);
        verifyNoMoreInteractions(statisticsService);
    }

    @Test
    void getUserStats_WithSpecificUserId_ShouldCallServiceWithCorrectId() {
        // Подготовка
        Map<String, Object> stats = Map.of("rulesEvaluated", 10);
        when(statisticsService.getUserStatistics(testUserId)).thenReturn(stats);

        // Действие
        controller.getUserStats(testUserId);

        // Проверка
        verify(statisticsService, times(1)).getUserStatistics(testUserId);
        verifyNoMoreInteractions(statisticsService);
    }
}
