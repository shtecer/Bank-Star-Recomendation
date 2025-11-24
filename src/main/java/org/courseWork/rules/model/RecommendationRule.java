package org.courseWork.rules.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RecommendationRule {
    private UUID id;
    private String name;
    private String description;
    private String productType; // DEBIT, SAVING, CREDIT, etc.
    private String conditionType; // HAS_PRODUCT, MIN_AMOUNT, NO_PRODUCT, etc.
    private String conditionJson; // JSON с параметрами условия
    private Integer priority = 1;
    private Boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    // Можно оставить метод для обновления даты (вызывать вручную)
    public void markUpdated() {
        this.updatedAt = LocalDateTime.now();
    }
}