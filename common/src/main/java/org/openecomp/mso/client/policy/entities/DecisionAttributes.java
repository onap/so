
package org.openecomp.mso.client.policy.entities;



import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ServiceType", "VNFType", "BB_ID", "WorkStep", "ErrorCode" })
public class DecisionAttributes {

	@JsonProperty("ServiceType")
	private String serviceType;
	@JsonProperty("VNFType")
	private String vNFType;
	@JsonProperty("BB_ID")
	private String bbID;
	@JsonProperty("WorkStep")
	private String workStep;
	@JsonProperty("ErrorCode")
	private String errorCode;

	@JsonProperty("ServiceType")
	public String getServiceType() {
		return serviceType;
	}

	@JsonProperty("ServiceType")
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	@JsonProperty("VNFType")
	public String getVNFType() {
		return vNFType;
	}

	@JsonProperty("VNFType")
	public void setVNFType(String vNFType) {
		this.vNFType = vNFType;
	}

	@JsonProperty("BB_ID")
	public String getBBID() {
		return bbID;
	}

	@JsonProperty("BB_ID")
	public void setBBID(String bBID) {
		this.bbID = bBID;
	}

	@JsonProperty("WorkStep")
	public String getWorkStep() {
		return workStep;
	}

	@JsonProperty("WorkStep")
	public void setWorkStep(String workStep) {
		this.workStep = workStep;
	}

	@JsonProperty("ErrorCode")
	public String getErrorCode() {
		return errorCode;
	}

	@JsonProperty("ErrorCode")
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
}
