package org.openecomp.mso.client.avpn.dmaap.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "instanceReferences")
public class InstanceReferences {

	@JsonProperty("serviceInstanceId")
	private String serviceInstanceId;

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}
}
