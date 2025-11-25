package org.courseWork.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/management")
public class DatabaseCheckController {

    private final JdbcTemplate primaryJdbcTemplate;
    private final JdbcTemplate rulesJdbcTemplate;

    public DatabaseCheckController(@Qualifier("primaryJdbcTemplate") JdbcTemplate primaryJdbcTemplate,
                                   @Qualifier("rulesJdbcTemplate") JdbcTemplate rulesJdbcTemplate) {
        this.primaryJdbcTemplate = primaryJdbcTemplate;
        this.rulesJdbcTemplate = rulesJdbcTemplate;
    }

    @GetMapping("/db-check")
    public Map<String, Object> checkDatabases() {
        try {
            // Проверяем основную БД
            Integer primaryCount = primaryJdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class);

            // Проверяем БД правил
            Integer rulesCount = rulesJdbcTemplate.queryForObject("SELECT COUNT(*) FROM recommendation_rules", Integer.class);

            return Map.of(
                    "status", "SUCCESS",
                    "primaryDatabase", Map.of("productsCount", primaryCount),
                    "rulesDatabase", Map.of("rulesCount", rulesCount)
            );

        } catch (Exception e) {
            return Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()
            );
        }
    }
}