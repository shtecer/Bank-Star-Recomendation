package org.courseWork.model;

import java.util.UUID;

public class RulesNotFoundException extends RuntimeException {

    public RulesNotFoundException() {
        super();
    }

    public RulesNotFoundException(String message) {
        super(message);
    }

    public RulesNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RulesNotFoundException(UUID ruleId) {
        super("Rule not found with id: " + ruleId);
    }

    public RulesNotFoundException(UUID ruleId, Throwable cause) {
        super("Rule not found with id: " + ruleId, cause);
    }
}