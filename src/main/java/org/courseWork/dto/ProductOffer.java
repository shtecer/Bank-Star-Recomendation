package org.courseWork.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class ProductOffer {
    private UUID productId;
    private String productName;
    private String description;
    private LocalDateTime offeredAt;

    public ProductOffer() {
        this.offeredAt = LocalDateTime.now();
    }

    public ProductOffer(UUID productId, String productName, String description) {
        this();
        this.productId = productId;
        this.productName = productName;
        this.description = description;

    }

    // Геттеры и сеттеры
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getOfferedAt() { return offeredAt; }
    public void setOfferedAt(LocalDateTime offeredAt) { this.offeredAt = offeredAt; }
}