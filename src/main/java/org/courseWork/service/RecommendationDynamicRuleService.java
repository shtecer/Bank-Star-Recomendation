package org.courseWork.service;

import lombok.extern.slf4j.Slf4j;
import org.courseWork.rules.dto.CreateRuleRequest;
import org.courseWork.model.Product;
import org.courseWork.model.ProductOffer;
import org.courseWork.model.RulesNotFoundException;
import org.courseWork.rules.model.RecommendationRule;
import org.courseWork.rules.repository.RecommendationRuleRepository;
import org.courseWork.rules.service.DynamicRuleService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecommendationDynamicRuleService {

    private final DynamicRuleService dynamicRuleService;
    private final ConditionCheckService conditionCheckService;
    private final RecommendationRuleRepository recommendationRuleRepository;

    // Кэш доступных продуктов
    private final Map<String, Product> availableProducts = Map.of(
            "DEBIT", new Product(UUID.fromString("123e4567-e89b-12d3-a456-426614174003"), "Дебетовая карта", "Базовая дебетовая карта"),
            "SAVING", new Product(UUID.fromString("123e4567-e89b-12d3-a456-426614174004"), "Накопительный счет", "Счет для накоплений"),
            "INVESTMENT", new Product(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), "Инвестиционный портфель", "Портфель ценных бумаг"),
            "CREDIT", new Product(UUID.fromString("123e4567-e89b-12d3-a456-426614174002"), "Кредитная карта", "Кредитная карта с льготным периодом"),
            "PREMIUM_CARD", new Product(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"), "Премиальная карта", "Карта с повышенным кэшбэком"),
            "MORTGAGE", new Product(UUID.fromString("123e4567-e89b-12d3-a456-426614174005"), "Ипотечный кредит", "Выгодная ипотека")
    );

    public RecommendationDynamicRuleService(DynamicRuleService dynamicRuleService,
                                            ConditionCheckService conditionCheckService,
                                            RecommendationRuleRepository recommendationRuleRepository) {
        this.dynamicRuleService = dynamicRuleService;
        this.conditionCheckService = conditionCheckService;
        this.recommendationRuleRepository = recommendationRuleRepository;
    }

    //рекомендации для пользователя
    public List<ProductOffer> getRecommendedProducts(UUID userId) {
        log.info("Getting recommendations for user: {}", userId);

        List<RecommendationRule> activeRules = dynamicRuleService.getAllActiveRules();

        if (activeRules.isEmpty()) {
            log.warn("No active rules found in database");
            return Collections.emptyList();
        }

        log.info("Found {} active rules to evaluate", activeRules.size());

        List<RecommendationRule> eligibleRules = activeRules.stream()
                .filter(rule -> checkRuleCondition(rule, userId))
                .collect(Collectors.toList());

        log.info("User {} is eligible for {} rules", userId, eligibleRules.size());

        return eligibleRules.stream()
                .map(this::convertToProductOffer)
                .collect(Collectors.toList());
    }

    //создать новое правило
    public RecommendationRule createRule(RecommendationRule rule) {
        log.info("Creating new rule: {}", rule.getName());

        // Устанавливаем значения по умолчанию
        if (rule.getActive() == null) {
            rule.setActive(true);
        }
        if (rule.getPriority() == null) {
            rule.setPriority(1);
        }
        if (rule.getCreatedAt() == null) {
            rule.setCreatedAt(LocalDateTime.now());
        }

        RecommendationRule savedRule = dynamicRuleService.createRule(rule);
        log.info("Rule created successfully with ID: {}", savedRule.getId());

        return savedRule;
    }

    //создать правило из DTO
    public RecommendationRule createRule(CreateRuleRequest request) {
        log.info("Creating rule from request: {}", request.getName());

        RecommendationRule rule = new RecommendationRule();
        rule.setName(request.getName());
        rule.setDescription(request.getRuleDescription());
        rule.setProductType(request.getProductType());
        rule.setConditionType(request.getConditionType());
        rule.setConditionJson(request.getConditionJson());
        rule.setPriority(request.getPriority() != null ? request.getPriority() : 1);
        rule.setActive(request.getActive() != null ? request.getActive() : true);
        rule.setCreatedAt(LocalDateTime.now());

        return createRule(rule);
    }

    //удалить правило по ID
    public void deleteRule(UUID ruleId) {
        log.info("Deleting rule with ID: {}", ruleId);

        // Проверяем существование правила
        RecommendationRule rule = recommendationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RulesNotFoundException(ruleId));

        dynamicRuleService.deleteRule(ruleId);
        log.info("Rule deleted successfully: {}", ruleId);
    }

    //обновить правило
    public RecommendationRule updateRule(UUID ruleId, RecommendationRule ruleDetails) {
        log.info("Updating rule with ID: {}", ruleId);

        RecommendationRule updatedRule = dynamicRuleService.updateRule(ruleId, ruleDetails);
        log.info("Rule updated successfully: {}", ruleId);

        return updatedRule;
    }

    //получить все доступные продукты
    public List<Product> getAllAvailableProducts() {
        log.info("Getting all available products");
        return new ArrayList<>(availableProducts.values());
    }

    //получить продукт по типу
    public Product getProductByType(String productType) {
        return availableProducts.get(productType.toUpperCase());
    }

    //получить все правила
    public List<RecommendationRule> getAllRules() {
        log.info("Getting all rules");
        return recommendationRuleRepository.findAll();
    }

    //получить правило по ID
    public RecommendationRule getRuleById(UUID ruleId) {
        log.info("Getting rule by ID: {}", ruleId);
        return recommendationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RulesNotFoundException(ruleId));
    }

    //получить активные правила
    public List<RecommendationRule> getActiveRules() {
        log.info("Getting active rules");
        return dynamicRuleService.getAllActiveRules();
    }

    //активировать/деактивировать правила
    public RecommendationRule toggleRuleStatus(UUID ruleId, boolean active) {
        log.info("Setting rule {} active status to: {}", ruleId, active);

        RecommendationRule rule = getRuleById(ruleId);
        rule.setActive(active);
        rule.setUpdatedAt(LocalDateTime.now());

        return recommendationRuleRepository.save(rule);
    }

    // Вспомогательные методы

    private boolean checkRuleCondition(RecommendationRule rule, UUID userId) {
        try {
            boolean eligible = conditionCheckService.evaluateCondition(
                    rule.getConditionType(),
                    rule.getConditionJson(),
                    userId
            );

            dynamicRuleService.logRuleExecution(
                    rule.getId(),
                    userId,
                    eligible,
                    String.format("Rule: %s, Condition: %s", rule.getName(), rule.getConditionType())
            );

            return eligible;

        } catch (Exception e) {
            log.error("Error evaluating rule '{}' for user {}", rule.getName(), userId, e);

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
        Product product = availableProducts.get(rule.getProductType());

        if (product == null) {
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
}