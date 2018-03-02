package org.openecomp.mso.cloudify.v3.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a generic Cloudify error response body.
 * These responses have a common format:
 * {
 *     "message": "<error message>",
 *     "error_code": "<cloudify error id string>".
 *     "server_traceback": "<Python traceback>"
 * }
 * 
 * @author jc1348
 */
public class CloudifyError implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@JsonProperty("message")
	private String message;
	
	@JsonProperty("error_code")
	private String errorCode;
	
	@JsonProperty("server_traceback")
	private String serverTraceback;
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getServerTraceback() {
		return serverTraceback;
	}

	public void setServerTraceback(String serverTraceback) {
		this.serverTraceback = serverTraceback;
	}
}
