package org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.Candidate;

public class SolutionCandidates implements Serializable{

	private static final long serialVersionUID = 2188754118148819627L;

	@JsonProperty("requiredCandidates")
	private List<Candidate> requiredCandidates = new ArrayList<Candidate>();
	@JsonProperty("excludedCandidates")
	private List<Candidate> excludedCandidates = new ArrayList<Candidate>();
	//TODO figure out best way to do this
	@JsonProperty("existingCandidates")
	private List<Candidate> existingCandidates = new ArrayList<Candidate>();


	public List<Candidate> getRequiredCandidates() {
		return requiredCandidates;
	}
	public void addRequiredCandidates(Candidate requiredCandidate) {
		this.requiredCandidates.add(requiredCandidate);
	}
	public List<Candidate> getExcludedCandidates() {
		return excludedCandidates;
	}
	public void addExcludedCandidates(Candidate excludedCandidate) {
		this.excludedCandidates.add(excludedCandidate);
	}

	public List<Candidate> getExistingCandidates(){
		return existingCandidates;
	}






}
