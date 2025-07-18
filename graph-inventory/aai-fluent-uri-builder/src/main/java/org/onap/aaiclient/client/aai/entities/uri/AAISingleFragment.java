package org.onap.aaiclient.client.aai.entities.uri;

import java.io.Serializable;
import org.onap.aaiclient.client.graphinventory.GraphInventorySingleFragment;

public class AAISingleFragment implements Serializable, GraphInventorySingleFragment<AAIFluentSingleType> {

    private static final long serialVersionUID = 1L;

    private final AAIFluentSingleType type;

    public AAISingleFragment(AAIFluentSingleType type) {
        this.type = type;
    }

    public AAIFluentSingleType get() {
        return type;
    }
}
