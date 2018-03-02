package org.openecomp.mso.cloudify.base.client;

import org.openecomp.mso.cloudify.v3.model.CloudifyError;

public class CloudifyResponseException extends CloudifyBaseException {

	private static final long serialVersionUID = 7294957362769575271L;

	protected String message;
	protected int status;
	
	// Make the response available for exception handling (includes body)
	protected CloudifyResponse response;

	public CloudifyResponseException(String message, int status) {
		this.message = message;
		this.status = status;
		this.response = null;
	}

	// Include the response message itself.  The body is a CloudifyError JSON structure.
	public CloudifyResponseException(String message, int status, CloudifyResponse response) {
		CloudifyError error = response.getErrorEntity(CloudifyError.class);
		this.message = message + ": " + error.getErrorCode();
		this.status = status;
		this.response = response;
	}

	public String getMessage() {
		return message;
	}

	public int getStatus() {
		return status;
	}

	public CloudifyResponse getResponse() {
		return response;
	}

}
