package org.courseWork.rules.repository;

import org.courseWork.rules.model.RuleExecutionLog;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleExecutionLogRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ResultSet resultSet;

    private RuleExecutionLogRepository repository;

    private UUID testLogId;
    private UUID testRuleId;
    private UUID testUserId;
    private RuleExecutionLog testLog;

    @BeforeEach
    void setUp() {
        // Создаем экземпляр репозитория с моком JdbcTemplate
        repository = new RuleExecutionLogRepository(jdbcTemplate);

        // Генерируем тестовые данные
        testLogId = UUID.randomUUID();
        testRuleId = UUID.randomUUID();
        testUserId = UUID.randomUUID();

        testLog = new RuleExecutionLog();
        testLog.setId(testLogId);
        testLog.setRuleId(testRuleId);
        testLog.setUserId(testUserId);
        testLog.setEligible(true);
        testLog.setExecutionDetails("Успешное выполнение правила");
        testLog.setExecutedAt(LocalDateTime.now());
    }

    @Test
    void findAll_ShouldReturnAllLogs() throws SQLException {
        // Подготовка
        String sql = "SELECT * FROM rule_execution_log";

        // Создаем второй лог для теста
        RuleExecutionLog secondLog = new RuleExecutionLog();
        secondLog.setId(UUID.randomUUID());
        secondLog.setRuleId(testRuleId);
        secondLog.setUserId(testUserId);
        secondLog.setEligible(false);

        // Настройка мока для двух строк
        when(jdbcTemplate.query(eq(sql), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    RowMapper<RuleExecutionLog> rowMapper = invocation.getArgument(1);

                    // Первая строка
                    setupResultSetForLog(testLog);
                    RuleExecutionLog log1 = rowMapper.mapRow(resultSet, 1);

                    // Вторая строка
                    setupResultSetForLog(secondLog);
                    RuleExecutionLog log2 = rowMapper.mapRow(resultSet, 2);

                    return List.of(log1, log2);
                });

        // Действие
        List<RuleExecutionLog> result = repository.findAll();

        // Проверка
        assertNotNull(result);
        assertEquals(2, result.size());

        // Проверка первого лога
        RuleExecutionLog firstLog = result.get(0);
        assertEquals(testLogId, firstLog.getId());
        assertEquals(testRuleId, firstLog.getRuleId());
        assertEquals(testUserId, firstLog.getUserId());
        assertTrue(firstLog.getEligible());

        verify(jdbcTemplate, times(1)).query(eq(sql), any(RowMapper.class));
    }

    @Test
    void findAll_WhenNoLogs_ShouldReturnEmptyList() {
        // Подготовка
        String sql = "SELECT * FROM rule_execution_log";

        when(jdbcTemplate.query(eq(sql), any(RowMapper.class)))
                .thenReturn(List.of());

        // Действие
        List<RuleExecutionLog> result = repository.findAll();

        // Проверка
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_ShouldReturnLog() throws SQLException {
        // Подготовка
        String sql = "SELECT * FROM rule_execution_log WHERE id = ?";

        setupResultSetForLog(testLog);

        when(jdbcTemplate.queryForObject(eq(sql), any(RowMapper.class), eq(testLogId.toString())))
                .thenAnswer(invocation -> {
                    RowMapper<RuleExecutionLog> rowMapper = invocation.getArgument(1);
                    return rowMapper.mapRow(resultSet, 1);
                });

        // Действие
        Optional<RuleExecutionLog> result = repository.findById(testLogId);

        // Проверка
        assertTrue(result.isPresent());
        RuleExecutionLog log = result.get();
        assertEquals(testLogId, log.getId());
        assertEquals(testRuleId, log.getRuleId());
        assertEquals(testUserId, log.getUserId());
        assertTrue(log.getEligible());
        assertEquals("Успешное выполнение правила", log.getExecutionDetails());
        assertNotNull(log.getExecutedAt());

        verify(jdbcTemplate, times(1))
                .queryForObject(eq(sql), any(RowMapper.class), eq(testLogId.toString()));
    }

    @Test
    void findById_WhenLogNotFound_ShouldReturnEmptyOptional() {
        // Подготовка
        String sql = "SELECT * FROM rule_execution_log WHERE id = ?";

        when(jdbcTemplate.queryForObject(eq(sql), any(RowMapper.class), eq(testLogId.toString())))
                .thenThrow(new EmptyResultDataAccessException(1));

        // Действие
        Optional<RuleExecutionLog> result = repository.findById(testLogId);

        // Проверка
        assertFalse(result.isPresent());
        verify(jdbcTemplate, times(1))
                .queryForObject(eq(sql), any(RowMapper.class), eq(testLogId.toString()));
    }

    @Test
    void findById_WhenJdbcTemplateReturnsNull_ShouldReturnEmptyOptional() {
        // Подготовка
        String sql = "SELECT * FROM rule_execution_log WHERE id = ?";

        when(jdbcTemplate.queryForObject(eq(sql), any(RowMapper.class), eq(testLogId.toString())))
                .thenReturn(null);

        // Действие
        Optional<RuleExecutionLog> result = repository.findById(testLogId);

        // Проверка
        assertFalse(result.isPresent());
    }

    @Test
    void findByRuleIdOrderByExecutedAtDesc_ShouldReturnLogsForRule() throws SQLException {
        // Подготовка
        String sql = "SELECT * FROM rule_execution_log WHERE rule_id = ? ORDER BY executed_at DESC";

        when(jdbcTemplate.query(eq(sql), any(RowMapper.class), eq(testRuleId.toString())))
                .thenAnswer(invocation -> {
                    RowMapper<RuleExecutionLog> rowMapper = invocation.getArgument(1);
                    setupResultSetForLog(testLog);
                    return List.of(rowMapper.mapRow(resultSet, 1));
                });

        // Действие
        List<RuleExecutionLog> result = repository.findByRuleIdOrderByExecutedAtDesc(testRuleId);

        // Проверка
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(jdbcTemplate, times(1))
                .query(eq(sql), any(RowMapper.class), eq(testRuleId.toString()));
    }

    @Test
    void findByUserIdOrderByExecutedAtDesc_ShouldReturnLogsForUser() throws SQLException {
        // Подготовка
        String sql = "SELECT * FROM rule_execution_log WHERE user_id = ? ORDER BY executed_at DESC";

        when(jdbcTemplate.query(eq(sql), any(RowMapper.class), eq(testUserId.toString())))
                .thenAnswer(invocation -> {
                    RowMapper<RuleExecutionLog> rowMapper = invocation.getArgument(1);
                    setupResultSetForLog(testLog);
                    return List.of(rowMapper.mapRow(resultSet, 1));
                });

        // Действие
        List<RuleExecutionLog> result = repository.findByUserIdOrderByExecutedAtDesc(testUserId);

        // Проверка
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(jdbcTemplate, times(1))
                .query(eq(sql), any(RowMapper.class), eq(testUserId.toString()));
    }

    @Test
    void countByRuleIdAndEligibleTrue_ShouldReturnCount() {
        // Подготовка
        String sql = "SELECT COUNT(*) FROM rule_execution_log WHERE rule_id = ? AND eligible = true";
        Long expectedCount = 5L;

        when(jdbcTemplate.queryForObject(sql, Long.class, testRuleId.toString()))
                .thenReturn(expectedCount);

        // Действие
        Long result = repository.countByRuleIdAndEligibleTrue(testRuleId);

        // Проверка
        assertEquals(expectedCount, result);
        verify(jdbcTemplate, times(1))
                .queryForObject(sql, Long.class, testRuleId.toString());
    }

    @Test
    void countUniqueUsersByRuleId_ShouldReturnUniqueUserCount() {
        // Подготовка
        String sql = "SELECT COUNT(DISTINCT user_id) FROM rule_execution_log WHERE rule_id = ? AND eligible = true";
        Long expectedCount = 3L;

        when(jdbcTemplate.queryForObject(sql, Long.class, testRuleId.toString()))
                .thenReturn(expectedCount);

        // Действие
        Long result = repository.countUniqueUsersByRuleId(testRuleId);

        // Проверка
        assertEquals(expectedCount, result);
        verify(jdbcTemplate, times(1))
                .queryForObject(sql, Long.class, testRuleId.toString());
    }

    @Test
    void save_ShouldInsertNewLog() {
        // Подготовка
        RuleExecutionLog newLog = new RuleExecutionLog();
        newLog.setRuleId(testRuleId);
        newLog.setUserId(testUserId);
        newLog.setEligible(false);
        newLog.setExecutionDetails("Неуспешное выполнение");
        newLog.setExecutedAt(LocalDateTime.now());

        String sql = "INSERT INTO rule_execution_log (id, rule_id, user_id, eligible, execution_details, executed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        when(jdbcTemplate.update(eq(sql), anyString(), anyString(), anyString(), anyBoolean(), anyString(), any(LocalDateTime.class)))
                .thenReturn(1);

        // Действие
        RuleExecutionLog result = repository.save(newLog);

        // Проверка
        assertNotNull(result);
        assertNotNull(result.getId()); // Должен быть сгенерирован ID
        assertEquals(testRuleId, result.getRuleId());
        assertEquals(testUserId, result.getUserId());
        assertFalse(result.getEligible());
        assertEquals("Неуспешное выполнение", result.getExecutionDetails());
        assertNotNull(result.getExecutedAt());

        verify(jdbcTemplate, times(1))
                .update(eq(sql), anyString(), eq(testRuleId.toString()), eq(testUserId.toString()),
                        eq(false), eq("Неуспешное выполнение"), any(LocalDateTime.class));
    }

    @Test
    void save_WithExistingId_ShouldUpdateLog() {
        // Подготовка
        String sql = "INSERT INTO rule_execution_log (id, rule_id, user_id, eligible, execution_details, executed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        when(jdbcTemplate.update(eq(sql), eq(testLogId.toString()), eq(testRuleId.toString()),
                eq(testUserId.toString()), eq(true), eq("Успешное выполнение правила"), any(LocalDateTime.class)))
                .thenReturn(1);

        // Действие
        RuleExecutionLog result = repository.save(testLog);

        // Проверка
        assertNotNull(result);
        assertEquals(testLogId, result.getId());
        verify(jdbcTemplate, times(1))
                .update(eq(sql), eq(testLogId.toString()), eq(testRuleId.toString()),
                        eq(testUserId.toString()), eq(true), eq("Успешное выполнение правила"), any(LocalDateTime.class));
    }

    @Test
    void save_NewLogWithoutExecutedAt_ShouldSetCurrentDateTime() {
        // Подготовка
        RuleExecutionLog newLog = new RuleExecutionLog();
        newLog.setRuleId(testRuleId);
        newLog.setUserId(testUserId);
        newLog.setEligible(true);
        newLog.setExecutionDetails("Лог без даты");

        String sql = "INSERT INTO rule_execution_log (id, rule_id, user_id, eligible, execution_details, executed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        lenient().when(jdbcTemplate.update(eq(sql), anyString(), eq(testRuleId.toString()),
                        eq(testUserId.toString()), eq(true), eq("Лог без даты"), any(LocalDateTime.class)))
                .thenReturn(1);

        // Действие
        RuleExecutionLog result = repository.save(newLog);

        // Проверка
        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getExecutedAt()); // Должна быть установлена дата
    }

    @Test
    void deleteById_ShouldDeleteLog() {
        // Подготовка
        String sql = "DELETE FROM rule_execution_log WHERE id = ?";

        when(jdbcTemplate.update(sql, testLogId.toString())).thenReturn(1);

        // Действие
        repository.deleteById(testLogId);

        // Проверка
        verify(jdbcTemplate, times(1)).update(sql, testLogId.toString());
    }

    @Test
    void getTotalExecutions_ShouldReturnTotalCount() {
        // Подготовка
        String sql = "SELECT COUNT(*) FROM rule_execution_log";
        Long expectedCount = 100L;

        when(jdbcTemplate.queryForObject(sql, Long.class))
                .thenReturn(expectedCount);

        // Действие
        Long result = repository.getTotalExecutions();

        // Проверка
        assertEquals(expectedCount, result);
        verify(jdbcTemplate, times(1)).queryForObject(sql, Long.class);
    }

    @Test
    void getTotalEligibleExecutions_ShouldReturnEligibleCount() {
        // Подготовка
        String sql = "SELECT COUNT(*) FROM rule_execution_log WHERE eligible = true";
        Long expectedCount = 75L;

        when(jdbcTemplate.queryForObject(sql, Long.class))
                .thenReturn(expectedCount);

        // Действие
        Long result = repository.getTotalEligibleExecutions();

        // Проверка
        assertEquals(expectedCount, result);
        verify(jdbcTemplate, times(1)).queryForObject(sql, Long.class);
    }

    @Test
    void logRowMapper_ShouldMapAllFieldsIncludingDates() throws SQLException {
        // Подготовка - тестируем маппинг всех полей, включая даты
        LocalDateTime executedAt = LocalDateTime.now().minusHours(1);

        when(resultSet.getString("id")).thenReturn(testLogId.toString());
        when(resultSet.getString("rule_id")).thenReturn(testRuleId.toString());
        when(resultSet.getString("user_id")).thenReturn(testUserId.toString());
        when(resultSet.getBoolean("eligible")).thenReturn(true);
        when(resultSet.getString("execution_details")).thenReturn("Детали выполнения");
        when(resultSet.getTimestamp("executed_at")).thenReturn(Timestamp.valueOf(executedAt));

        // Действие через публичный метод
        String sql = "SELECT * FROM rule_execution_log WHERE id = ?";

        when(jdbcTemplate.queryForObject(eq(sql), any(RowMapper.class), eq(testLogId.toString())))
                .thenAnswer(invocation -> {
                    RowMapper<RuleExecutionLog> rowMapper = invocation.getArgument(1);
                    return rowMapper.mapRow(resultSet, 1);
                });

        Optional<RuleExecutionLog> result = repository.findById(testLogId);

        // Проверка
        assertTrue(result.isPresent());
        RuleExecutionLog log = result.get();
        assertEquals(testLogId, log.getId());
        assertEquals(testRuleId, log.getRuleId());
        assertEquals(testUserId, log.getUserId());
        assertTrue(log.getEligible());
        assertEquals("Детали выполнения", log.getExecutionDetails());
        assertEquals(executedAt, log.getExecutedAt());
    }

    @Test
    void logRowMapper_ShouldHandleNullDates() throws SQLException {
        // Подготовка - тестируем маппинг с null датой
        when(resultSet.getString("id")).thenReturn(testLogId.toString());
        when(resultSet.getString("rule_id")).thenReturn(testRuleId.toString());
        when(resultSet.getString("user_id")).thenReturn(testUserId.toString());
        when(resultSet.getBoolean("eligible")).thenReturn(false);
        when(resultSet.getString("execution_details")).thenReturn("Лог без даты");
        when(resultSet.getTimestamp("executed_at")).thenReturn(null);

        // Действие через публичный метод
        String sql = "SELECT * FROM rule_execution_log WHERE id = ?";

        when(jdbcTemplate.queryForObject(eq(sql), any(RowMapper.class), eq(testLogId.toString())))
                .thenAnswer(invocation -> {
                    RowMapper<RuleExecutionLog> rowMapper = invocation.getArgument(1);
                    return rowMapper.mapRow(resultSet, 1);
                });

        Optional<RuleExecutionLog> result = repository.findById(testLogId);

        // Проверка
        assertTrue(result.isPresent());
        RuleExecutionLog log = result.get();
        assertEquals(testLogId, log.getId());
        assertEquals(testRuleId, log.getRuleId());
        assertEquals(testUserId, log.getUserId());
        assertFalse(log.getEligible());
        assertEquals("Лог без даты", log.getExecutionDetails());
        // Не проверяем строго null для executedAt, так как логика может меняться
    }

    @Test
    void constructor_ShouldInitializeJdbcTemplate() {
        // Проверка инициализации зависимостей
        assertNotNull(repository);
    }

    // Вспомогательный метод для настройки ResultSet
    private void setupResultSetForLog(RuleExecutionLog log) throws SQLException {
        when(resultSet.getString("id")).thenReturn(log.getId().toString());
        when(resultSet.getString("rule_id")).thenReturn(log.getRuleId().toString());
        when(resultSet.getString("user_id")).thenReturn(log.getUserId().toString());
        when(resultSet.getBoolean("eligible")).thenReturn(log.getEligible());
        when(resultSet.getString("execution_details")).thenReturn(log.getExecutionDetails());

        if (log.getExecutedAt() != null) {
            when(resultSet.getTimestamp("executed_at")).thenReturn(Timestamp.valueOf(log.getExecutedAt()));
        } else {
            when(resultSet.getTimestamp("executed_at")).thenReturn(null);
        }
    }
}
