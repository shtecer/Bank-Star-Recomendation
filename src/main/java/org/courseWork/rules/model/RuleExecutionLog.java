package org.courseWork.rules.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RuleExecutionLog {
    private UUID id;
    private UUID ruleId;
    private UUID userId;
    private Boolean eligible;
    private String executionDetails; // Детали выполнения правила
    private LocalDateTime executedAt = LocalDateTime.now();
}