package org.openecomp.mso.client.avpn.dmaap.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "requestStatus")
public class RequestStatus {

	@JsonProperty("timestamp")
	private String timestamp;
	@JsonProperty("requestState")
	private String requestState;
	@JsonProperty("statusMessage")
	private String statusMessage;
	@JsonProperty("percentProgress")
	private String percentProgress;
	@JsonProperty("wasRolledBack")
	private Boolean wasRolledBack;
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getRequestState() {
		return requestState;
	}
	
	public void setRequestState(String requestState) {
		this.requestState = requestState;
	}
	
	public String getStatusMessage() {
		return statusMessage;
	}
	
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	
	public String getPercentProgress() {
		return percentProgress;
	}
	
	public void setPercentProgress(String percentProgress) {
		this.percentProgress = percentProgress;
	}

	public Boolean isWasRolledBack() {
		return wasRolledBack;
	}

	public void setWasRolledBack(Boolean wasRolledBack) {
		this.wasRolledBack = wasRolledBack;
	}
}
