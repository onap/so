package org.onap.so.client.sniro.beans;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Demand implements Serializable{

	private static final long serialVersionUID = 5676094538091859816L;

	@JsonProperty("serviceResourceId")
	private String serviceResourceId;
	@JsonProperty("resourceModuleName")
	private String resourceModuleName;
	@JsonProperty("resourceModelInfo")
	private ModelInfo modelInfo;
	@JsonProperty("requiredCandidates")
	private List<Candidate> requiredCandidates;
	@JsonProperty("excludedCandidates")
	private List<Candidate> excludedCandidates;


	public List<Candidate> getRequiredCandidates(){
		return requiredCandidates;
	}

	public void setRequiredCandidates(List<Candidate> requiredCandidates){
		this.requiredCandidates = requiredCandidates;
	}

	public List<Candidate> getExcludedCandidates(){
		return excludedCandidates;
	}

	public void setExcludedCandidates(List<Candidate> excludedCandidates){
		this.excludedCandidates = excludedCandidates;
	}

	public String getServiceResourceId(){
		return serviceResourceId;
	}

	public void setServiceResourceId(String serviceResourceId){
		this.serviceResourceId = serviceResourceId;
	}

	public String getResourceModuleName(){
		return resourceModuleName;
	}

	public void setResourceModuleName(String resourceModuleName){
		this.resourceModuleName = resourceModuleName;
	}

	public ModelInfo getModelInfo(){
		return modelInfo;
	}

	public void setModelInfo(ModelInfo modelInfo){
		this.modelInfo = modelInfo;
	}

}
