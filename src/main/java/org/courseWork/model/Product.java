package org.courseWork.model;

import java.util.UUID;

public class Product {
    private UUID id;
    private UUID productId;
    private UUID userId;
    private String type;
    private int amount;

    public Product() {}

    
    public Product(UUID id, UUID productId, UUID userId, String type, int amount) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID user_id) {
        this.userId = user_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
