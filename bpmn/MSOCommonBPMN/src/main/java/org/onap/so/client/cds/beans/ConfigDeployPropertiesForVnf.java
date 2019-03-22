package org.onap.so.client.cds.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"service-instance-id",
"vnf-id",
"vnf-name",
"service-model-uuid",
"vnf-customization-uuid"
})
public class ConfigDeployPropertiesForVnf {

	@JsonProperty("service-instance-id")
	private String serviceInstanceId;

	@JsonProperty("vnf-id")
	private String vnfId;

	@JsonProperty("vnf-name")
	private String vnfName;

	@JsonProperty("service-model-uuid")
	private String serviceModelUuid;

	@JsonProperty("vnf-customization-uuid")
	private String vnfCustomizationUuid;

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}

	public String getVnfId() {
		return vnfId;
	}

	public void setVnfId(String vnfId) {
		this.vnfId = vnfId;
	}

	public String getVnfName() {
		return vnfName;
	}

	public void setVnfName(String vnfName) {
		this.vnfName = vnfName;
	}

	public String getServiceModelUuid() {
		return serviceModelUuid;
	}

	public void setServiceModelUuid(String serviceModelUuid) {
		this.serviceModelUuid = serviceModelUuid;
	}

	public String getVnfCustomizationUuid() {
		return vnfCustomizationUuid;
	}

	public void setVnfCustomizationUuid(String vnfCustomizationUuid) {
		this.vnfCustomizationUuid = vnfCustomizationUuid;
	}

	@Override
	public String toString() {

		final StringBuilder sb = new StringBuilder("{");
		sb.append("\"service-instance-id\":").append("\"").append(serviceInstanceId).append("\"");
		sb.append(", \"vnf-id\":").append("\"").append(vnfId).append("\"");
		sb.append(", \"vnf-name\":").append("\"").append(vnfName).append("\"");
		sb.append(", \"service-model-uuid\":").append("\"").append(serviceModelUuid).append("\"");
		sb.append(", \"vnf-customization-uuid\":").append("\"").append(vnfCustomizationUuid).append("\"");

		sb.append('}');

		return sb.toString();
	}

}
