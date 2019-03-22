package org.onap.so.client.cds.beans;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"service-instance-id",
"pnf-id",
"pnf-name",
"service-model-uuid",
"pnf-customization-uuid"
})

public class ConfigAssignPropertiesForPnf {

	@JsonProperty("service-instance-id")
	private String serviceInstanceId;

	@JsonProperty("pnf-id")
	private String pnfId;

	@JsonProperty("pnf-name")
	private String pnfName;

	@JsonProperty("service-model-uuid")
	private String serviceModelUuid;

	@JsonProperty("pnf-customization-uuid")
	private String pnfCustomizationUuid;

	@JsonIgnore
	private Map<String, Object> userParam = new HashMap<String, Object>();

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}

	public String getPnfId() {
		return pnfId;
	}

	public void setPnfId(String pnfId) {
		this.pnfId = pnfId;
	}

	public String getPnfName() {
		return pnfName;
	}

	public void setPnfName(String pnfName) {
		this.pnfName = pnfName;
	}

	public String getServiceModelUuid() {
		return serviceModelUuid;
	}

	public void setServiceModelUuid(String serviceModelUuid) {
		this.serviceModelUuid = serviceModelUuid;
	}

	public String getPnfCustomizationUuid() {
		return pnfCustomizationUuid;
	}

	public void setPnfCustomizationUuid(String pnfCustomizationUuid) {
		this.pnfCustomizationUuid = pnfCustomizationUuid;
	}

	public Map<String, Object> getUserParam() {
		return this.userParam;
	}

	public void setUserParam(String name, Object value) {
		this.userParam.put(name, value);
	}

	@Override
	public String toString() {

		final StringBuilder sb = new StringBuilder("{");
		sb.append("\"service-instance-id\":").append("\"").append(serviceInstanceId).append("\"");
		sb.append(", \"pnf-id\":").append("\"").append(pnfId).append("\"");
		sb.append(", \"pnf-name\":").append("\"").append(pnfName).append("\"");
		sb.append(", \"service-model-uuid\":").append("\"").append(serviceModelUuid).append("\"");
		sb.append(", \"pnf-customization-uuid\":").append("\"").append(pnfCustomizationUuid).append("\"");
		for (Map.Entry<String, Object> entry : userParam.entrySet()) {
			sb.append(",");
			sb.append("\"");
			sb.append(entry.getKey());
			sb.append("\"");
			sb.append(":");
			sb.append("\"");
			sb.append(entry.getValue());
			sb.append("\"");
		}
		sb.append('}');

		return sb.toString();
	}

}
