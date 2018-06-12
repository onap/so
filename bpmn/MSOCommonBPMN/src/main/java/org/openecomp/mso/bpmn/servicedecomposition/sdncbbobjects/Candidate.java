package org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Candidate implements Serializable {


	private static final long serialVersionUID = -3959572501582849328L;

	@JsonProperty("candidateType")
	private CandidateType candidateType;
	@JsonProperty("candidates")
	private List<String> candidates;

	/**
	 * list of candidates
	 * i.e. actual serviceInstanceId, actual vnfName, actual cloudRegionId, etc.
	 */
	public List<String> getCandidates() {
		return candidates;
	}

	/**
	 * list of candidates
	 * i.e. actual serviceInstanceId, actual vnfName, actual cloudRegionId, etc.
	 */
	public void setCandidates(List<String> candidates) {
		this.candidates = candidates;
	}

	/**
	 * Way to identify the type of candidate
	 * i.e. "serviceInstanceId", "vnfName", "cloudRegionId", etc.
	 */
	public CandidateType getCandidateType(){
		return candidateType;
	}

	/**
	 * Way to identify the type of candidate
	 * i.e. "serviceInstanceId", "vnfName", "cloudRegionId", etc.
	 */
	public void setCandidateType(CandidateType candidateType){
		this.candidateType = candidateType;
	}

}
