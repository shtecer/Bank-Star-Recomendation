package org.courseWork.dto;

import org.courseWork.model.RuleCondition;

import java.util.List;
import java.util.UUID;

public class CreateRuleRequest {
    private UUID productId;
    private String ruleName;
    private String ruleDescription;
    private List<RuleCondition> conditions;
    private Boolean active;

    // Геттеры и сеттеры
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getRuleDescription() { return ruleDescription; }
    public void setRuleDescription(String ruleDescription) { this.ruleDescription = ruleDescription; }
    public List<RuleCondition> getConditions() { return conditions; }
    public void setConditions(List<RuleCondition> conditions) { this.conditions = conditions; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
