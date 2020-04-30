package org.onap.aaiclient.client.graphinventory.entities;

import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectName;

public class DSLStartNode extends DSLNodeBase<DSLStartNode> implements Start {


    public DSLStartNode() {
        super();
    }

    public DSLStartNode(GraphInventoryObjectName name) {
        super(name);
    }

    public DSLStartNode(GraphInventoryObjectName name, DSLNodeKey... key) {
        super(name, key);
    }
}
