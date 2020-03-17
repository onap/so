package org.onap.so.client.graphinventory.entities;

import org.onap.so.client.graphinventory.GraphInventoryObjectName;

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
