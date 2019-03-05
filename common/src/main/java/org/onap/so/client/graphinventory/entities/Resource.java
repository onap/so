package org.onap.so.client.graphinventory.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"resource-type",
	"resource-link"
})
public class Resource {

	@JsonProperty("resource-type")
	private String resourceType;
	@JsonProperty("resource-link")
	private String resourceLink;

	@JsonProperty("resource-type")
	public String getResourceType() {
		return resourceType;
	}

	@JsonProperty("resource-type")
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	@JsonProperty("resource-link")
	public String getResourceLink() {
		return resourceLink;
	}

	@JsonProperty("resource-link")
	public void setResourceLink(String resourceLink) {
		this.resourceLink = resourceLink;
	}

}

