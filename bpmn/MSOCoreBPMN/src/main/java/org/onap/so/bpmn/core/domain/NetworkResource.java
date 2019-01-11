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

package org.onap.so.bpmn.core.domain;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;


/**
 * Encapsulates Network resource data set
 *
 */
@JsonRootName("networkResource")
public class NetworkResource extends Resource {

	private static final long serialVersionUID = 1L;
	/*
	 * set resourceType for this object
	 */
	public NetworkResource(){
		resourceType = ResourceType.NETWORK;
		setResourceId(UUID.randomUUID().toString());
	}
	/*
	 * fields specific to Network resource type
	 */
	private String networkType;
	private String networkRole;
	private String networkTechnology;
	private String networkScope;

	@JsonIgnore
	private String resourceInput;

	/*
	 * GET and SET
	 */
	public String getNetworkType() {
		return networkType;
	}
	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}
	public String getNetworkRole() {
		return networkRole;
	}
	public void setNetworkRole(String networkRole) {
		this.networkRole = networkRole;
	}
	public String getNetworkTechnology() {
		return networkTechnology;
	}
	public void setNetworkTechnology(String networkTechnology) {
		this.networkTechnology = networkTechnology;
	}
	public String getNetworkScope() {
		return networkScope;
	}
	public void setNetworkScope(String networkScope) {
		this.networkScope = networkScope;
	}

	public String getResourceInput() {
		return resourceInput;
	}

	public void setResourceInput(String resourceInput) {
		this.resourceInput = resourceInput;
	}
}