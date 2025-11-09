package org.courseWork.model;

import java.util.UUID;

public class Product {
    private UUID id;
    private String productName;
    private String description;

    public Product() {}

    
    public Product(UUID id, String productName, String description) {
        this.id = id;
        this.productName = productName;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
