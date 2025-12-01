package org.courseWork.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.courseWork.model.ProductRule;
import org.courseWork.model.RulesNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductRuleRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ResultSet resultSet;

    private ProductRuleRepository repository;

    private UUID testRuleId;
    private UUID testProductId;
    private ProductRule testRule;

    @BeforeEach
    void setUp() {
        // Создаем экземпляр репозитория с моками
        repository = new ProductRuleRepository(jdbcTemplate, objectMapper);

        // Генерируем тестовые данные
        testRuleId = UUID.randomUUID();
        testProductId = UUID.randomUUID();

        testRule = new ProductRule();
        testRule.setId(testRuleId);
        testRule.setProductId(testProductId);
        testRule.setRuleName("Тестовое правило");
        testRule.setRuleDescription("Тестовое описание правила");
        testRule.setConditionType("AGE_CONDITION");
        testRule.setConditionJson("{\"minAge\": 18}");
        testRule.setActive(true);
    }

    @Test
    void findByProductId_ShouldReturnActiveRules() throws SQLException {
        // Подготовка
        String sql = "SELECT id, product_id, rule_name, rule_description, condition_type, condition_json, active " +
                "FROM product_rules WHERE product_id = ? AND active = true";

        // Создаем второе правило для теста
        ProductRule secondRule = new ProductRule();
        secondRule.setId(UUID.randomUUID());
        secondRule.setProductId(testProductId);
        secondRule.setRuleName("Второе правило");
        secondRule.setActive(true);

        // Настройка мока для двух строк
        when(jdbcTemplate.query(eq(sql), any(RowMapper.class), eq(testProductId.toString())))
                .thenAnswer(invocation -> {
                    RowMapper<ProductRule> rowMapper = invocation.getArgument(1);

                    // Первая строка
                    setupResultSetForRule(testRule);
                    ProductRule rule1 = rowMapper.mapRow(resultSet, 1);

                    // Вторая строка
                    setupResultSetForRule(secondRule);
                    ProductRule rule2 = rowMapper.mapRow(resultSet, 2);

                    return List.of(rule1, rule2);
                });

        // Действие
        List<ProductRule> result = repository.findByProductId(testProductId);

        // Проверка
        assertNotNull(result);
        assertEquals(2, result.size());

        // Проверка первого правила
        ProductRule firstRule = result.get(0);
        assertEquals(testRuleId, firstRule.getId());
        assertEquals(testProductId, firstRule.getProductId());
        assertEquals("Тестовое правило", firstRule.getRuleName());

        // Проверка вызова JdbcTemplate
        verify(jdbcTemplate, times(1))
                .query(eq(sql), any(RowMapper.class), eq(testProductId.toString()));
    }

    @Test
    void findByProductId_WhenNoActiveRules_ShouldReturnEmptyList() {
        // Подготовка
        String sql = "SELECT id, product_id, rule_name, rule_description, condition_type, condition_json, active " +
                "FROM product_rules WHERE product_id = ? AND active = true";

        when(jdbcTemplate.query(eq(sql), any(RowMapper.class), eq(testProductId.toString())))
                .thenReturn(List.of());

        // Действие
        List<ProductRule> result = repository.findByProductId(testProductId);

        // Проверка
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllActive_ShouldReturnAllActiveRules() throws SQLException {
        // Подготовка
        String sql = "SELECT id, product_id, rule_name, rule_description, condition_type, condition_json, active " +
                "FROM product_rules WHERE active = true";

        UUID secondProductId = UUID.randomUUID();
        ProductRule secondRule = new ProductRule();
        secondRule.setId(UUID.randomUUID());
        secondRule.setProductId(secondProductId);
        secondRule.setRuleName("Правило для второго продукта");
        secondRule.setActive(true);

        when(jdbcTemplate.query(eq(sql), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    RowMapper<ProductRule> rowMapper = invocation.getArgument(1);

                    setupResultSetForRule(testRule);
                    ProductRule rule1 = rowMapper.mapRow(resultSet, 1);

                    setupResultSetForRule(secondRule);
                    ProductRule rule2 = rowMapper.mapRow(resultSet, 2);

                    return List.of(rule1, rule2);
                });

        // Действие
        List<ProductRule> result = repository.findAllActive();

        // Проверка
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jdbcTemplate, times(1)).query(eq(sql), any(RowMapper.class));
    }

    @Test
    void save_ShouldInsertNewRule() {
        // Подготовка
        ProductRule newRule = new ProductRule();
        newRule.setProductId(testProductId);
        newRule.setRuleName("Новое правило");
        newRule.setRuleDescription("Описание нового правила");
        newRule.setConditionType("BALANCE_CONDITION");
        newRule.setConditionJson("{\"minBalance\": 1000}");
        newRule.setActive(true);

        String sql = "INSERT INTO product_rules (id, product_id, rule_name, rule_description, condition_type, condition_json, active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        when(jdbcTemplate.update(eq(sql), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(1);

        // Действие
        ProductRule result = repository.save(newRule);

        // Проверка
        assertNotNull(result);
        assertNotNull(result.getId()); // Должен быть сгенерирован ID
        assertEquals(testProductId, result.getProductId());
        assertEquals("Новое правило", result.getRuleName());

        verify(jdbcTemplate, times(1))
                .update(eq(sql), anyString(), eq(testProductId.toString()), eq("Новое правило"),
                        eq("Описание нового правила"), eq("BALANCE_CONDITION"), eq("{\"minBalance\": 1000}"), eq(true));
    }

    @Test
    void save_WithExistingId_ShouldUpdateRule() {
        // Подготовка
        String sql = "INSERT INTO product_rules (id, product_id, rule_name, rule_description, condition_type, condition_json, active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        when(jdbcTemplate.update(eq(sql), eq(testRuleId.toString()), eq(testProductId.toString()),
                eq("Тестовое правило"), eq("Тестовое описание правила"),
                eq("AGE_CONDITION"), eq("{\"minAge\": 18}"), eq(true)))
                .thenReturn(1);

        // Действие
        ProductRule result = repository.save(testRule);

        // Проверка
        assertNotNull(result);
        assertEquals(testRuleId, result.getId());
        verify(jdbcTemplate, times(1))
                .update(eq(sql), eq(testRuleId.toString()), eq(testProductId.toString()),
                        eq("Тестовое правило"), eq("Тестовое описание правила"),
                        eq("AGE_CONDITION"), eq("{\"minAge\": 18}"), eq(true));
    }

    @Test
    void deleteById_ShouldDeleteRule() {
        // Подготовка
        String sql = "DELETE FROM product_rules WHERE id = ?";

        when(jdbcTemplate.update(sql, testRuleId.toString())).thenReturn(1);

        // Действие
        repository.deleteById(testRuleId);

        // Проверка
        verify(jdbcTemplate, times(1)).update(sql, testRuleId.toString());
    }

    @Test
    void deleteById_WhenRuleNotFound_ShouldThrowException() {
        // Подготовка
        String sql = "DELETE FROM product_rules WHERE id = ?";

        when(jdbcTemplate.update(sql, testRuleId.toString())).thenReturn(0);

        // Действие и проверка
        RulesNotFoundException exception = assertThrows(RulesNotFoundException.class, () -> {
            repository.deleteById(testRuleId);
        });

        // Проверяем сообщение исключения
        assertEquals("Rule not found with id: " + testRuleId, exception.getMessage());
        verify(jdbcTemplate, times(1)).update(sql, testRuleId.toString());
    }

    @Test
    void softDeleteById_ShouldDeactivateRule() {
        // Подготовка
        String sql = "UPDATE product_rules SET active = false WHERE id = ?";

        when(jdbcTemplate.update(sql, testRuleId.toString())).thenReturn(1);

        // Действие
        repository.softDeleteById(testRuleId);

        // Проверка
        verify(jdbcTemplate, times(1)).update(sql, testRuleId.toString());
    }

    @Test
    void softDeleteById_WhenRuleNotFound_ShouldThrowException() {
        // Подготовка
        String sql = "UPDATE product_rules SET active = false WHERE id = ?";

        when(jdbcTemplate.update(sql, testRuleId.toString())).thenReturn(0);

        // Действие и проверка
        RulesNotFoundException exception = assertThrows(RulesNotFoundException.class, () -> {
            repository.softDeleteById(testRuleId);
        });

        // Проверяем сообщение исключения
        assertEquals("Rule not found with id: " + testRuleId, exception.getMessage());
        verify(jdbcTemplate, times(1)).update(sql, testRuleId.toString());
    }

    @Test
    void findById_ShouldReturnRule() throws SQLException {
        // Подготовка
        String sql = "SELECT id, product_id, rule_name, rule_description, condition_type, condition_json, active " +
                "FROM product_rules WHERE id = ?";

        setupResultSetForRule(testRule);

        when(jdbcTemplate.queryForObject(eq(sql), any(RowMapper.class), eq(testRuleId.toString())))
                .thenAnswer(invocation -> {
                    RowMapper<ProductRule> rowMapper = invocation.getArgument(1);
                    return rowMapper.mapRow(resultSet, 1);
                });

        // Действие
        Optional<ProductRule> result = repository.findById(testRuleId);

        // Проверка
        assertTrue(result.isPresent());
        ProductRule rule = result.get();
        assertEquals(testRuleId, rule.getId());
        assertEquals(testProductId, rule.getProductId());
        assertEquals("Тестовое правило", rule.getRuleName());
        assertEquals("Тестовое описание правила", rule.getRuleDescription());
        assertEquals("AGE_CONDITION", rule.getConditionType());
        assertEquals("{\"minAge\": 18}", rule.getConditionJson());
        assertTrue(rule.getActive());

        verify(jdbcTemplate, times(1))
                .queryForObject(eq(sql), any(RowMapper.class), eq(testRuleId.toString()));
    }

    @Test
    void findById_WhenRuleNotFound_ShouldReturnEmptyOptional() {
        // Подготовка
        String sql = "SELECT id, product_id, rule_name, rule_description, condition_type, condition_json, active " +
                "FROM product_rules WHERE id = ?";

        when(jdbcTemplate.queryForObject(eq(sql), any(RowMapper.class), eq(testRuleId.toString())))
                .thenThrow(new EmptyResultDataAccessException(1));

        // Действие
        Optional<ProductRule> result = repository.findById(testRuleId);

        // Проверка
        assertFalse(result.isPresent());
        verify(jdbcTemplate, times(1))
                .queryForObject(eq(sql), any(RowMapper.class), eq(testRuleId.toString()));
    }

    @Test
    void findById_WhenJdbcTemplateReturnsNull_ShouldReturnEmptyOptional() {
        // Подготовка
        String sql = "SELECT id, product_id, rule_name, rule_description, condition_type, condition_json, active " +
                "FROM product_rules WHERE id = ?";

        when(jdbcTemplate.queryForObject(eq(sql), any(RowMapper.class), eq(testRuleId.toString())))
                .thenReturn(null);

        // Действие
        Optional<ProductRule> result = repository.findById(testRuleId);

        // Проверка
        assertFalse(result.isPresent());
        verify(jdbcTemplate, times(1))
                .queryForObject(eq(sql), any(RowMapper.class), eq(testRuleId.toString()));
    }

    @Test
    void constructor_ShouldInitializeDependencies() {
        // Проверка инициализации зависимостей
        assertNotNull(repository);
    }

    @Test
    void ruleRowMapper_ShouldMapAllFieldsCorrectly() throws SQLException {
        // Подготовка - тестируем маппинг всех полей
        when(resultSet.getString("id")).thenReturn(testRuleId.toString());
        when(resultSet.getString("product_id")).thenReturn(testProductId.toString());
        when(resultSet.getString("rule_name")).thenReturn("Тестовое правило");
        when(resultSet.getString("rule_description")).thenReturn("Тестовое описание");
        when(resultSet.getString("condition_type")).thenReturn("SALARY_CONDITION");
        when(resultSet.getString("condition_json")).thenReturn("{\"minSalary\": 50000}");
        when(resultSet.getBoolean("active")).thenReturn(false);

        // Действие через публичный метод
        String sql = "SELECT id, product_id, rule_name, rule_description, condition_type, condition_json, active " +
                "FROM product_rules WHERE id = ?";

        when(jdbcTemplate.queryForObject(eq(sql), any(RowMapper.class), eq(testRuleId.toString())))
                .thenAnswer(invocation -> {
                    RowMapper<ProductRule> rowMapper = invocation.getArgument(1);
                    return rowMapper.mapRow(resultSet, 1);
                });

        Optional<ProductRule> result = repository.findById(testRuleId);

        // Проверка
        assertTrue(result.isPresent());
        ProductRule rule = result.get();
        assertEquals(testRuleId, rule.getId());
        assertEquals(testProductId, rule.getProductId());
        assertEquals("Тестовое правило", rule.getRuleName());
        assertEquals("Тестовое описание", rule.getRuleDescription());
        assertEquals("SALARY_CONDITION", rule.getConditionType());
        assertEquals("{\"minSalary\": 50000}", rule.getConditionJson());
        assertFalse(rule.getActive());
    }

    // Вспомогательный метод для настройки ResultSet
    private void setupResultSetForRule(ProductRule rule) throws SQLException {
        when(resultSet.getString("id")).thenReturn(rule.getId().toString());
        when(resultSet.getString("product_id")).thenReturn(rule.getProductId().toString());
        when(resultSet.getString("rule_name")).thenReturn(rule.getRuleName());
        when(resultSet.getString("rule_description")).thenReturn(rule.getRuleDescription());
        when(resultSet.getString("condition_type")).thenReturn(rule.getConditionType());
        when(resultSet.getString("condition_json")).thenReturn(rule.getConditionJson());
        when(resultSet.getBoolean("active")).thenReturn(rule.getActive());
    }
}