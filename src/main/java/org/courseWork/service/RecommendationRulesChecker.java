package org.courseWork.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RecommendationRulesChecker {

    private final JdbcTemplate jdbcTemplate;

    public RecommendationRulesChecker(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public boolean isProductAEligible(UUID userId) {
        String sql =
                "SELECT CASE WHEN " +
             "    EXISTS (SELECT 1 FROM TRANSACTIONS tr " +
             "             JOIN PRODUCTS pr ON tr.PRODUCT_ID = pr.ID " +
             "             WHERE tr.USER_ID = ? AND pr.TYPE = 'DEBIT') " +
             "    AND NOT EXISTS (SELECT 1 FROM TRANSACTIONS tr " +
             "                    JOIN PRODUCTS pr ON tr.PRODUCT_ID = pr.ID " +
             "                    WHERE tr.USER_ID = ? AND pr.TYPE = 'INVEST') " +
             "    AND (SELECT COALESCE(SUM(tr.AMOUNT), 0) FROM TRANSACTIONS tr " +
             "          JOIN PRODUCTS pr ON tr.PRODUCT_ID = pr.ID " +
             "          WHERE tr.USER_ID = ? AND pr.TYPE = 'SAVING' AND tr.TYPE = 'DEPOSIT') > 1000 " +
             "THEN 1 ELSE 0 END as is_eligible";

        try {
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, userId, userId, userId);
        return result != null && result == 1;
    } catch (Exception e) {
        System.err.println("Error checking product A eligibility for user " + userId + ": " + e.getMessage());
        return false;
    }
}

// Проверка условия для продукта Б
public boolean isProductBEligible(UUID userId) {
    String sql =
            "WITH debit_usage AS (" +
                    "    SELECT COUNT(DISTINCT tr.PRODUCT_ID) as debit_count " +
                    "    FROM TRANSACTIONS tr " +
                    "    JOIN PRODUCTS pr ON tr.PRODUCT_ID = pr.ID " +
                    "    WHERE tr.USER_ID = ? AND pr.TYPE = 'DEBIT'" +
                    "), transaction_totals AS (" +
                    "    SELECT " +
                    "        SUM(CASE WHEN pr.TYPE = 'DEBIT' AND tr.TYPE = 'DEPOSIT' THEN tr.AMOUNT ELSE 0 END) as debit_deposits," +
                    "        SUM(CASE WHEN pr.TYPE = 'DEBIT' AND tr.TYPE = 'WITHDRAWAL' THEN tr.AMOUNT ELSE 0 END) as debit_withdrawals," +
                    "        SUM(CASE WHEN pr.TYPE = 'SAVING' AND tr.TYPE = 'DEPOSIT' THEN tr.AMOUNT ELSE 0 END) as saving_deposits " +
                    "    FROM TRANSACTIONS tr " +
                    "    JOIN PRODUCTS pr ON tr.PRODUCT_ID = pr.ID " +
                    "    WHERE tr.USER_ID = ?" +
                    ") " +
                    "SELECT CASE WHEN " +
                    "    debit_count > 0 " +
                    "    AND (debit_deposits >= 50000 OR saving_deposits >= 50000) " +
                    "    AND debit_deposits > debit_withdrawals " +
                    "THEN 1 ELSE 0 END as is_eligible " +
                    "FROM debit_usage, transaction_totals";

    try {
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, userId, userId);
        return result != null && result == 1;
    } catch (Exception e) {
        System.err.println("Error checking product B eligibility for user " + userId + ": " + e.getMessage());
        return false;
    }
}

// Проверка условия для продукта В
public boolean isProductCEligible(UUID userId) {
    String sql =
            "WITH credit_usage AS (" +
                    "    SELECT COUNT(DISTINCT tr.PRODUCT_ID) as credit_count " +
                    "    FROM TRANSACTIONS tr " +
                    "    JOIN PRODUCTS pr ON tr.PRODUCT_ID = pr.ID " +
                    "    WHERE tr.USER_ID = ? AND pr.TYPE = 'CREDIT'" +
                    "), debit_totals AS (" +
                    "    SELECT " +
                    "        SUM(CASE WHEN tr.TYPE = 'DEPOSIT' THEN tr.AMOUNT ELSE 0 END) as debit_deposits," +
                    "        SUM(CASE WHEN tr.TYPE = 'WITHDRAWAL' THEN tr.AMOUNT ELSE 0 END) as debit_withdrawals " +
                    "    FROM TRANSACTIONS tr " +
                    "    JOIN PRODUCTS pr ON tr.PRODUCT_ID = pr.ID " +
                    "    WHERE tr.USER_ID = ? AND pr.TYPE = 'DEBIT'" +
                    ") " +
                    "SELECT CASE WHEN " +
                    "    credit_count = 0 " +
                    "    AND debit_deposits > debit_withdrawals " +
                    "    AND debit_withdrawals > 100000 " +
                    "THEN 1 ELSE 0 END as is_eligible " +
                    "FROM credit_usage, debit_totals";

    try {
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, userId, userId);
        return result != null && result == 1;
    } catch (Exception e) {
        System.err.println("Error checking product C eligibility for user " + userId + ": " + e.getMessage());
        return false;
    }
}
}