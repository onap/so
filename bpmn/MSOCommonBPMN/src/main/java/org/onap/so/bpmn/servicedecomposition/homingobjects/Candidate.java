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

	@JsonProperty("identifierType")
	private CandidateType identifierType;
	@JsonProperty("identifiers")
	private List<String> identifiers;
	@JsonProperty("cloudOwner")
	private String cloudOwner;

	/**
	 * list of candidates
	 * i.e. actual serviceInstanceId, actual vnfName, actual cloudRegionId, etc.
	 */
	public List<String> getIdentifiers() {
		return identifiers;
	}

	/**
	 * list of candidates
	 * i.e. actual serviceInstanceId, actual vnfName, actual cloudRegionId, etc.
	 */
	public void setIdentifiers(List<String> identifiers) {
		this.identifiers = identifiers;
	}

	/**
	 * Way to identify the type of candidate
	 * i.e. "serviceInstanceId", "vnfName", "cloudRegionId", etc.
	 */
	public CandidateType getIdentifierType(){
		return identifierType;
	}

	/**
	 * Way to identify the type of candidate
	 * i.e. "serviceInstanceId", "vnfName", "cloudRegionId", etc.
	 */
	public void setIdentifierType(CandidateType identifierType){
		this.identifierType = identifierType;
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
