package org.onap.so.client.aai.entities;

import org.onap.so.client.graphinventory.entities.GraphInventoryEdgeLabel;

public enum AAIEdgeLabel implements GraphInventoryEdgeLabel {

	BELONGS_TO("org.onap.relationships.inventory.BelongsTo"),
	USES("org.onap.relationships.inventory.Uses");
	
	
	private final String label;
	private AAIEdgeLabel(String label) {
		this.label = label;
	}
	
	
	@Override
	public String toString() {
		return this.label;
	}
}
