package org.onap.so.client.adapter.cnf.entities;

public enum HealthcheckResult {
    UNKNOWN("Unknown"), RUNNING("Running"), SUCCEEDED("Succeeded"), FAILED("Failed");

    private final String type;

    private HealthcheckResult(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }

    public static HealthcheckResult fromString(String text) {
        for (HealthcheckResult x : HealthcheckResult.values()) {
            if (x.type.equalsIgnoreCase(text)) {
                return x;
            }
        }
        return null;
    }

}
