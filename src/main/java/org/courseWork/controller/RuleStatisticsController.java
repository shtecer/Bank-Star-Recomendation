package org.courseWork.controller;

import org.courseWork.service.RuleStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/rule")
public class RuleStatisticsController {

    private final RuleStatisticsService statisticsService;

    public RuleStatisticsController(RuleStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getRuleStats() {
        return ResponseEntity.ok(statisticsService.getOverallStatistics());
    }

    @GetMapping("/stats/{ruleId}")
    public ResponseEntity<Map<String, Object>> getRuleStats(@PathVariable UUID ruleId) {
        return ResponseEntity.ok(statisticsService.getRuleStatistics(ruleId));
    }

    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable Long userId) {
        return ResponseEntity.ok(statisticsService.getUserStatistics(userId));
    }

    @PostMapping("/stats/clear")
    public ResponseEntity<Map<String, Object>> clearStats() {
        statisticsService.clearStatistics();
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Statistics cleared successfully"
        ));
    }
}
