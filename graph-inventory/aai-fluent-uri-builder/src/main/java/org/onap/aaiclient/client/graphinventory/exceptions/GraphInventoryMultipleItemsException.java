package org.onap.aaiclient.client.graphinventory.exceptions;

import org.onap.aaiclient.client.graphinventory.entities.uri.GraphInventoryPluralResourceUri;

public class GraphInventoryMultipleItemsException extends RuntimeException {

    private static final long serialVersionUID = -1596266941681036917L;

    public GraphInventoryMultipleItemsException(int size, GraphInventoryPluralResourceUri uri) {
        super(String.format("Found %s objects at %s when we only expected to find one.", size, uri.build()));
    }

}
