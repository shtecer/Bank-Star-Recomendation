package org.courseWork.dto;

import java.util.List;
import java.util.UUID;

public class RecommendationRequest {
    private UUID userId;
    private List<UUID> productIds; // Опционально - если нужны рекомендации по конкретным продуктам

    // Геттеры и сеттеры
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public List<UUID> getProductIds() { return productIds; }
    public void setProductIds(List<UUID> productIds) { this.productIds = productIds; }
}
