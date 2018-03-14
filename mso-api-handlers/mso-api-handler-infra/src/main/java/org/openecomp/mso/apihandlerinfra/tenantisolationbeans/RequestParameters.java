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

package org.openecomp.mso.apihandlerinfra.tenantisolationbeans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "requestParameters")
@JsonInclude(Include.NON_DEFAULT)
public class RequestParameters implements Serializable {

	private static final long serialVersionUID = 8530327178156183693L;
	@JsonProperty("operationalEnvironmentType")
	private OperationalEnvironment operationalEnvironmentType;
	@JsonProperty("tenantContext")
	private String tenantContext;
	@JsonProperty("workloadContext")
	private String workloadContext;
	@JsonProperty("manifest")
	private Manifest manifest;	
	
	public OperationalEnvironment getOperationalEnvironmentType() {
		return operationalEnvironmentType;
	}

	public void setOperationalEnvironmentType(OperationalEnvironment operationalEnvironmentType) {
		this.operationalEnvironmentType = operationalEnvironmentType;
	}

	public String getTenantContext() {
		return tenantContext;
	}

	public void setTenantContext(String tenantContext) {
		this.tenantContext = tenantContext;
	}
	
	public String getWorkloadContext() {
		return workloadContext;
	}

	public void setWorkloadContext(String workloadContext) {
		this.workloadContext = workloadContext;
	}		
	
	public Manifest getManifest() {
		return manifest;
	}

	public void setManifest(Manifest manifest) {
		this.manifest = manifest;
	}	
	

	@Override
	public String toString() {
		return "RequestParameters [operationalEnvironmentType=" + operationalEnvironmentType 
			                  + ", tenantContext=" + tenantContext
				              + ", workloadContext=" + workloadContext  
				              + ", manifes=" + manifest +"]";
	}
}
