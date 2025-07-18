package org.onap.aaiclient.client.aai.entities.uri;

import java.io.Serializable;
import org.onap.aaiclient.client.graphinventory.GraphInventoryPluralFragment;

public class AAIPluralFragment implements Serializable, GraphInventoryPluralFragment<AAIFluentPluralType> {

    private static final long serialVersionUID = 1L;

    private final AAIFluentPluralType type;

    public AAIPluralFragment(AAIFluentPluralType type) {
        this.type = type;
    }

    public AAIFluentPluralType get() {
        return type;
    }

}
