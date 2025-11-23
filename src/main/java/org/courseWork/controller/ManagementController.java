package org.courseWork.controller;

import org.courseWork.service.CacheService;
import org.courseWork.service.RuleStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/management")
public class ManagementController {

    private final CacheService cacheService;
    private final RuleStatisticsService statisticsService;

    public ManagementController(CacheService cacheService, RuleStatisticsService statisticsService) {
        this.cacheService = cacheService;
        this.statisticsService = statisticsService;
    }

    @PostMapping("/clear-caches")
    public ResponseEntity<Map<String, Object>> clearCaches() {
        cacheService.clearAllCaches();
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "All caches cleared successfully",
                "timestamp", LocalDateTime.now()
        ));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = Map.of(
                "name", "Product Recommendation System",
                "version", "1.0.0",
                "description", "Dynamic product recommendation system with Telegram integration",
                "uptime", ManagementFactory.getRuntimeMXBean().getUptime() + " ms",
                "timestamp", LocalDateTime.now()
        );
        return ResponseEntity.ok(info);
    }

    @GetMapping("/cache-stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        return ResponseEntity.ok(cacheService.getCacheStats());
    }
}
