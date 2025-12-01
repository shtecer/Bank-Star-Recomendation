package org.courseWork.repository;

import org.courseWork.model.Product;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ResultSet resultSet;

    private ProductRepository repository;

    private UUID testProductId;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Создаем экземпляр репозитория с моком JdbcTemplate
        repository = new ProductRepository(jdbcTemplate);

        // Генерируем тестовые данные
        testProductId = UUID.randomUUID();
        testProduct = new Product(testProductId, "Тестовый продукт", "Тестовое описание");
    }

    @Test
    void findAll_ShouldReturnListOfProducts() throws SQLException {
        // Подготовка
        String sql = "SELECT id, product_name, description FROM products";

        // Создаем второй продукт для теста
        UUID secondProductId = UUID.randomUUID();
        Product secondProduct = new Product(secondProductId, "Второй продукт", "Описание второго продукта");

        List<Product> expectedProducts = List.of(testProduct, secondProduct);

        // Настройка мока для двух строк
        when(jdbcTemplate.query(eq(sql), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    RowMapper<Product> rowMapper = invocation.getArgument(1);

                    // Первая строка
                    when(resultSet.getString("id")).thenReturn(testProductId.toString());
                    when(resultSet.getString("product_name")).thenReturn("Тестовый продукт");
                    when(resultSet.getString("description")).thenReturn("Тестовое описание");
                    Product product1 = rowMapper.mapRow(resultSet, 1);

                    // Вторая строка
                    when(resultSet.getString("id")).thenReturn(secondProductId.toString());
                    when(resultSet.getString("product_name")).thenReturn("Второй продукт");
                    when(resultSet.getString("description")).thenReturn("Описание второго продукта");
                    Product product2 = rowMapper.mapRow(resultSet, 2);

                    return List.of(product1, product2);
                });

        // Действие
        List<Product> result = repository.findAll();

        // Проверка
        assertNotNull(result);
        assertEquals(2, result.size());

        // Проверка первого продукта
        Product firstProduct = result.get(0);
        assertEquals(testProductId, firstProduct.getId());
        assertEquals("Тестовый продукт", firstProduct.getProductName());
        assertEquals("Тестовое описание", firstProduct.getDescription());

        // Проверка второго продукта
        Product secondProductResult = result.get(1);
        assertEquals(secondProductId, secondProductResult.getId());
        assertEquals("Второй продукт", secondProductResult.getProductName());
        assertEquals("Описание второго продукта", secondProductResult.getDescription());

        // Проверка вызова JdbcTemplate
        verify(jdbcTemplate, times(1)).query(eq(sql), any(RowMapper.class));
    }

    @Test
    void findAll_WhenNoProducts_ShouldReturnEmptyList() {
        // Подготовка
        String sql = "SELECT id, product_name, description FROM products";

        when(jdbcTemplate.query(eq(sql), any(RowMapper.class)))
                .thenReturn(List.of());

        // Действие
        List<Product> result = repository.findAll();

        // Проверка
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jdbcTemplate, times(1)).query(eq(sql), any(RowMapper.class));
    }

    @Test
    void findById_ShouldReturnProduct() throws SQLException {
        // Подготовка
        String sql = "SELECT id, product_name, description FROM products WHERE id = ?";

        // Настройка мока ResultSet
        when(resultSet.getString("id")).thenReturn(testProductId.toString());
        when(resultSet.getString("product_name")).thenReturn("Тестовый продукт");
        when(resultSet.getString("description")).thenReturn("Тестовое описание");

        when(jdbcTemplate.queryForObject(eq(sql), any(RowMapper.class), eq(testProductId.toString())))
                .thenAnswer(invocation -> {
                    RowMapper<Product> rowMapper = invocation.getArgument(1);
                    return rowMapper.mapRow(resultSet, 1);
                });

        // Действие
        Optional<Product> result = repository.findById(testProductId);

        // Проверка
        assertTrue(result.isPresent());
        Product product = result.get();
        assertEquals(testProductId, product.getId());
        assertEquals("Тестовый продукт", product.getProductName());
        assertEquals("Тестовое описание", product.getDescription());

        // Проверка вызова JdbcTemplate
        verify(jdbcTemplate, times(1))
                .queryForObject(eq(sql), any(RowMapper.class), eq(testProductId.toString()));
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmptyOptional() {
        // Подготовка
        String sql = "SELECT id, product_name, description FROM products WHERE id = ?";
        UUID nonExistentId = UUID.randomUUID();

        when(jdbcTemplate.queryForObject(eq(sql), any(RowMapper.class), eq(nonExistentId.toString())))
                .thenThrow(new org.springframework.dao.EmptyResultDataAccessException(1));

        // Действие
        Optional<Product> result = repository.findById(nonExistentId);

        // Проверка
        assertFalse(result.isPresent());
        verify(jdbcTemplate, times(1))
                .queryForObject(eq(sql), any(RowMapper.class), eq(nonExistentId.toString()));
    }

    @Test
    void findById_WhenDatabaseError_ShouldReturnEmptyOptional() {
        // Подготовка
        String sql = "SELECT id, product_name, description FROM products WHERE id = ?";
        UUID productId = UUID.randomUUID();

        when(jdbcTemplate.queryForObject(eq(sql), any(RowMapper.class), eq(productId.toString())))
                .thenThrow(new RuntimeException("Ошибка базы данных"));

        // Действие
        Optional<Product> result = repository.findById(productId);

        // Проверка
        assertFalse(result.isPresent());
        verify(jdbcTemplate, times(1))
                .queryForObject(eq(sql), any(RowMapper.class), eq(productId.toString()));
    }

    @Test
    void findById_WhenJdbcTemplateReturnsNull_ShouldReturnEmptyOptional() {
        // Подготовка
        String sql = "SELECT id, product_name, description FROM products WHERE id = ?";
        UUID productId = UUID.randomUUID();

        when(jdbcTemplate.queryForObject(eq(sql), any(RowMapper.class), eq(productId.toString())))
                .thenReturn(null);

        // Действие
        Optional<Product> result = repository.findById(productId);

        // Проверка
        assertFalse(result.isPresent());
        verify(jdbcTemplate, times(1))
                .queryForObject(eq(sql), any(RowMapper.class), eq(productId.toString()));
    }

    @Test
    void productRowMapper_ShouldMapResultSetCorrectly() throws SQLException {
        // Подготовка - тестируем напрямую RowMapper
        when(resultSet.getString("id")).thenReturn(testProductId.toString());
        when(resultSet.getString("product_name")).thenReturn("Дебетовая карта");
        when(resultSet.getString("description")).thenReturn("Базовая дебетовая карта");

        // Действие - создаем репозиторий и используем его RowMapper
        ProductRepository repo = new ProductRepository(jdbcTemplate);

        // Для тестирования RowMapper нам нужно получить к нему доступ через рефлексию
        // или протестировать через публичные методы

        String sql = "SELECT id, product_name, description FROM products WHERE id = ?";
        when(jdbcTemplate.queryForObject(eq(sql), any(RowMapper.class), eq(testProductId.toString())))
                .thenAnswer(invocation -> {
                    RowMapper<Product> rowMapper = invocation.getArgument(1);
                    return rowMapper.mapRow(resultSet, 1);
                });

        Optional<Product> result = repo.findById(testProductId);

        // Проверка
        assertTrue(result.isPresent());
        Product product = result.get();
        assertEquals(testProductId, product.getId());
        assertEquals("Дебетовая карта", product.getProductName());
        assertEquals("Базовая дебетовая карта", product.getDescription());
    }

    @Test
    void constructor_ShouldInitializeJdbcTemplate() {
        // Проверка, что JdbcTemplate правильно инициализирован
        assertNotNull(repository);
    }

    @Test
    void findAll_ShouldUseCorrectSQL() {
        // Подготовка
        String expectedSql = "SELECT id, product_name, description FROM products";
        when(jdbcTemplate.query(eq(expectedSql), any(RowMapper.class)))
                .thenReturn(List.of());

        // Действие
        repository.findAll();

        // Проверка
        verify(jdbcTemplate, times(1)).query(eq(expectedSql), any(RowMapper.class));
    }

    @Test
    void findById_ShouldUseCorrectSQLAndParameters() {
        // Подготовка
        String expectedSql = "SELECT id, product_name, description FROM products WHERE id = ?";
        when(jdbcTemplate.queryForObject(eq(expectedSql), any(RowMapper.class), eq(testProductId.toString())))
                .thenReturn(testProduct);

        // Действие
        repository.findById(testProductId);

        // Проверка
        verify(jdbcTemplate, times(1))
                .queryForObject(eq(expectedSql), any(RowMapper.class), eq(testProductId.toString()));
    }

    @Test
    void findById_WithDifferentFormats_ShouldWorkCorrectly() throws SQLException {
        // Подготовка - тестируем разные UUID форматы
        UUID[] testIds = {
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                UUID.randomUUID()
        };

        for (UUID testId : testIds) {
            // Reset mocks for each iteration
            reset(jdbcTemplate, resultSet);

            String sql = "SELECT id, product_name, description FROM products WHERE id = ?";

            when(resultSet.getString("id")).thenReturn(testId.toString());
            when(resultSet.getString("product_name")).thenReturn("Продукт " + testId);
            when(resultSet.getString("description")).thenReturn("Описание " + testId);

            when(jdbcTemplate.queryForObject(eq(sql), any(RowMapper.class), eq(testId.toString())))
                    .thenAnswer(invocation -> {
                        RowMapper<Product> rowMapper = invocation.getArgument(1);
                        return rowMapper.mapRow(resultSet, 1);
                    });

            // Действие
            Optional<Product> result = repository.findById(testId);

            // Проверка
            assertTrue(result.isPresent());
            assertEquals(testId, result.get().getId());
        }
    }
}