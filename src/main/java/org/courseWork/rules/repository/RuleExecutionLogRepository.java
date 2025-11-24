package org.courseWork.rules.repository;

import org.courseWork.rules.model.RuleExecutionLog;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RuleExecutionLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public RuleExecutionLogRepository(@Qualifier("rulesJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<RuleExecutionLog> logRowMapper = (rs, rowNum) -> {
        RuleExecutionLog log = new RuleExecutionLog();
        log.setId(UUID.fromString(rs.getString("id")));
        log.setRuleId(UUID.fromString(rs.getString("rule_id")));
        log.setUserId(UUID.fromString(rs.getString("user_id")));
        log.setEligible(rs.getBoolean("eligible"));
        log.setExecutionDetails(rs.getString("execution_details"));

        if (rs.getTimestamp("executed_at") != null) {
            log.setExecutedAt(rs.getTimestamp("executed_at").toLocalDateTime());
        }

        return log;
    };

    public List<RuleExecutionLog> findAll() {
        String sql = "SELECT * FROM rule_execution_log";
        return jdbcTemplate.query(sql, logRowMapper);
    }

    public Optional<RuleExecutionLog> findById(UUID id) {
        String sql = "SELECT * FROM rule_execution_log WHERE id = ?";
        try {
            RuleExecutionLog log = jdbcTemplate.queryForObject(sql, logRowMapper, id.toString());
            return Optional.ofNullable(log);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<RuleExecutionLog> findByRuleIdOrderByExecutedAtDesc(UUID ruleId) {
        String sql = "SELECT * FROM rule_execution_log WHERE rule_id = ? ORDER BY executed_at DESC";
        return jdbcTemplate.query(sql, logRowMapper, ruleId.toString());
    }

    public List<RuleExecutionLog> findByUserIdOrderByExecutedAtDesc(UUID userId) {
        String sql = "SELECT * FROM rule_execution_log WHERE user_id = ? ORDER BY executed_at DESC";
        return jdbcTemplate.query(sql, logRowMapper, userId.toString());
    }

    public Long countByRuleIdAndEligibleTrue(UUID ruleId) {
        String sql = "SELECT COUNT(*) FROM rule_execution_log WHERE rule_id = ? AND eligible = true";
        return jdbcTemplate.queryForObject(sql, Long.class, ruleId.toString());
    }

    public Long countUniqueUsersByRuleId(UUID ruleId) {
        String sql = "SELECT COUNT(DISTINCT user_id) FROM rule_execution_log WHERE rule_id = ? AND eligible = true";
        return jdbcTemplate.queryForObject(sql, Long.class, ruleId.toString());
    }

    public RuleExecutionLog save(RuleExecutionLog log) {
        if (log.getId() == null) {
            log.setId(UUID.randomUUID());
        }
        if (log.getExecutedAt() == null) {
            log.setExecutedAt(LocalDateTime.now());
        }

        String sql = "INSERT INTO rule_execution_log (id, rule_id, user_id, eligible, execution_details, executed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                log.getId().toString(),
                log.getRuleId().toString(),
                log.getUserId().toString(),
                log.getEligible(),
                log.getExecutionDetails(),
                log.getExecutedAt()
        );

        return log;
    }

    public void deleteById(UUID id) {
        String sql = "DELETE FROM rule_execution_log WHERE id = ?";
        jdbcTemplate.update(sql, id.toString());
    }

    // Дополнительные методы для статистики
    public Long getTotalExecutions() {
        String sql = "SELECT COUNT(*) FROM rule_execution_log";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Long getTotalEligibleExecutions() {
        String sql = "SELECT COUNT(*) FROM rule_execution_log WHERE eligible = true";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}