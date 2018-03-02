package org.openecomp.mso.cloudify.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class NodeInstances implements Serializable{

	private static final long serialVersionUID = 1L;

	@JsonProperty("items")
	private List<NodeInstance> items;
	
	@JsonProperty("metadata")
	private Metadata metadata;
	
	public List<NodeInstance> getItems() {
		return items;
	}

	public void setItems(List<NodeInstance> items) {
		this.items = items;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

}
