package org.courseWork.rules.model;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class RuleCondition {
    private UUID id;
    private UUID ruleId; // Вместо связи @ManyToOne
    private String type; // HAS_PRODUCT, MIN_AMOUNT, NO_PRODUCT, etc.
    private String productType;
    private String transactionType;
    private Double minAmount;
    private Double maxAmount;
    private Integer minCount;
    private String comparisonType; // GREATER_THAN, LESS_THAN, EQUALS
    private Double comparisonAmount;
    private List<String> productTypes;
}