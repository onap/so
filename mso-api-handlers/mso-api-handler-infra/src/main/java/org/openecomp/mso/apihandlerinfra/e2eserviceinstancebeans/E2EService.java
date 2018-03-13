/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.apihandlerinfra.e2eserviceinstancebeans;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties({ "additionalProperties" })
public class E2EService {

	@JsonProperty("name")
	private String name;

	@JsonProperty("description")
	private String description;

	@JsonProperty("serviceInvariantUuid")
	private String serviceInvariantUuid;

	@JsonProperty("serviceUuid")
	private String serviceUuid;

	@JsonProperty("globalSubscriberId")
	private String globalSubscriberId;

	@JsonProperty("serviceType")
	private String serviceType;

	@JsonProperty("parameters")
	private E2EParameters parameters;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public E2EParameters getParameters() {
		return parameters;
	}

	public void setParameters(E2EParameters parameters) {
		this.parameters = parameters;
	}

	public Map<String, Object> getAdditionalProperties() {
		return additionalProperties;
	}

	public void setAdditionalProperties(Map<String, Object> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}

	public String getServiceInvariantUuid() {
		return serviceInvariantUuid;
	}

	public void setServiceInvariantUuid(String serviceInvariantUuid) {
		this.serviceInvariantUuid = serviceInvariantUuid;
	}

	public String getGlobalSubscriberId() {
		return globalSubscriberId;
	}

	public void setGlobalSubscriberId(String globalSubscriberId) {
		this.globalSubscriberId = globalSubscriberId;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getServiceUuid() {
		return serviceUuid;
	}

	public void setServiceUuid(String serviceUuid) {
		this.serviceUuid = serviceUuid;
	}
}
