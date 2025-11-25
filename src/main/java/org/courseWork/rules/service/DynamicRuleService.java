package org.courseWork.rules.service;

import lombok.extern.slf4j.Slf4j;
import org.courseWork.rules.model.RecommendationRule;
import org.courseWork.rules.model.RuleExecutionLog;
import org.courseWork.rules.repository.RecommendationRuleRepository;
import org.courseWork.rules.repository.RuleExecutionLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class DynamicRuleService {

    private final RecommendationRuleRepository ruleRepository;
    private final RuleExecutionLogRepository executionLogRepository;

    public DynamicRuleService(RecommendationRuleRepository ruleRepository,
                              RuleExecutionLogRepository executionLogRepository) {
        this.ruleRepository = ruleRepository;
        this.executionLogRepository = executionLogRepository;
    }

    public List<RecommendationRule> getAllActiveRules() {
        return ruleRepository.findByActiveTrueOrderByPriorityDesc();
    }

    public RecommendationRule createRule(RecommendationRule rule) {
        rule.setCreatedAt(LocalDateTime.now());
        return ruleRepository.save(rule);
    }

    public RecommendationRule updateRule(UUID ruleId, RecommendationRule ruleDetails) {
        RecommendationRule existingRule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found: " + ruleId));

        // Обновляем только изменяемые поля
        existingRule.setName(ruleDetails.getName());
        existingRule.setDescription(ruleDetails.getDescription());
        existingRule.setProductType(ruleDetails.getProductType());
        existingRule.setConditionType(ruleDetails.getConditionType());
        existingRule.setConditionJson(ruleDetails.getConditionJson());
        existingRule.setPriority(ruleDetails.getPriority());
        existingRule.setActive(ruleDetails.getActive());
        existingRule.setUpdatedAt(LocalDateTime.now());

        return ruleRepository.save(existingRule);
    }

    public void deleteRule(UUID ruleId) {
        if (!ruleRepository.existsById(ruleId)) {
            throw new RuntimeException("Rule not found: " + ruleId);
        }
        ruleRepository.deleteById(ruleId);
    }

    public void logRuleExecution(UUID ruleId, UUID userId, Boolean eligible, String details) {
        RuleExecutionLog logEntry = new RuleExecutionLog();
        logEntry.setRuleId(ruleId);
        logEntry.setUserId(userId);
        logEntry.setEligible(eligible);
        logEntry.setExecutionDetails(details);
        logEntry.setExecutedAt(LocalDateTime.now());

        executionLogRepository.save(logEntry);
    }

    public RuleStatistics getRuleStatistics(UUID ruleId) {
        Long totalExecutions = executionLogRepository.countByRuleIdAndEligibleTrue(ruleId);
        Long uniqueUsers = executionLogRepository.countUniqueUsersByRuleId(ruleId);

        return new RuleStatistics(ruleId, totalExecutions, uniqueUsers);
    }

    public static class RuleStatistics {
        private final UUID ruleId;
        private final Long totalExecutions;
        private final Long uniqueUsers;

        public RuleStatistics(UUID ruleId, Long totalExecutions, Long uniqueUsers) {
            this.ruleId = ruleId;
            this.totalExecutions = totalExecutions;
            this.uniqueUsers = uniqueUsers;
        }

        // Геттеры
        public UUID getRuleId() { return ruleId; }
        public Long getTotalExecutions() { return totalExecutions; }
        public Long getUniqueUsers() { return uniqueUsers; }
    }
}