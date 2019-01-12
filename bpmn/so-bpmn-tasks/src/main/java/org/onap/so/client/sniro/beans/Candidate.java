package org.onap.so.client.sniro.beans;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Candidate implements Serializable{

	private static final long serialVersionUID = -5474502255533410907L;

	@JsonProperty("candidateType")
	private CandidateType candidateType;
	@JsonProperty("candidates")
	private List<String> candidates;
	@JsonProperty("cloudOwner")
	private String cloudOwner;


	public CandidateType getCandidateType(){
		return candidateType;
	}

	public void setCandidateType(CandidateType candidateType){
		this.candidateType = candidateType;
	}

	public List<String> getCandidates(){
		return candidates;
	}

	public void setCandidates(List<String> candidates){
		this.candidates = candidates;
	}

	public String getCloudOwner(){
		return cloudOwner;
	}

	public void setCloudOwner(String cloudOwner){
		this.cloudOwner = cloudOwner;
	}

}
