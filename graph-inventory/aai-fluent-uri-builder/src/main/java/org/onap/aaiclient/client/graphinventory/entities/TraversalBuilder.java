package org.onap.aaiclient.client.graphinventory.entities;

public class TraversalBuilder {


    private TraversalBuilder() {

    }

    public static DSLQueryBuilder<Start, Start> fragment(Start node) {
        return new DSLQueryBuilder<>(node);
    }

    public static DSLQueryBuilder<Output, Output> traversal(Output node) {
        return new DSLQueryBuilder<>(node);
    }
}
