package org.openecomp.mso.client.grm.beans;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "serviceEndPoint", "env" })
public class ServiceEndPointRequest {

	@JsonProperty("serviceEndPoint")
	private ServiceEndPoint serviceEndPoint;
	@JsonProperty("env")
	private String env;

	@JsonProperty("serviceEndPoint")
	public ServiceEndPoint getServiceEndPoint() {
		return serviceEndPoint;
	}

	@JsonProperty("serviceEndPoint")
	public void setServiceEndPoint(ServiceEndPoint serviceEndPoint) {
		this.serviceEndPoint = serviceEndPoint;
	}

	@JsonProperty("env")
	public String getEnv() {
		return env;
	}

	@JsonProperty("env")
	public void setEnv(String env) {
		this.env = env;
	}

}