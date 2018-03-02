package org.openecomp.mso.apihandlerinfra.tenantisolationbeans;

public class Distribution {
	
	private Status status;
	private String errorReason;
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public String getErrorReason() {
		return errorReason;
	}
	
	public void setErrorReason(String errorReason) {
		this.errorReason = errorReason;
	}

}
