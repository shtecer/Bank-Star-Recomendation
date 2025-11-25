package org.courseWork.service;

import lombok.extern.slf4j.Slf4j;
import org.courseWork.model.Product;
import org.courseWork.model.ProductOffer;
import org.courseWork.rules.model.RecommendationRule;
import org.courseWork.rules.service.DynamicRuleService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecommendationService {

    private final DynamicRuleService dynamicRuleService;
    private final ConditionCheckService conditionCheckService;
    private final JdbcTemplate jdbcTemplate;

    // Кэш продуктов (можно вынести в БД)
    private final Map<String, Product> availableProducts = Map.of(
            "DEBIT", new Product(UUID.fromString("123e4567-e89b-12d3-a456-426614174003"), "Дебетовая карта", "Базовая дебетовая карта"),
            "SAVING", new Product(UUID.fromString("123e4567-e89b-12d3-a456-426614174004"), "Накопительный счет", "Счет для накоплений"),
            "INVESTMENT", new Product(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), "Инвестиционный портфель", "Портфель ценных бумаг"),
            "CREDIT", new Product(UUID.fromString("123e4567-e89b-12d3-a456-426614174002"), "Кредитная карта", "Кредитная карта с льготным периодом")
    );

    public RecommendationService(DynamicRuleService dynamicRuleService,
                                 ConditionCheckService conditionCheckService,
                                 @Qualifier("primaryJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.dynamicRuleService = dynamicRuleService;
        this.conditionCheckService = conditionCheckService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ProductOffer> getRecommendedProducts(UUID userId) {
        log.info("Getting recommendations for user: {}", userId);

        // Получаем активные правила из БД правил
        List<RecommendationRule> activeRules = dynamicRuleService.getAllActiveRules();

        if (activeRules.isEmpty()) {
            log.warn("No active rules found in database");
            return Collections.emptyList();
        }

        log.info("Found {} active rules to evaluate", activeRules.size());

        // Проверяем условия для каждого правила
        List<RecommendationRule> eligibleRules = activeRules.stream()
                .filter(rule -> {
                    boolean eligible = checkRuleCondition(rule, userId);
                    log.debug("Rule '{}' for user {}: {}", rule.getName(), userId, eligible);
                    return eligible;
                })
                .collect(Collectors.toList());

        log.info("User {} is eligible for {} rules", userId, eligibleRules.size());

        // Конвертируем подходящие правила в предложения продуктов
        return eligibleRules.stream()
                .map(this::convertToProductOffer)
                .collect(Collectors.toList());
    }

    private boolean checkRuleCondition(RecommendationRule rule, UUID userId) {
        try {
            boolean eligible = conditionCheckService.evaluateCondition(
                    rule.getConditionType(),
                    rule.getConditionJson(),
                    userId
            );

            // Логируем выполнение правила в БД правил
            dynamicRuleService.logRuleExecution(
                    rule.getId(),
                    userId,
                    eligible,
                    String.format("Rule: %s, Condition: %s", rule.getName(), rule.getConditionType())
            );

            return eligible;

        } catch (Exception e) {
            log.error("Error evaluating rule '{}' for user {}", rule.getName(), userId, e);

            // Логируем ошибку выполнения
            dynamicRuleService.logRuleExecution(
                    rule.getId(),
                    userId,
                    false,
                    String.format("ERROR: %s", e.getMessage())
            );

            return false;
        }
    }

    private ProductOffer convertToProductOffer(RecommendationRule rule) {
        // Сопоставляем тип продукта из правила с реальным продуктом
        Product product = availableProducts.get(rule.getProductType());

        if (product == null) {
            // Если продукт не найден, создаем generic предложение
            log.warn("Product not found for type: {}, using generic product", rule.getProductType());
            return new ProductOffer(
                    UUID.randomUUID(),
                    "Специальное предложение: " + rule.getName(),
                    rule.getDescription()
            );
        }

        return new ProductOffer(
                product.getId(),
                product.getProductName(),
                product.getDescription()
        );
    }

    //информация для получения деталей
    public Map<String, Object> getRecommendationDetails(UUID userId) {
        List<RecommendationRule> allRules = dynamicRuleService.getAllActiveRules();
        List<ProductOffer> recommendations = getRecommendedProducts(userId);

        Map<String, Object> details = new HashMap<>();
        details.put("userId", userId);
        details.put("totalRules", allRules.size());
        details.put("eligibleRules", recommendations.size());
        details.put("recommendations", recommendations);
        details.put("checkedAt", new Date());

        // Добавляем статистику транзакций
        var stats = conditionCheckService.getUserTransactionStats(userId, null);
        details.put("userStats", stats);

        return details;
    }
}
