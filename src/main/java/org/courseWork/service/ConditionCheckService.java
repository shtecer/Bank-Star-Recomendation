
package org.courseWork.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.courseWork.rules.model.RuleCondition;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ConditionCheckService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public ConditionCheckService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    //условия проверки
    public boolean evaluateCondition(String conditionType, String conditionJson, UUID userId) {
        try {
            RuleCondition condition = objectMapper.readValue(conditionJson, RuleCondition.class);

            switch (conditionType.toUpperCase()) {
                case "HAS_PRODUCT":
                    return hasProductType(userId, condition.getProductType());

                case "NO_PRODUCT":
                    return hasNoProductType(userId, condition.getProductType());

                case "MIN_AMOUNT":
                    return hasMinAmount(userId, condition.getProductType(),
                            condition.getTransactionType(), condition.getMinAmount());

                case "MIN_TRANSACTION_COUNT":
                    return hasMinTransactionCount(userId, condition.getProductType(),
                            condition.getTransactionType(), condition.getMinCount());

                case "AMOUNT_COMPARISON":
                    return compareAmounts(userId, condition.getProductType(),
                            condition.getComparisonType(), condition.getComparisonAmount());

                default:
                    log.warn("Unknown condition type: {}", conditionType);
                    return false;
            }
        } catch (Exception e) {
            log.error("Error evaluating condition: {}", conditionType, e);
            return false;
        }
    }

    //проверка наличия продукта у пользователя
    public boolean hasProductType(UUID userId, String productType) {
        String sql = """
            SELECT COUNT(DISTINCT tr.product_id) 
            FROM transactions tr 
            JOIN products pr ON tr.product_id = pr.id 
            WHERE tr.user_id = ? AND pr.type = ?
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, productType);
        return count != null && count > 0;
    }

    //проверка отсутствия продукта у пользователя
    public boolean hasNoProductType(UUID userId, String productType) {
        return !hasProductType(userId, productType);
    }

    //минимальная сумма транзакций
    public boolean hasMinAmount(UUID userId, String productType, String transactionType, Double minAmount) {
        if (minAmount == null) return false;

        String sql = """
            SELECT COALESCE(SUM(tr.amount), 0) 
            FROM transactions tr 
            JOIN products pr ON tr.product_id = pr.id 
            WHERE tr.user_id = ? AND pr.type = ? AND tr.type = ?
            """;

        Double totalAmount = jdbcTemplate.queryForObject(sql, Double.class, userId, productType, transactionType);
        return totalAmount != null && totalAmount >= minAmount;
    }

    //минимальное количество транзакций
    public boolean hasMinTransactionCount(UUID userId, String productType, String transactionType, Integer minCount) {
        if (minCount == null) return false;

        String sql = """
            SELECT COUNT(*) 
            FROM transactions tr 
            JOIN products pr ON tr.product_id = pr.id 
            WHERE tr.user_id = ? AND pr.type = ? AND tr.type = ?
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, productType, transactionType);
        return count != null && count >= minCount;
    }

    //сравнение суммы транзакций
    public boolean compareAmounts(UUID userId, String productType, String comparisonType, Double comparisonAmount) {
        if (comparisonType == null || comparisonAmount == null) return false;

        String sql = """
            SELECT 
                COALESCE(SUM(CASE WHEN tr.type = 'DEPOSIT' THEN tr.amount ELSE 0 END), 0) as deposits,
                COALESCE(SUM(CASE WHEN tr.type = 'WITHDRAWAL' THEN tr.amount ELSE 0 END), 0) as withdrawals
            FROM transactions tr 
            JOIN products pr ON tr.product_id = pr.id 
            WHERE tr.user_id = ? AND pr.type = ?
            """;

        try {
            var result = jdbcTemplate.queryForMap(sql, userId, productType);
            Double deposits = ((Number) result.get("deposits")).doubleValue();
            Double withdrawals = ((Number) result.get("withdrawals")).doubleValue();

            switch (comparisonType.toUpperCase()) {
                case "GREATER_THAN":
                    return deposits > comparisonAmount;
                case "LESS_THAN":
                    return deposits < comparisonAmount;
                case "EQUALS":
                    return Math.abs(deposits - comparisonAmount) < 0.01;
                case "GREATER_THAN_OR_EQUAL":
                    return deposits >= comparisonAmount;
                case "LESS_THAN_OR_EQUAL":
                    return deposits <= comparisonAmount;
                case "DEPOSITS_GT_WITHDRAWALS":
                    return deposits > withdrawals;
                case "WITHDRAWALS_GT_AMOUNT":
                    return withdrawals > comparisonAmount;
                default:
                    log.warn("Unknown comparison type: {}", comparisonType);
                    return false;
            }
        } catch (Exception e) {
            log.error("Error comparing amounts for user {} and product {}", userId, productType, e);
            return false;
        }
    }

    //комплексная проверка
    public boolean checkComplexCondition(UUID userId, List<RuleCondition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return false;
        }

        // Все условия должны быть выполнены (AND логика)
        return conditions.stream()
                .allMatch(condition -> evaluateCondition(condition.getType(),
                        convertToJson(condition), userId));
    }

    //конвертация в JSON
    private String convertToJson(RuleCondition condition) {
        try {
            return objectMapper.writeValueAsString(condition);
        } catch (Exception e) {
            log.error("Error converting condition to JSON", e);
            return "{}";
        }
    }

    //статистика транзакций
    public UserTransactionStats getUserTransactionStats(UUID userId, String productType) {
        String sql = """
            SELECT 
                COUNT(*) as transaction_count,
                COALESCE(SUM(CASE WHEN tr.type = 'DEPOSIT' THEN tr.amount ELSE 0 END), 0) as total_deposits,
                COALESCE(SUM(CASE WHEN tr.type = 'WITHDRAWAL' THEN tr.amount ELSE 0 END), 0) as total_withdrawals,
                COUNT(DISTINCT tr.product_id) as unique_products
            FROM transactions tr 
            JOIN products pr ON tr.product_id = pr.id 
            WHERE tr.user_id = ? AND (? IS NULL OR pr.type = ?)
            """;

        try {
            var result = jdbcTemplate.queryForMap(sql, userId, productType, productType);
            return new UserTransactionStats(
                    ((Number) result.get("transaction_count")).longValue(),
                    ((Number) result.get("total_deposits")).doubleValue(),
                    ((Number) result.get("total_withdrawals")).doubleValue(),
                    ((Number) result.get("unique_products")).intValue()
            );
        } catch (Exception e) {
            log.error("Error getting user transaction stats", e);
            return new UserTransactionStats(0L, 0.0, 0.0, 0);
        }
    }

    @Data
    @AllArgsConstructor
    public static class UserTransactionStats {
        private Long transactionCount;
        private Double totalDeposits;
        private Double totalWithdrawals;
        private Integer uniqueProducts;
    }
}