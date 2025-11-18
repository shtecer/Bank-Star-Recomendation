package org.courseWork.model;

import java.util.UUID;

public class ProductRule {
    private UUID id;
    private UUID productId;
    private String ruleName;
    private String ruleDescription;
    private String conditionType; // "PRODUCT_USAGE", "TRANSACTION_COUNT", "AMOUNT_COMPARISON"
    private String conditionJson; // JSON с параметрами условия
    private Boolean active;

    public ProductRule() {}

    // Геттеры и сеттеры
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getRuleDescription() { return ruleDescription; }
    public void setRuleDescription(String ruleDescription) { this.ruleDescription = ruleDescription; }
    public String getConditionType() { return conditionType; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }
    public String getConditionJson() { return conditionJson; }
    public void setConditionJson(String conditionJson) { this.conditionJson = conditionJson; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
