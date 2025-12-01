package org.courseWork.repository;

import org.courseWork.model.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankClientsRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ResultSet resultSet;

    private BankClientsRepository repository;

    private UUID testUserId;
    private Client testClient;

    @BeforeEach
    void setUp() {
        // Создаем экземпляр репозитория с моком JdbcTemplate
        repository = new BankClientsRepository(jdbcTemplate);

        // Генерируем тестовые данные
        testUserId = UUID.randomUUID();
        testClient = new Client(testUserId, "testuser", "Иван", "Иванов");
    }

    @Test
    void findById_ShouldReturnClient() throws SQLException {
        // Подготовка
        String sql = "SELECT * FROM PUBLIC.USERS WHERE ID = ?";

        // Настройка мока ResultSet
        when(resultSet.getString("ID")).thenReturn(testUserId.toString());
        when(resultSet.getString("USERNAME")).thenReturn("testuser");
        when(resultSet.getString("FIRST_NAME")).thenReturn("Иван");
        when(resultSet.getString("LAST_NAME")).thenReturn("Иванов");

        when(jdbcTemplate.queryForObject(eq(sql), any(Object[].class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    RowMapper<Client> rowMapper = invocation.getArgument(2);
                    return rowMapper.mapRow(resultSet, 1);
                });

        // Действие
        Client result = repository.findById(testUserId);

        // Проверка
        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals("testuser", result.getUserName());
        assertEquals("Иван", result.getFirstName());
        assertEquals("Иванов", result.getLastName());

        // Проверка вызова JdbcTemplate
        verify(jdbcTemplate, times(1))
                .queryForObject(eq(sql), eq(new Object[]{testUserId.toString()}), any(RowMapper.class));
    }

    @Test
    void findById_WithNonExistentId_ShouldThrowException() {
        // Подготовка
        String sql = "SELECT * FROM PUBLIC.USERS WHERE ID = ?";
        UUID nonExistentId = UUID.randomUUID();

        when(jdbcTemplate.queryForObject(eq(sql), eq(new Object[]{nonExistentId.toString()}), any(RowMapper.class)))
                .thenThrow(new org.springframework.dao.EmptyResultDataAccessException(1));

        // Действие и проверка
        assertThrows(org.springframework.dao.EmptyResultDataAccessException.class, () -> {
            repository.findById(nonExistentId);
        });
    }

    @Test
    void findAll_ShouldReturnListOfClients() throws SQLException {
        // Подготовка
        String sql = "SELECT * FROM PUBLIC.USERS";

        // Создаем второго клиента для теста
        UUID secondUserId = UUID.randomUUID();
        Client secondClient = new Client(secondUserId, "seconduser", "Петр", "Петров");

        List<Client> expectedClients = List.of(testClient, secondClient);

        // Настройка мока ResultSet для двух строк
        when(jdbcTemplate.query(eq(sql), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    RowMapper<Client> rowMapper = invocation.getArgument(1);

                    // Первая строка
                    when(resultSet.getObject("ID")).thenReturn(testUserId);
                    when(resultSet.getString("USERNAME")).thenReturn("testuser");
                    when(resultSet.getString("FIRST_NAME")).thenReturn("Иван");
                    when(resultSet.getString("LAST_NAME")).thenReturn("Иванов");
                    Client client1 = rowMapper.mapRow(resultSet, 1);

                    // Вторая строка
                    when(resultSet.getObject("ID")).thenReturn(secondUserId);
                    when(resultSet.getString("USERNAME")).thenReturn("seconduser");
                    when(resultSet.getString("FIRST_NAME")).thenReturn("Петр");
                    when(resultSet.getString("LAST_NAME")).thenReturn("Петров");
                    Client client2 = rowMapper.mapRow(resultSet, 2);

                    return List.of(client1, client2);
                });

        // Действие
        List<Client> result = repository.findAll();

        // Проверка
        assertNotNull(result);
        assertEquals(2, result.size());

        // Проверка первого клиента
        Client firstClient = result.get(0);
        assertEquals(testUserId, firstClient.getId());
        assertEquals("testuser", firstClient.getUserName());
        assertEquals("Иван", firstClient.getFirstName());
        assertEquals("Иванов", firstClient.getLastName());

        // Проверка второго клиента
        Client secondClientResult = result.get(1);
        assertEquals(secondUserId, secondClientResult.getId());
        assertEquals("seconduser", secondClientResult.getUserName());
        assertEquals("Петр", secondClientResult.getFirstName());
        assertEquals("Петров", secondClientResult.getLastName());

        // Проверка вызова JdbcTemplate
        verify(jdbcTemplate, times(1)).query(eq(sql), any(RowMapper.class));
    }

    @Test
    void findAll_WhenNoClients_ShouldReturnEmptyList() {
        // Подготовка
        String sql = "SELECT * FROM PUBLIC.USERS";

        when(jdbcTemplate.query(eq(sql), any(RowMapper.class)))
                .thenReturn(List.of());

        // Действие
        List<Client> result = repository.findAll();

        // Проверка
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jdbcTemplate, times(1)).query(eq(sql), any(RowMapper.class));
    }

    @Test
    void existsById_ShouldAlwaysReturnFalse() {
        // Подготовка
        Long testId = 1L;

        // Действие
        boolean result = repository.existsById(testId);

        // Проверка
        assertFalse(result);
    }

    @Test
    void existsById_WithDifferentIds_ShouldAlwaysReturnFalse() {
        // Проверка для разных ID
        assertFalse(repository.existsById(1L));
        assertFalse(repository.existsById(100L));
        assertFalse(repository.existsById(-1L));
    }

    @Test
    void constructor_ShouldInitializeJdbcTemplate() {
        // Проверка, что JdbcTemplate правильно инициализирован
        assertNotNull(repository);
    }

    @Test
    void findById_RowMapper_ShouldMapResultSetCorrectly() throws SQLException {
        // Подготовка
        String sql = "SELECT * FROM PUBLIC.USERS WHERE ID = ?";

        // Настройка мока ResultSet
        when(resultSet.getString("ID")).thenReturn(testUserId.toString());
        when(resultSet.getString("USERNAME")).thenReturn("testuser");
        when(resultSet.getString("FIRST_NAME")).thenReturn("Иван");
        when(resultSet.getString("LAST_NAME")).thenReturn("Иванов");

        when(jdbcTemplate.queryForObject(eq(sql), any(Object[].class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    RowMapper<Client> rowMapper = invocation.getArgument(2);
                    return rowMapper.mapRow(resultSet, 1);
                });

        // Действие
        Client result = repository.findById(testUserId);

        // Проверка маппинга полей
        assertEquals(testUserId, result.getId());
        assertEquals("testuser", result.getUserName());
        assertEquals("Иван", result.getFirstName());
        assertEquals("Иванов", result.getLastName());
    }

    @Test
    void findAll_RowMapper_ShouldMapMultipleRowsCorrectly() throws SQLException {
        // Подготовка
        String sql = "SELECT * FROM PUBLIC.USERS";

        UUID secondUserId = UUID.randomUUID();

        when(jdbcTemplate.query(eq(sql), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    RowMapper<Client> rowMapper = invocation.getArgument(1);

                    // Первая строка
                    when(resultSet.getObject("ID")).thenReturn(testUserId);
                    when(resultSet.getString("USERNAME")).thenReturn("user1");
                    when(resultSet.getString("FIRST_NAME")).thenReturn("Анна");
                    when(resultSet.getString("LAST_NAME")).thenReturn("Смирнова");
                    Client client1 = rowMapper.mapRow(resultSet, 1);

                    // Вторая строка
                    when(resultSet.getObject("ID")).thenReturn(secondUserId);
                    when(resultSet.getString("USERNAME")).thenReturn("user2");
                    when(resultSet.getString("FIRST_NAME")).thenReturn("Сергей");
                    when(resultSet.getString("LAST_NAME")).thenReturn("Кузнецов");
                    Client client2 = rowMapper.mapRow(resultSet, 2);

                    return List.of(client1, client2);
                });

        // Действие
        List<Client> result = repository.findAll();

        // Проверка
        assertEquals(2, result.size());

        Client firstClient = result.get(0);
        assertEquals(testUserId, firstClient.getId());
        assertEquals("user1", firstClient.getUserName());
        assertEquals("Анна", firstClient.getFirstName());
        assertEquals("Смирнова", firstClient.getLastName());

        Client secondClient = result.get(1);
        assertEquals(secondUserId, secondClient.getId());
        assertEquals("user2", secondClient.getUserName());
        assertEquals("Сергей", secondClient.getFirstName());
        assertEquals("Кузнецов", secondClient.getLastName());
    }
}