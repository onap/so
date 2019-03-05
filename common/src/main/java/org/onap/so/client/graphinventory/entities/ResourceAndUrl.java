package org.onap.so.client.graphinventory.entities;

import org.onap.so.client.graphinventory.GraphInventoryObjectType;

public class ResourceAndUrl<Wrapper extends GraphInventoryResultWrapper> {

	private String url;
	private GraphInventoryObjectType type;
	private Wrapper wrapper;
	
	public ResourceAndUrl(String url, GraphInventoryObjectType type, Wrapper wrapper) {
		this.url = url;
		this.type = type;
		this.wrapper = wrapper;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Wrapper getWrapper() {
		return wrapper;
	}
	public void setWrapper(Wrapper wrapper) {
		this.wrapper = wrapper;
	}
	public GraphInventoryObjectType getType() {
		return type;
	}
	
	public void setType(GraphInventoryObjectType type) {
		this.type = type;
	}
	
}
