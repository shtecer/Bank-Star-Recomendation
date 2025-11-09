package org.courseWork.service;

import org.courseWork.dto.ProductOffer;
import org.courseWork.model.Product;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class RecommendationService {

    private final RecommendationRulesChecker recommendationRulesCheckerService;

    //добавляем продукты
    private final Map<String, Product> availableProducts = Map.of(
            "A", new Product(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), "Invest500", "Инвестиционный портфель"),
            "B", new Product(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"), "TopSaving", "Премиальная карта"),
            "C", new Product(UUID.fromString("123e4567-e89b-12d3-a456-426614174002"), "Простой кредит", "Ипотечный кредит")
    );


    public RecommendationService(RecommendationRulesChecker recommendationRulesCheckerService) {
        this.recommendationRulesCheckerService = recommendationRulesCheckerService;
    }

    public List<ProductOffer> getRecommendedProducts(UUID userId) {
        System.out.println("Getting recommendations for user: " + userId);

        List<ProductOffer> recommendations = new ArrayList<>();

        //проверяем каждый продукт
        if (recommendationRulesCheckerService.isProductAEligible(userId)) {
            Product productA = availableProducts.get("A");
            recommendations.add(new ProductOffer(
                    productA.getId(),
                    productA.getProductName(),
                    productA.getDescription()
                   // productA.getPriority()
            ));
            System.out.println("Product A is eligible for user " + userId);
        }

        if (recommendationRulesCheckerService.isProductBEligible(userId)) {
            Product productB = availableProducts.get("B");
            recommendations.add(new ProductOffer(
                    productB.getId(),
                    productB.getProductName(),
                    productB.getDescription()
            ));
            System.out.println("Product B is eligible for user " + userId);
        }

        if (recommendationRulesCheckerService.isProductCEligible(userId)) {
            Product productC = availableProducts.get("C");
            recommendations.add(new ProductOffer(
                    productC.getId(),
                    productC.getProductName(),
                    productC.getDescription()
            ));
            System.out.println("Product C is eligible for user " + userId);
        }

        System.out.println("Found " + recommendations.size() + " recommendations for user " + userId);
        return recommendations;
    }

    //метод для проверки всех условий сразу (для отладки)
    public Map<String, Boolean> checkAllEligibilities(UUID userId) {
        Map<String, Boolean> results = new HashMap<>();
        results.put("PRODUCT_A", recommendationRulesCheckerService.isProductAEligible(userId));
        results.put("PRODUCT_B", recommendationRulesCheckerService.isProductBEligible(userId));
        results.put("PRODUCT_C", recommendationRulesCheckerService.isProductCEligible(userId));
        return results;
    }
    //получение всех продуктов
    public List<Product> getAllAvailableProducts() {
        return new ArrayList<>(availableProducts.values());
    }
}