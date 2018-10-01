/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.servicedecomposition.homingobjects;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Candidate implements Serializable {


	private static final long serialVersionUID = -3959572501582849328L;

	@JsonProperty("candidateType")
	private CandidateType candidateType;
	@JsonProperty("candidates")
	private List<String> candidates;
	@JsonProperty("cloudOwner")
	private String cloudOwner;

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

	/**
	 * The name of the cloud owner. Required if candidateType is cloudRegionId
	 */
	public String getCloudOwner(){
		return cloudOwner;
	}

	/**
	 * The name of the cloud owner. Required if candidateType is cloudRegionId
	 */
	public void setCloudOwner(String cloudOwner){
		this.cloudOwner = cloudOwner;
	}



}
