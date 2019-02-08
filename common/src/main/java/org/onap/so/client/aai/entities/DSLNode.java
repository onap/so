package org.onap.so.client.aai.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.onap.so.client.graphinventory.GraphInventoryObjectName;

public class DSLNode implements QueryStep {

	private final String nodeName;
	private final List<DSLNodeKey> nodeKeys;
	private final StringBuilder query = new StringBuilder();
	private boolean output = false;
	
	public DSLNode() {
		this.nodeName = "";
		this.nodeKeys = new ArrayList<>();
		
	}
	public DSLNode(GraphInventoryObjectName name) {
		this.nodeName = name.typeName();
		this.nodeKeys = new ArrayList<>();
		query.append(nodeName);
	}
	public DSLNode(GraphInventoryObjectName name, DSLNodeKey... key) {
		this.nodeName = name.typeName();
		this.nodeKeys = Arrays.asList(key);
		query.append(nodeName);
	}
	
	public DSLNode output() {
		this.output = true;
		
		return this;
	}

	public DSLNode and(DSLNodeKey... key) {
		this.nodeKeys.addAll(Arrays.asList(key));
		
		return this;
	}
	
	@Override
	public String build() {
		if (output) {
			query.append("*");
		}
		for (DSLNodeKey key : nodeKeys) {
			query.append(key.build());
		}
		
		return query.toString();
	}
}
