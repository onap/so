package org.onap.so.client.cds.beans;

import java.io.Serializable;

public class AbstractCDSPropertiesBean implements Serializable {

	private static final long serialVersionUID = -4800522372460352963L;

	private String blueprintName;

	private String blueprintVersion;

	private String requestObject;

	private String originatorId;

	private String requestId;

	private String subRequestId;

	private String actionName;

	private String mode;

	public String getBlueprintName() {
		return blueprintName;
	}

	public void setBlueprintName(String blueprintName) {
		this.blueprintName = blueprintName;
	}

	public String getBlueprintVersion() {
		return blueprintVersion;
	}

	public void setBlueprintVersion(String blueprintVersion) {
		this.blueprintVersion = blueprintVersion;
	}

	public String getRequestObject() {
		return requestObject;
	}

	public void setRequestObject(String requestObject) {
		this.requestObject = requestObject;
	}

	public String getOriginatorId() {
		return originatorId;
	}

	public void setOriginatorId(String originatorId) {
		this.originatorId = originatorId;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getSubRequestId() {
		return subRequestId;
	}

	public void setSubRequestId(String subRequestId) {
		this.subRequestId = subRequestId;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

}
