package org.courseWork.rules.dto;

import lombok.Data;

@Data
public class CreateRuleRequest {
    private String name;
    private String description;
    private String productType;
    private String conditionType;
    private String conditionJson;
    private Integer priority = 1;
    private Boolean active = true;

    public String getRuleDescription() { return description;    }
}