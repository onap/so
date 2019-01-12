package org.onap.so.client.sniro.beans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonPropertyOrder({
    "modelInfo",
    "serviceRole",
    "serviceInstanceId",
    "serviceName"
})
@JsonRootName("serviceInfo")
public class ServiceInfo implements Serializable{

	private static final long serialVersionUID = -6866022419398548585L;

	@JsonProperty("serviceInstanceId")
	private String serviceInstanceId;
	@JsonProperty("serviceName")
	private String serviceName;
	@JsonProperty("serviceRole")
	private String serviceRole;
	@JsonProperty("modelInfo")
	private ModelInfo modelInfo;


	public String getServiceInstanceId(){
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId){
		this.serviceInstanceId = serviceInstanceId;
	}

	public String getServiceName(){
		return serviceName;
	}

	public void setServiceName(String serviceName){
		this.serviceName = serviceName;
	}

	public String getServiceRole(){
		return serviceRole;
	}

	public void setServiceRole(String serviceRole){
		this.serviceRole = serviceRole;
	}

	public ModelInfo getModelInfo(){
		return modelInfo;
	}

	public void setModelInfo(ModelInfo modelInfo){
		this.modelInfo = modelInfo;
	}

}
