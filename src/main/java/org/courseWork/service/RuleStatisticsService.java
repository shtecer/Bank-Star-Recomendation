package org.courseWork.service;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RuleStatisticsService {

    private final Map<UUID, RuleStatistics> ruleStats = new ConcurrentHashMap<>();
    private final Map<UUID, UserStatistics> userStats = new ConcurrentHashMap<UUID, UserStatistics>();
    private final AtomicLong totalRecommendations = new AtomicLong(0);



    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserStatistics {
        private UUID userId;
        @Builder.Default
        private AtomicLong recommendationCount = new AtomicLong(0);
        @Builder.Default
        private LocalDateTime lastActivity = LocalDateTime.now();
        @Builder.Default
        private Set<UUID> triggeredRules = new ConcurrentHashMap().newKeySet();
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RuleStatistics {
        private UUID ruleId;
        private String ruleName;
        @Builder.Default
        private AtomicLong triggerCount = new AtomicLong(0);
        @Builder.Default
        private AtomicLong totalUsers = new AtomicLong(0);
        @Builder.Default
        private LocalDateTime lastTriggered = LocalDateTime.now();


        // Или методы для получения long значений напрямую
        public long getTriggerCountValue() {
            return triggerCount.get();
        }

        public long getTotalUsersValue() {
            return totalUsers.get();
        }
    }

    public void recordRuleTrigger(UUID ruleId, String ruleName, UUID userId) {
        ruleStats.computeIfAbsent(ruleId, id -> RuleStatistics.builder()
                .ruleId(ruleId)
                .ruleName(ruleName)
                .build()
        ).triggerCount.incrementAndGet();

        if (userId != null) {
            ruleStats.get(ruleId).totalUsers.incrementAndGet();

            userStats.computeIfAbsent(userId, id -> UserStatistics.builder()
                    .userId(userId)
                    .build()
            ).recommendationCount.incrementAndGet();

            userStats.get(userId).getTriggeredRules().add(ruleId);
            userStats.get(userId).setLastActivity(LocalDateTime.now());
        }

        totalRecommendations.incrementAndGet();
    }

    public void recordRecommendationEvent(UUID userId, int recommendationsCount) {
        if (userId != null) {
            userStats.computeIfAbsent(userId, id -> UserStatistics.builder()
                    .userId(userId)
                    .build()
            ).recommendationCount.addAndGet(recommendationsCount);

            userStats.get(userId).setLastActivity(LocalDateTime.now());
        }

        totalRecommendations.addAndGet(recommendationsCount);
    }

    public Map<String, Object> getRuleStatistics(UUID ruleId) {
        RuleStatistics stats = ruleStats.get(ruleId);
        if (stats == null) {
            return Map.of("error", "Statistics not found for rule: " + ruleId);
        }

        return Map.of(
                "ruleId", stats.getRuleId(),
                "ruleName", stats.getRuleName(),
                "triggerCount", stats.getTriggerCount().get(),
                "totalUsers", stats.getTotalUsers().get(),
                "lastTriggered", stats.getLastTriggered(),
                "averagePerUser", stats.getTotalUsers().get() > 0 ?
                        (double) stats.getTriggerCount().get() / stats.getTotalUsers().get() : 0
        );
    }
    public Map<String, Object> getOverallStatistics() {
        List<Map<String, Object>> topRules = ruleStats.values().stream()
                .sorted(Comparator.comparingLong((RuleStatistics stat) -> stat.getTriggerCount().get()).reversed())
                .limit(5)
                .map(stats -> {
                    Map<String, Object> ruleMap = new HashMap<>();
                    ruleMap.put("ruleId", stats.getRuleId());
                    ruleMap.put("ruleName", stats.getRuleName());
                    ruleMap.put("count", stats.getTriggerCount().get());
                    ruleMap.put("users", stats.getTotalUsers().get());
                    return ruleMap;
                })
                .collect(Collectors.toList());

        return Map.of(
                "totalRecommendations", totalRecommendations.get(),
                "uniqueUsers", userStats.size(),
                "activeRules", ruleStats.size(),
                "topRules", topRules,
                "generatedAt", LocalDateTime.now()
        );
    }

    public Map<String, Object> getUserStatistics(Long userId) {
        UserStatistics stats = userStats.get(userId);
        if (stats == null) {
            return Map.of("error", "Statistics not found for user: " + userId);
        }

        return Map.of(
                "userId", stats.getUserId(),
                "recommendationCount", stats.getRecommendationCount().get(),
                "lastActivity", stats.getLastActivity(),
                "triggeredRulesCount", stats.getTriggeredRules().size()
        );
    }

    public void clearStatistics() {
        ruleStats.clear();
        userStats.clear();
        totalRecommendations.set(0);
        log.info("Statistics cleared");
    }

}
