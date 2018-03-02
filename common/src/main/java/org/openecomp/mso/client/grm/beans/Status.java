package org.openecomp.mso.client.grm.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "status", "statusReasonCode", "statusReasonDescription", "statusCheckTime" })
public class Status {

	@JsonProperty("status")
	private String status;
	@JsonProperty("statusReasonCode")
	private String statusReasonCode;
	@JsonProperty("statusReasonDescription")
	private String statusReasonDescription;
	@JsonProperty("statusCheckTime")
	private String statusCheckTime;

	@JsonProperty("status")
	public String getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(String status) {
		this.status = status;
	}

	@JsonProperty("statusReasonCode")
	public String getStatusReasonCode() {
		return statusReasonCode;
	}

	@JsonProperty("statusReasonCode")
	public void setStatusReasonCode(String statusReasonCode) {
		this.statusReasonCode = statusReasonCode;
	}

	@JsonProperty("statusReasonDescription")
	public String getStatusReasonDescription() {
		return statusReasonDescription;
	}

	@JsonProperty("statusReasonDescription")
	public void setStatusReasonDescription(String statusReasonDescription) {
		this.statusReasonDescription = statusReasonDescription;
	}

	@JsonProperty("statusCheckTime")
	public String getStatusCheckTime() {
		return statusCheckTime;
	}

	@JsonProperty("statusCheckTime")
	public void setStatusCheckTime(String statusCheckTime) {
		this.statusCheckTime = statusCheckTime;
	}

}