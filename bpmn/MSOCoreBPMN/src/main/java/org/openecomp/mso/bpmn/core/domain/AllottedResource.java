/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.bpmn.core.domain;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Stores allotted-resource information
 *
 */
@JsonRootName("allottedResource")
public class AllottedResource extends Resource {

	private static final long serialVersionUID = 1L;

	/*
	 * set resourceType for this object
	 */
	public AllottedResource(){
		resourceType = ResourceType.ALLOTTED_RESOURCE;
		setResourceId(UUID.randomUUID().toString());
	}

	/*
	 * fields specific to Allotted Resource resource type
	 */
	private String allottedResourceType;
	private String allottedResourceRole;
	private String providingServiceModelName;
	private String providingServiceModelInvariantUuid;
	private String providingServiceModelUuid;
	private String nfFunction;
	private String nfType;
	private String nfRole;
	private String nfNamingCode;
	private String orchestrationStatus;
	private TunnelConnect tunnelConnect;

	/*
	 * GET and SET
	 */
	public String getAllottedResourceType() {
		return allottedResourceType;
	}
	public void setAllottedResourceType(String allottedResourceType) {
		this.allottedResourceType = allottedResourceType;
	}
	public String getAllottedResourceRole() {
		return allottedResourceRole;
	}
	public void setAllottedResourceRole(String allottedResourceRole) {
		this.allottedResourceRole = allottedResourceRole;
	}
	public String getProvidingServiceModelName() {
		return providingServiceModelName;
	}
	public void setProvidingServiceModelName(String providingServiceModelName) {
		this.providingServiceModelName = providingServiceModelName;
	}
	public String getProvidingServiceModelInvariantUuid() {
		return providingServiceModelInvariantUuid;
	}
	public void setProvidingServiceModelInvariantUuid(
			String providingServiceModelInvariantUuid) {
		this.providingServiceModelInvariantUuid = providingServiceModelInvariantUuid;
	}
	public String getProvidingServiceModelUuid() {
		return providingServiceModelUuid;
	}
	public void setProvidingServiceModelUuid(String providingServiceModelUuid) {
		this.providingServiceModelUuid = providingServiceModelUuid;
	}
	public String getNfFunction() {
		return nfFunction;
	}
	public void setNfFunction(String nfFunction) {
		this.nfFunction = nfFunction;
	}
	public String getNfType() {
		return nfType;
	}
	public void setNfType(String nfType) {
		this.nfType = nfType;
	}
	public String getNfRole() {
		return nfRole;
	}
	public void setNfRole(String nfRole) {
		this.nfRole = nfRole;
	}
	public String getNfNamingCode() {
		return nfNamingCode;
	}
	public void setNfNamingCode(String nfNamingCode) {
		this.nfNamingCode = nfNamingCode;
	}
	public String getOrchestrationStatus() {
		return orchestrationStatus;
	}
	public void setOrchestrationStatus(String orchestrationStatus) {
		this.orchestrationStatus = orchestrationStatus;
	}
	public TunnelConnect getTunnelConnect() {
		return tunnelConnect;
	}
	public void setTunnelConnect(TunnelConnect tunnelConnect) {
		this.tunnelConnect = tunnelConnect;
	}
}