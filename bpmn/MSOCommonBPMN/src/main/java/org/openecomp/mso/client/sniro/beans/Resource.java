package org.openecomp.mso.client.sniro.beans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Resource implements Serializable{

	private static final long serialVersionUID = 5949861520571440421L;

	@JsonProperty("service-resource-id")
	private String serviceResourceId;
	@JsonProperty("status")
	private String status;


	public String getServiceResourceId(){
		return serviceResourceId;
	}

	public void setServiceResourceId(String serviceResourceId){
		this.serviceResourceId = serviceResourceId;
	}

	public String getStatus(){
		return status;
	}

	public void setStatus(String status){
		this.status = status;
	}



}
