package org.onap.aaiclient.client.graphinventory;

import java.util.List;

public interface GraphInventoryFluentTypeBase {

    public interface Info {
        String getPartialUri();

        List<String> getPaths();
    }

    Object[] values();

    String uriTemplate();
}
