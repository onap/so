package org.onap.aaiclient.client.graphinventory;

import java.util.Collections;
import java.util.List;

public interface GraphInventoryFluentTypeBase {

    public interface Info {
        default String getPartialUri() {
            return "";
        }

        default List<String> getPaths() {
            return Collections.emptyList();
        }
    }

    default Object[] values() {
        return new Object[] {};
    }

    default String uriTemplate() {
        return "";
    }
}
