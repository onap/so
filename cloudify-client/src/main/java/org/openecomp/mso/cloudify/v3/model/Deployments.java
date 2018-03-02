package org.openecomp.mso.cloudify.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class Deployments implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@JsonProperty("items")
	private List<Deployment> items;
	
	@JsonProperty("metadata")
	private Metadata metadata;
	
	public List<Deployment> getItems() {
		return items;
	}

	public void setItems(List<Deployment> items) {
		this.items = items;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

}
