package org.onap.aaiclient.client.graphinventory;

public interface GraphInventoryFluentType<T> extends GraphInventoryFluentTypeBase {

    public interface Info extends GraphInventoryFluentTypeBase.Info {
        String getName();

        UriParams getUriParams();

        public interface UriParams {

        }
    }

    T build();
}
