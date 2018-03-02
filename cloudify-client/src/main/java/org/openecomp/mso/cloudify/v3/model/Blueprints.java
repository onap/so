package org.openecomp.mso.cloudify.v3.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Blueprints implements Serializable{

	private static final long serialVersionUID = 1L;

	@JsonProperty("items")
	private List<Blueprint> items;
	
	@JsonProperty("metadata")
	private Metadata metadata;
	
	public List<Blueprint> getItems() {
		return items;
	}

	public void setItems(List<Blueprint> items) {
		this.items = items;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

}
