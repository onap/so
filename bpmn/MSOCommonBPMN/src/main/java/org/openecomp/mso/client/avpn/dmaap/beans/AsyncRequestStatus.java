package org.openecomp.mso.client.avpn.dmaap.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "asyncRequestStatus")
public class AsyncRequestStatus {
	
	@JsonProperty("requestId")
	private String requestId;
	@JsonProperty("clientSource")
	private String clientSource;
	@JsonProperty("correlator")
	private String correlator;
	@JsonProperty("instanceReferences")
	private InstanceReferences instanceReferences;
	@JsonProperty("startTime")
	private String startTime;
	@JsonProperty("finishTime")
	private String finishTime;
	@JsonProperty("requestScope")
	private String requestScope;
	@JsonProperty("requestType")
	private String requestType;
	@JsonProperty("requestStatus")
	private RequestStatus requestStatus;
	
	public String getRequestId() {
		return requestId;
	}
	
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	
	public String getClientSource() {
		return clientSource;
	}
	
	public void setClientSource(String clientSource) {
		this.clientSource = clientSource;
	}
	
	public String getCorrelator() {
		return correlator;
	}
	
	public void setCorrelator(String correlator) {
		this.correlator = correlator;
	}
	
	public InstanceReferences getInstanceReferences() {
		return instanceReferences;
	}
	
	public void setInstanceReferences(InstanceReferences instanceReferences) {
		this.instanceReferences = instanceReferences;
	}
	
	public String getStartTime() {
		return startTime;
	}
	
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	
	public String getFinishTime() {
		return finishTime;
	}
	
	public void setFinishTime(String finishTime) {
		this.finishTime = finishTime;
	}
	
	public String getRequestScope() {
		return requestScope;
	}
	
	public void setRequestScope(String requestScope) {
		this.requestScope = requestScope;
	}
	
	public String getRequestType() {
		return requestType;
	}
	
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	
	public RequestStatus getRequestStatus() {
		return requestStatus;
	}
	
	public void setRequestStatus(RequestStatus requestStatus) {
		this.requestStatus = requestStatus;
	}
}
