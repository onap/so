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

package org.openecomp.mso.client.aai.objects;

import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.entities.AAIEntityObject;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AAIServiceInstance implements AAIEntityObject {
	
	@JsonProperty("service-instance-id")
	private String serviceInstanceId;
	@JsonProperty("service-instance-name")
	private String serviceInstanceName;	
	@JsonProperty("service-type")
	private String serviceType;
	@JsonProperty("service-role")
	private String serviceRole;
	@JsonProperty("orchestration-status")
	private String oStatus;
	@JsonProperty("model-invariant-id")
	private String modelInvariantUuid;
	@JsonProperty("model-version-id")
	private String modelUuid;
	@JsonProperty("environment-context")
	private String environmentContext;
	@JsonProperty("workload-context")
	private String workloadContext;
	
	public String getServiceInstanceName() {
		return serviceInstanceName;
	}

	public void setServiceInstanceName(String serviceInstanceName) {
		this.serviceInstanceName = serviceInstanceName;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getServiceRole() {
		return serviceRole;
	}

	public void setServiceRole(String serviceRole) {
		this.serviceRole = serviceRole;
	}

	public String getoStatus() {
		return oStatus;
	}

	public void setoStatus(String oStatus) {
		this.oStatus = oStatus;
	}

	public String getModelInvariantUuid() {
		return modelInvariantUuid;
	}

	public void setModelInvariantUuid(String modelInvariantUuid) {
		this.modelInvariantUuid = modelInvariantUuid;
	}

	public String getModelUuid() {
		return modelUuid;
	}

	public void setModelUuid(String modelUuid) {
		this.modelUuid = modelUuid;
	}

	public String getEnvironmentContext() {
		return environmentContext;
	}

	public void setEnvironmentContext(String environmentContext) {
		this.environmentContext = environmentContext;
	}

	public String getWorkloadContext() {
		return workloadContext;
	}

	public void setWorkloadContext(String workloadContext) {
		this.workloadContext = workloadContext;
	}

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}
	
	public AAIServiceInstance withServiceInstance(String serviceInstanceId) {
		this.setServiceInstanceId(serviceInstanceId);
		return this;
	}

	@Override
	public AAIResourceUri getUri() {
		final AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, this.serviceInstanceId);
		return uri;
	}
	
	
}
