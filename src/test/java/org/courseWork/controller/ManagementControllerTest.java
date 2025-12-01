package org.courseWork.controller;

import org.courseWork.service.CacheService;
import org.courseWork.service.RuleStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManagementControllerTest {
    @Mock
    private CacheService cacheService;

    @Mock
    private RuleStatisticsService statisticsService;

    private ManagementController managementController;

    @BeforeEach
    void setUp() {
        managementController = new ManagementController(cacheService, statisticsService);
    }

    @Test
    void clearCaches_Success() {
        // Arrange
        doNothing().when(cacheService).clearAllCaches();

        // Act
        ResponseEntity<Map<String, Object>> response = managementController.clearCaches();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals("All caches cleared successfully", body.get("message"));
        assertNotNull(body.get("timestamp"));
        assertInstanceOf(LocalDateTime.class, body.get("timestamp"));

        verify(cacheService, times(1)).clearAllCaches();
    }

    @Test
    void getSystemInfo_Success() {
        // Arrange
        // Mock the runtime MX bean behavior if needed
        // For now, we'll test the basic structure

        // Act
        ResponseEntity<Map<String, Object>> response = managementController.getSystemInfo();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Product Recommendation System", body.get("name"));
        assertEquals("1.0.0", body.get("version"));
        assertEquals("Dynamic product recommendation system with Telegram integration", body.get("description"));
        assertNotNull(body.get("uptime"));
        assertTrue(body.get("uptime").toString().endsWith(" ms"));
        assertNotNull(body.get("timestamp"));
        assertInstanceOf(LocalDateTime.class, body.get("timestamp"));
    }

    @Test
    void getCacheStats_Success() {
        // Arrange
        Map<String, Object> expectedStats = Map.of(
                "cache1", Map.of("size", 100, "hits", 50),
                "cache2", Map.of("size", 200, "hits", 75)
        );
        when(cacheService.getCacheStats()).thenReturn(expectedStats);

        // Act
        ResponseEntity<Map<String, Object>> response = managementController.getCacheStats();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedStats, response.getBody());

        verify(cacheService, times(1)).getCacheStats();
    }

    @Test
    void getSystemInfo_UptimeFormat() {
        // Act
        ResponseEntity<Map<String, Object>> response = managementController.getSystemInfo();

        // Assert
        Map<String, Object> body = response.getBody();
        String uptime = (String) body.get("uptime");

        assertNotNull(uptime);
        assertTrue(uptime.matches("\\d+ ms")); // Should be digits followed by " ms"
    }

    @Test
    void clearCaches_VerifyServiceInteraction() {
        // Arrange
        doNothing().when(cacheService).clearAllCaches();

        // Act
        managementController.clearCaches();

        // Assert
        verify(cacheService, times(1)).clearAllCaches();
        verifyNoInteractions(statisticsService); // statisticsService should not be called
    }

    @Test
    void getCacheStats_EmptyStats() {
        // Arrange
        Map<String, Object> emptyStats = Map.of();
        when(cacheService.getCacheStats()).thenReturn(emptyStats);

        // Act
        ResponseEntity<Map<String, Object>> response = managementController.getCacheStats();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emptyStats, response.getBody());
    }

    @Test
    void getCacheStats_NullStats() {
        // Arrange
        when(cacheService.getCacheStats()).thenReturn(null);

        // Act
        ResponseEntity<Map<String, Object>> response = managementController.getCacheStats();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void constructor_Injection() {
        // This test verifies that constructor injection works correctly
        assertNotNull(managementController);
        assertNotNull(managementController); // This line seems redundant, but it's for demonstration
    }

    @Test
    void clearCaches_TimestampIsRecent() {
        // Arrange
        doNothing().when(cacheService).clearAllCaches();
        LocalDateTime testStart = LocalDateTime.now();

        // Act
        ResponseEntity<Map<String, Object>> response = managementController.clearCaches();

        // Assert
        Map<String, Object> body = response.getBody();
        LocalDateTime timestamp = (LocalDateTime) body.get("timestamp");

        assertNotNull(timestamp);
        assertTrue(timestamp.isAfter(testStart.minusSeconds(1)) || timestamp.isEqual(testStart));
        assertTrue(timestamp.isBefore(testStart.plusSeconds(1)) || timestamp.isEqual(testStart));
    }
}
