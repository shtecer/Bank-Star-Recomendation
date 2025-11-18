package org.courseWork.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.courseWork.model.ProductRule;
import org.courseWork.model.RulesNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProductRuleRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public ProductRuleRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    private final RowMapper<ProductRule> ruleRowMapper = (rs, rowNum) -> {
        ProductRule rule = new ProductRule();
        rule.setId(UUID.fromString(rs.getString("id")));
        rule.setProductId(UUID.fromString(rs.getString("product_id")));
        rule.setRuleName(rs.getString("rule_name"));
        rule.setRuleDescription(rs.getString("rule_description"));
        rule.setConditionType(rs.getString("condition_type"));
        rule.setConditionJson(rs.getString("condition_json"));
        rule.setActive(rs.getBoolean("active"));
        return rule;
    };

    public List<ProductRule> findByProductId(UUID productId) {
        String sql = "SELECT id, product_id, rule_name, rule_description, condition_type, condition_json, active " +
                "FROM product_rules WHERE product_id = ? AND active = true";
        return jdbcTemplate.query(sql, ruleRowMapper, productId.toString());
    }

    public List<ProductRule> findAllActive() {
        String sql = "SELECT id, product_id, rule_name, rule_description, condition_type, condition_json, active " +
                "FROM product_rules WHERE active = true";
        return jdbcTemplate.query(sql, ruleRowMapper);
    }

    public ProductRule save(ProductRule rule) {
        if (rule.getId() == null) {
            rule.setId(UUID.randomUUID());
        }

        String sql = "INSERT INTO product_rules (id, product_id, rule_name, rule_description, condition_type, condition_json, active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                rule.getId().toString(),
                rule.getProductId().toString(),
                rule.getRuleName(),
                rule.getRuleDescription(),
                rule.getConditionType(),
                rule.getConditionJson(),
                rule.getActive()
        );

        return rule;
    }

    public void deleteById(UUID ruleId) {
        String sql = "DELETE FROM product_rules WHERE id = ?";
        int affectedRows = jdbcTemplate.update(sql, ruleId.toString());

        if (affectedRows == 0) {
            throw new RulesNotFoundException(ruleId);
        }
    }

    // Или альтернативный вариант - мягкое удаление
    public void softDeleteById(UUID ruleId) {
        String sql = "UPDATE product_rules SET active = false WHERE id = ?";
        int affectedRows = jdbcTemplate.update(sql, ruleId.toString());

        if (affectedRows == 0) {
            throw new RulesNotFoundException(ruleId);
        }
    }

    public Optional<ProductRule> findById(UUID ruleId) {
        String sql = "SELECT id, product_id, rule_name, rule_description, condition_type, condition_json, active " +
                "FROM product_rules WHERE id = ?";
        try {
            ProductRule rule = jdbcTemplate.queryForObject(sql, ruleRowMapper, ruleId.toString());
            return Optional.ofNullable(rule);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
