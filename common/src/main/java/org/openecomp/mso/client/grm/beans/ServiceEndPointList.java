package org.openecomp.mso.client.grm.beans;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "serviceEndPointList" })
public class ServiceEndPointList {

	@JsonProperty("serviceEndPointList")
	private List<ServiceEndPoint> serviceEndPointList = null;

	@JsonProperty("serviceEndPointList")
	public List<ServiceEndPoint> getServiceEndPointList() {
		return serviceEndPointList;
	}

	@JsonProperty("serviceEndPointList")
	public void setServiceEndPointList(List<ServiceEndPoint> serviceEndPointList) {
		this.serviceEndPointList = serviceEndPointList;
	}

}