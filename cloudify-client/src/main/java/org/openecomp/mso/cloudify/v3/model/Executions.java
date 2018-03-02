package org.openecomp.mso.cloudify.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class Executions implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@JsonProperty("items")
	private List<Execution> items;
	
	@JsonProperty("metadata")
	private Metadata metadata;
	
	public List<Execution> getItems() {
		return items;
	}

	public void setItems(List<Execution> items) {
		this.items = items;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

}
