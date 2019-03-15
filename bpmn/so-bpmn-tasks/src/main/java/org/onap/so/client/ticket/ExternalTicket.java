package org.onap.so.client.ticket;

public class ExternalTicket {
	private String requestId;
	private String nfRole;
	private String currentActivity;
	private String description;
	private String subscriptionServiceType;
	private String requestorId;
	private String timeout;
	private String errorSource;
	private String errorCode;
	private String errorMessage;
	private String workStep;	

	public String getRequestId() {
		return requestId;
	}



	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}



	public String getNfRole() {
		return nfRole;
	}



	public void setNfRole(String nfRole) {
		this.nfRole = nfRole;
	}



	public String getCurrentActivity() {
		return currentActivity;
	}



	public void setCurrentActivity(String currentActivity) {
		this.currentActivity = currentActivity;
	}



	public String getDescription() {
		return description;
	}



	public void setDescription(String description) {
		this.description = description;
	}



	public String getSubscriptionServiceType() {
		return subscriptionServiceType;
	}



	public void setSubscriptionServiceType(String subscriptionServiceType) {
		this.subscriptionServiceType = subscriptionServiceType;
	}



	public String getRequestorId() {
		return requestorId;
	}



	public void setRequestorId(String requestorId) {
		this.requestorId = requestorId;
	}



	public String getTimeout() {
		return timeout;
	}



	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}



	public String getErrorSource() {
		return errorSource;
	}



	public void setErrorSource(String errorSource) {
		this.errorSource = errorSource;
	}



	public String getErrorCode() {
		return errorCode;
	}



	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}



	public String getErrorMessage() {
		return errorMessage;
	}



	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}



	public String getWorkStep() {
		return workStep;
	}



	public void setWorkStep(String workStep) {
		this.workStep = workStep;
	}



	public void createTicket() throws Exception {
		//Replace with your ticket creation mechanism if any
	}
	
	
	

}
