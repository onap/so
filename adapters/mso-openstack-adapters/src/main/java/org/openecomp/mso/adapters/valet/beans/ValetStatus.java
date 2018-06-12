package org.openecomp.mso.adapters.valet.beans;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

/* 
 * This class represents the status object as defined in the Valet Placement Operations API - part of Response objects
 */
public class ValetStatus implements Serializable {
	private static final long serialVersionUID = 1L;
	@JsonProperty("status")
	private String status;
	@JsonProperty("message")
	private String message;
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("status", status).append("message", message).toString();
	}

	public ValetStatus() {
		super();
	}
	
	public ValetStatus(String statusCode, String statusMessage) {
		super();
		this.status = statusCode;
		this.message = statusMessage;
	}
	
	public String getStatus() {
		return this.status;
	}
	public void setStatus(String statusCode) {
		this.status = statusCode;
	}
	public String getMessage() {
		return this.message;
	}
	public void setMessage(String statusMessage) {
		this.message = statusMessage;
	}

	@Override
	public int hashCode() {
		return Objects.hash(status, message);
	}
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof ValetStatus)) {
			return false;
		}
		ValetStatus vs = (ValetStatus) o;
		return Objects.equals(status, vs.status) && Objects.equals(message, vs.message);
	}
}
