package org.courseWork.rules.repository;

import org.courseWork.rules.model.RecommendationRule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RecommendationRuleRepository {

    private final JdbcTemplate jdbcTemplate;

    public RecommendationRuleRepository(@Qualifier("rulesJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<RecommendationRule> ruleRowMapper = (rs, rowNum) -> {
        RecommendationRule rule = new RecommendationRule();
        rule.setId(UUID.fromString(rs.getString("id")));
        rule.setName(rs.getString("name"));
        rule.setDescription(rs.getString("description"));
        rule.setProductType(rs.getString("product_type"));
        rule.setConditionType(rs.getString("condition_type"));
        rule.setConditionJson(rs.getString("condition_json"));
        rule.setPriority(rs.getInt("priority"));
        rule.setActive(rs.getBoolean("active"));

        // Обработка дат
        if (rs.getTimestamp("created_at") != null) {
            rule.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            rule.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }

        return rule;
    };

    public List<RecommendationRule> findAll() {
        String sql = "SELECT * FROM recommendation_rules";
        return jdbcTemplate.query(sql, ruleRowMapper);
    }

    public Optional<RecommendationRule> findById(UUID id) {
        String sql = "SELECT * FROM recommendation_rules WHERE id = ?";
        try {
            RecommendationRule rule = jdbcTemplate.queryForObject(sql, ruleRowMapper, id.toString());
            return Optional.ofNullable(rule);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<RecommendationRule> findByActiveTrueOrderByPriorityDesc() {
        String sql = "SELECT * FROM recommendation_rules WHERE active = true ORDER BY priority DESC";
        return jdbcTemplate.query(sql, ruleRowMapper);
    }

    public List<RecommendationRule> findByProductTypeAndActiveTrue(String productType) {
        String sql = "SELECT * FROM recommendation_rules WHERE product_type = ? AND active = true";
        return jdbcTemplate.query(sql, ruleRowMapper, productType);
    }

    public List<RecommendationRule> findByConditionTypeAndActiveTrue(String conditionType) {
        String sql = "SELECT * FROM recommendation_rules WHERE condition_type = ? AND active = true";
        return jdbcTemplate.query(sql, ruleRowMapper, conditionType);
    }

    public List<RecommendationRule> findByConditionJsonContaining(String condition) {
        String sql = "SELECT * FROM recommendation_rules WHERE active = true AND condition_json LIKE ?";
        return jdbcTemplate.query(sql, ruleRowMapper, "%" + condition + "%");
    }

    public RecommendationRule save(RecommendationRule rule) {
        if (rule.getId() == null) {
            // INSERT
            rule.setId(UUID.randomUUID());
            String sql = "INSERT INTO recommendation_rules (id, name, description, product_type, condition_type, condition_json, priority, active, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            jdbcTemplate.update(sql,
                    rule.getId().toString(),
                    rule.getName(),
                    rule.getDescription(),
                    rule.getProductType(),
                    rule.getConditionType(),
                    rule.getConditionJson(),
                    rule.getPriority(),
                    rule.getActive(),
                    rule.getCreatedAt() != null ? rule.getCreatedAt() : LocalDateTime.now()
            );
        } else {
            // UPDATE
            String sql = "UPDATE recommendation_rules SET name = ?, description = ?, product_type = ?, condition_type = ?, " +
                    "condition_json = ?, priority = ?, active = ?, updated_at = ? WHERE id = ?";

            jdbcTemplate.update(sql,
                    rule.getName(),
                    rule.getDescription(),
                    rule.getProductType(),
                    rule.getConditionType(),
                    rule.getConditionJson(),
                    rule.getPriority(),
                    rule.getActive(),
                    LocalDateTime.now(),
                    rule.getId().toString()
            );
        }
        return rule;
    }

    public void deleteById(UUID id) {
        String sql = "DELETE FROM recommendation_rules WHERE id = ?";
        jdbcTemplate.update(sql, id.toString());
    }

    public boolean existsById(UUID id) {
        String sql = "SELECT COUNT(*) FROM recommendation_rules WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id.toString());
        return count != null && count > 0;
    }
}
