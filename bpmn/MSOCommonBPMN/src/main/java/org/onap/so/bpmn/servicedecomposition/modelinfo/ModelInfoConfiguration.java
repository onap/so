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

package org.onap.so.bpmn.servicedecomposition.modelinfo;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelInfoConfiguration implements Serializable{

	private static final long serialVersionUID = -387242776138157250L;
	
	@JsonProperty("model-invariant-id")
	private String modelInvariantId;
	@JsonProperty("model-version-id")
	private String modelVersionId;
	@JsonProperty("model-customization-id")
	private String modelCustomizationId;
	@JsonProperty("configuration-type")
	private String configurationType;
	@JsonProperty("configuration-role")
	private String configurationRole;
	@JsonProperty("policy-name")
	private String policyName;
	
	public String getConfigurationRole() {
		return configurationRole;
	}
	public void setConfigurationRole(String configurationRole) {
		this.configurationRole = configurationRole;
	}
	public String getConfigurationType() {
		return configurationType;
	}
	public void setConfigurationType(String configurationType) {
		this.configurationType = configurationType;
	}
	public String getModelInvariantId() {
		return modelInvariantId;
	}
	public void setModelInvariantId(String modelInvariantId) {
		this.modelInvariantId = modelInvariantId;
	}
	public String getModelVersionId() {
		return modelVersionId;
	}
	public void setModelVersionId(String modelVersionId) {
		this.modelVersionId = modelVersionId;
	}
	public String getModelCustomizationId() {
		return modelCustomizationId;
	}
	public void setModelCustomizationId(String modelCustomizationId) {
		this.modelCustomizationId = modelCustomizationId;
	}
	public String getPolicyName() {
		return policyName;
	}
	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}
}
