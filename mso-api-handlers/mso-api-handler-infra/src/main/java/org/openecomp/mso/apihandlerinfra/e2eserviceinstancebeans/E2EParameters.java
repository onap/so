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
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties({ "additionalProperties" })
public class E2EParameters {

	@JsonProperty("locationConstraints")
	List<LocationConstraint> locationConstraints;

	@JsonProperty("resources")
	private List<ResourceRequest> resources;

	@JsonProperty("requestInputs")
	private HashMap<String, ?> requestInputs;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<>();

	public Map<String, Object> getAdditionalProperties() {
		return additionalProperties;
	}

	public void setAdditionalProperties(Map<String, Object> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}
    
    /**
     * @return Returns the resources.
     */
    public List<ResourceRequest> getResources() {
        return resources;
    }
    
    /**
     * @param resources The resources to set.
     */
    public void setResources(List<ResourceRequest> resources) {
        this.resources = resources;
    }

    public List<LocationConstraint> getLocationConstraints() {
        return locationConstraints;
    }

    public void setLocationConstraints(List<LocationConstraint> locationConstraints) {
        this.locationConstraints = locationConstraints;
    }

	public HashMap<String, ?> getRequestInputs() {
		return requestInputs;
	}

	public void setRequestInputs(HashMap<String, ?> requestInputs) {
		this.requestInputs = requestInputs;
	}
}
