package org.onap.so.adapters.appc.orchestrator.client;

public enum StatusCategory {
    NORMAL("normal"), WARNING("warning"), ERROR("error");

    private final String category;

    private StatusCategory(final String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return category;
    }
}
