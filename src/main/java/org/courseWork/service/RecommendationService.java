package org.courseWork.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.courseWork.model.*;
import org.courseWork.repository.ProductRepository;
import org.courseWork.repository.ProductRuleRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class RecommendationService {

    private final ProductRepository productRepository;
    private final ProductRuleRepository ruleRepository;
    private final RecommendationRulesChecker recommendationRulesChecker;
    private final ObjectMapper objectMapper;

    public RecommendationService(ProductRepository productRepository,
                                 ProductRuleRepository ruleRepository,
                                 RecommendationRulesChecker recommendationRulesChecker, RecommendationRulesChecker recommendationRulesChecker1,
                                 ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.ruleRepository = ruleRepository;
        this.recommendationRulesChecker = recommendationRulesChecker1;
        this.objectMapper = objectMapper;
    }

    public List<ProductOffer> getRecommendedProducts(UUID userId) {
        System.out.println("Getting recommendations for user: " + userId);

        // Получаем все активные правила
        List<ProductRule> allRules = ruleRepository.findAllActive();

        // Поскольку мы принимаем только userId, не фильтруем по productIds
        // Если нужна фильтрация по продуктам, нужно изменить сигнатуру метода

        // Проверяем условия для каждого правила
        List<ProductRule> eligibleRules = allRules.stream()
                .filter(rule -> checkRuleConditions(rule, userId))
                .collect(Collectors.toList());

        // Получаем информацию о продуктах
        List<UUID> eligibleProductIds = eligibleRules.stream()
                .map(ProductRule::getProductId)
                .distinct()
                .collect(Collectors.toList());

        List<Product> eligibleProducts = productRepository.findByIds(eligibleProductIds);
        Map<UUID, Product> productMap = eligibleProducts.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // Создаем предложения
        List<ProductOffer> offers = eligibleRules.stream()
                .filter(rule -> productMap.containsKey(rule.getProductId()))
                .map(rule -> {
                    Product product = productMap.get(rule.getProductId());
                    return new ProductOffer(product.getId(), product.getProductName(), product.getDescription());
                })
                .collect(Collectors.toList());

        System.out.println("Found " + offers.size() + " recommendations for user " + userId);
        return offers;
    }

    private boolean checkRuleConditions(ProductRule rule, UUID userId) {
        try {
            // Парсим JSON условия
            RuleCondition condition = objectMapper.readValue(rule.getConditionJson(), RuleCondition.class);
            return checkSingleCondition(condition, userId);

        } catch (Exception e) {
            System.err.println("Error parsing rule condition for rule " + rule.getRuleName() + ": " + e.getMessage());
            return false;
        }
    }

    private boolean checkSingleCondition(RuleCondition condition, UUID userId) {
        if (condition == null) {
            return false;
        }

        switch (condition.getType().toUpperCase()) {
            case "HAS_PRODUCT":
                return recommendationRulesChecker.hasProductType(userId, condition.getProductType());

            case "NO_PRODUCT":
                return recommendationRulesChecker.hasNoProductType(userId, condition.getProductType());

            case "MIN_TRANSACTION_COUNT":
                return recommendationRulesChecker.hasMinTransactionCount(
                        userId, condition.getProductType(), condition.getTransactionType(), condition.getMinCount());

            case "MIN_AMOUNT":
                return recommendationRulesChecker.hasMinAmount(
                        userId, condition.getProductType(), condition.getTransactionType(), condition.getMinAmount());

            case "AMOUNT_COMPARISON":
                return recommendationRulesChecker.compareAmounts(
                        userId, condition.getProductType(), condition.getComparisonType(), condition.getComparisonAmount());

            default:
                System.err.println("Unknown condition type: " + condition.getType());
                return false;
        }
    }

    public ProductRule createRule(ProductRule request) {
        try {
            ProductRule rule = new ProductRule();
            rule.setProductId(request.getProductId());
            rule.setRuleName(request.getRuleName());
            rule.setRuleDescription(request.getRuleDescription());
            rule.setActive(request.getActive() != null ? request.getActive() : true);
            rule.setConditionType("CUSTOM");

            // Просто копируем conditionJson как есть
            rule.setConditionJson(request.getConditionJson());

            return ruleRepository.save(rule);

        } catch (Exception e) {
            throw new RuntimeException("Error creating rule: " + e.getMessage(), e);
        }
    }

    public List<Product> getAllAvailableProducts() {
        return productRepository.findAllActive();
    }

    public List<ProductRule> getProductRules(UUID productId) {
        return ruleRepository.findByProductId(productId);
    }

    public void deleteRule(UUID ruleId) {
        // Проверяем существование правила перед удалением
        Optional<ProductRule> existingRule = ruleRepository.findById(ruleId);
        if (existingRule.isEmpty()) {
            throw new RulesNotFoundException(ruleId);
        }

        // Выполняем удаление
        ruleRepository.deleteById(ruleId);
    }

    // Альтернативный вариант с мягким удалением
    public void softDeleteRule(UUID ruleId) {
        Optional<ProductRule> existingRule = ruleRepository.findById(ruleId);
        if (existingRule.isEmpty()) {
            throw new RulesNotFoundException(ruleId);
        }

        ruleRepository.softDeleteById(ruleId);
    }

    // Метод для получения правила по ID
    public ProductRule getRuleById(UUID ruleId) {
        return ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RulesNotFoundException(ruleId));
    }
}