package org.onap.aaiclient.client.graphinventory.entities;

public class DSLTraversal<T> {

    private final String traversal;

    protected DSLTraversal(String traversal) {
        this.traversal = traversal;
    }

    public String get() {
        return traversal;
    }

    @Override
    public String toString() {
        return traversal;
    }

    @Override
    public int hashCode() {
        return traversal.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o != null) {
            return this.toString().equals(o);
        }
        return false;
    }

}
