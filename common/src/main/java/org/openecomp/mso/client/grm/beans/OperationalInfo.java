package org.openecomp.mso.client.grm.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "createdBy", "updatedBy", "createdTimestamp", "updatedTimestamp" })
public class OperationalInfo {

	@JsonProperty("createdBy")
	private String createdBy;
	@JsonProperty("updatedBy")
	private String updatedBy;
	@JsonProperty("createdTimestamp")
	private String createdTimestamp;
	@JsonProperty("updatedTimestamp")
	private String updatedTimestamp;

	@JsonProperty("createdBy")
	public String getCreatedBy() {
		return createdBy;
	}

	@JsonProperty("createdBy")
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@JsonProperty("updatedBy")
	public String getUpdatedBy() {
		return updatedBy;
	}

	@JsonProperty("updatedBy")
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	@JsonProperty("createdTimestamp")
	public String getCreatedTimestamp() {
		return createdTimestamp;
	}

	@JsonProperty("createdTimestamp")
	public void setCreatedTimestamp(String createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	@JsonProperty("updatedTimestamp")
	public String getUpdatedTimestamp() {
		return updatedTimestamp;
	}

	@JsonProperty("updatedTimestamp")
	public void setUpdatedTimestamp(String updatedTimestamp) {
		this.updatedTimestamp = updatedTimestamp;
	}

}