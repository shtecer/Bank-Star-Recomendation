package org.courseWork.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class RecommendationRulesChecker {

    private final JdbcTemplate jdbcTemplate;

    public RecommendationRulesChecker(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    // Проверяет наличие продукта у пользователя
    public boolean hasProductType(UUID userId, String productType) {
        String sql = "SELECT COUNT(DISTINCT tr.product_id) " +
                "FROM transactions tr " +
                "JOIN products pr ON tr.product_id = pr.id " +
                "WHERE tr.user_id = ? AND pr.type = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, productType);
        return count != null && count > 0;
    }

    // Проверяет отсутствие продукта у пользователя
    public boolean hasNoProductType(UUID userId, String productType) {
        return !hasProductType(userId, productType);
    }

    // Проверяет минимальное количество транзакций
    public boolean hasMinTransactionCount(UUID userId, String productType, String transactionType, Integer minCount) {
        String sql = "SELECT COUNT(*) " +
                "FROM transactions tr " +
                "JOIN products pr ON tr.product_id = pr.id " +
                "WHERE tr.user_id = ? AND pr.type = ? AND tr.type = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, productType, transactionType);
        return count != null && count >= minCount;
    }

    // Проверяет минимальную сумму транзакций
    public boolean hasMinAmount(UUID userId, String productType, String transactionType, Double minAmount) {
        String sql = "SELECT COALESCE(SUM(tr.amount), 0) " +
                "FROM transactions tr " +
                "JOIN products pr ON tr.product_id = pr.id " +
                "WHERE tr.user_id = ? AND pr.type = ? AND tr.type = ?";

        Double totalAmount = jdbcTemplate.queryForObject(sql, Double.class, userId, productType, transactionType);
        return totalAmount != null && totalAmount >= minAmount;
    }

    // Сравнивает суммы разных типов транзакций
    public boolean compareAmounts(UUID userId, String productType, String comparisonType, Double comparisonAmount) {
        String sql = "SELECT " +
                "COALESCE(SUM(CASE WHEN tr.type = 'DEPOSIT' THEN tr.amount ELSE 0 END), 0) as deposits, " +
                "COALESCE(SUM(CASE WHEN tr.type = 'WITHDRAWAL' THEN tr.amount ELSE 0 END), 0) as withdrawals " +
                "FROM transactions tr " +
                "JOIN products pr ON tr.product_id = pr.id " +
                "WHERE tr.user_id = ? AND pr.type = ?";

        Map<String, Object> result = jdbcTemplate.queryForMap(sql, userId, productType);
        Double deposits = ((Number) result.get("deposits")).doubleValue();
        Double withdrawals = ((Number) result.get("withdrawals")).doubleValue();

        switch (comparisonType.toUpperCase()) {
            case "GREATER_THAN":
                return deposits > withdrawals && deposits > comparisonAmount;
            case "LESS_THAN":
                return deposits < withdrawals && deposits < comparisonAmount;
            case "EQUALS":
                return Math.abs(deposits - comparisonAmount) < 0.01; // Для double сравнения
            case "DEPOSITS_GT_WITHDRAWALS":
                return deposits > withdrawals;
            case "WITHDRAWALS_GT_AMOUNT":
                return withdrawals > comparisonAmount;
            default:
                return false;
        }
    }

    // Получает сумму транзакций по типу
    public Double getTransactionAmount(Long userId, String productType, String transactionType) {
        String sql = "SELECT COALESCE(SUM(tr.amount), 0) " +
                "FROM transactions tr " +
                "JOIN products pr ON tr.product_id = pr.id " +
                "WHERE tr.user_id = ? AND pr.type = ? AND tr.type = ?";

        return jdbcTemplate.queryForObject(sql, Double.class, userId, productType, transactionType);
    }
}