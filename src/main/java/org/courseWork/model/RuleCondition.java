package org.courseWork.model;

import java.util.List;

public class RuleCondition {
    private String type; // "HAS_PRODUCT", "NO_PRODUCT", "MIN_TRANSACTION_COUNT", "MIN_AMOUNT", "AMOUNT_COMPARISON"
    private String productType;
    private String transactionType;
    private Integer minCount;
    private Double minAmount;
    private String comparisonType; // "GREATER_THAN", "LESS_THAN", "EQUALS"
    private Double comparisonAmount;
    private List<String> productTypes;

    // Геттеры и сеттеры
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public Integer getMinCount() { return minCount; }
    public void setMinCount(Integer minCount) { this.minCount = minCount; }
    public Double getMinAmount() { return minAmount; }
    public void setMinAmount(Double minAmount) { this.minAmount = minAmount; }
    public String getComparisonType() { return comparisonType; }
    public void setComparisonType(String comparisonType) { this.comparisonType = comparisonType; }
    public Double getComparisonAmount() { return comparisonAmount; }
    public void setComparisonAmount(Double comparisonAmount) { this.comparisonAmount = comparisonAmount; }
    public List<String> getProductTypes() { return productTypes; }
    public void setProductTypes(List<String> productTypes) { this.productTypes = productTypes; }
}
