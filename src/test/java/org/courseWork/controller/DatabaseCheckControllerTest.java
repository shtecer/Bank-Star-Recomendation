package org.courseWork.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class DatabaseCheckControllerTest {

    @Mock
    private JdbcTemplate primaryJdbcTemplate;

    @Mock
    private JdbcTemplate rulesJdbcTemplate;

    private DatabaseCheckController databaseCheckController;

    @BeforeEach
    void setUp() {
        databaseCheckController = new DatabaseCheckController(primaryJdbcTemplate, rulesJdbcTemplate);
    }

    @Test
    void checkDatabases_Success() {
        // Arrange
        Integer expectedProductsCount = 100;
        Integer expectedRulesCount = 25;

        when(primaryJdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenReturn(expectedProductsCount);
        when(rulesJdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenReturn(expectedRulesCount);

        // Act
        Map<String, Object> result = databaseCheckController.checkDatabases();

        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));

        @SuppressWarnings("unchecked")
        Map<String, Object> primaryDatabase = (Map<String, Object>) result.get("primaryDatabase");
        assertNotNull(primaryDatabase);
        assertEquals(expectedProductsCount, primaryDatabase.get("productsCount"));

        @SuppressWarnings("unchecked")
        Map<String, Object> rulesDatabase = (Map<String, Object>) result.get("rulesDatabase");
        assertNotNull(rulesDatabase);
        assertEquals(expectedRulesCount, rulesDatabase.get("rulesCount"));
    }

    @Test
    void checkDatabases_WhenPrimaryDatabaseFails() {
        // Arrange
        String errorMessage = "Primary database connection failed";

        when(primaryJdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        Map<String, Object> result = databaseCheckController.checkDatabases();

        // Assert
        assertNotNull(result);
        assertEquals("ERROR", result.get("status"));
        assertEquals(errorMessage, result.get("message"));

        assertFalse(result.containsKey("primaryDatabase"));
        assertFalse(result.containsKey("rulesDatabase"));
    }

    @Test
    void checkDatabases_WhenRulesDatabaseFails() {
        // Arrange
        Integer expectedProductsCount = 100;
        String errorMessage = "Rules database connection failed";

        when(primaryJdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenReturn(expectedProductsCount);
        when(rulesJdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        Map<String, Object> result = databaseCheckController.checkDatabases();

        // Assert
        assertNotNull(result);
        assertEquals("ERROR", result.get("status"));
        assertEquals(errorMessage, result.get("message"));

        assertFalse(result.containsKey("primaryDatabase"));
        assertFalse(result.containsKey("rulesDatabase"));
    }

    @Test
    void checkDatabases_WithZeroCounts() {
        // Arrange
        Integer zeroProductsCount = 0;
        Integer zeroRulesCount = 0;

        when(primaryJdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenReturn(zeroProductsCount);
        when(rulesJdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenReturn(zeroRulesCount);

        // Act
        Map<String, Object> result = databaseCheckController.checkDatabases();

        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));

        @SuppressWarnings("unchecked")
        Map<String, Object> primaryDatabase = (Map<String, Object>) result.get("primaryDatabase");
        assertNotNull(primaryDatabase);
        assertEquals(zeroProductsCount, primaryDatabase.get("productsCount"));

        @SuppressWarnings("unchecked")
        Map<String, Object> rulesDatabase = (Map<String, Object>) result.get("rulesDatabase");
        assertNotNull(rulesDatabase);
        assertEquals(zeroRulesCount, rulesDatabase.get("rulesCount"));
    }
}
