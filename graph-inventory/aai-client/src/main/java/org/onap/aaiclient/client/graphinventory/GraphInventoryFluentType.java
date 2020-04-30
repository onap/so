package org.onap.aaiclient.client.graphinventory;

public interface GraphInventoryFluentType<T> extends GraphInventoryFluentTypeBase {

    public interface Info extends GraphInventoryFluentTypeBase.Info {
        String getName();
    }

    T build();
}
