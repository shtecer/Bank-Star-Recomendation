package org.courseWork.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CacheService {

    private final Map<String, Object> caches = new ConcurrentHashMap<>();

    public void put(String cacheName, Object key, Object value) {
        String cacheKey = generateCacheKey(cacheName, key);
        caches.put(cacheKey, value);
    }

    public Object get(String cacheName, Object key) {
        String cacheKey = generateCacheKey(cacheName, key);
        return caches.get(cacheKey);
    }

    public void evict(String cacheName, Object key) {
        String cacheKey = generateCacheKey(cacheName, key);
        caches.remove(cacheKey);
    }

    public void clearCache(String cacheName) {
        caches.keySet().removeIf(key -> key.startsWith(cacheName + ":"));
        log.info("Cache cleared: {}", cacheName);
    }

    public void clearAllCaches() {
        caches.clear();
        log.info("All caches cleared");
    }

    public Map<String, Object> getCacheStats() {
        Map<String, Long> cacheSizes = caches.keySet().stream()
                .map(key -> key.split(":")[0])
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return Map.of(
                "totalCaches", cacheSizes.size(),
                "totalEntries", caches.size(),
                "cacheSizes", cacheSizes,
                "timestamp", LocalDateTime.now()
        );
    }

    private String generateCacheKey(String cacheName, Object key) {
        return cacheName + ":" + key.toString();
    }
}