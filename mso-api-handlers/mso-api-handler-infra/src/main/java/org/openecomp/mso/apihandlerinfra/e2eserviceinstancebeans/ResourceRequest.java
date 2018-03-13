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
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class ResourceRequest {

    @JsonProperty("resourceName")
    private String resourceName;

    @JsonProperty("resourceInvariantUuid")
    private String resourceInvariantUuid;

    @JsonProperty("resourceUuid")
    private String resourceUuid;

    @JsonProperty("resourceCustomizationUuid")
    private String resourceCustomizationUuid;

    @JsonProperty("parameters")
    private E2EParameters parameters;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * @return Returns the resourceName.
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * @param resourceName The resourceName to set.
     */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public String getResourceInvariantUuid() {
        return resourceInvariantUuid;
    }

    public void setResourceInvariantUuid(String resourceInvariantUuid) {
        this.resourceInvariantUuid = resourceInvariantUuid;
    }

    public String getResourceUuid() {
        return resourceUuid;
    }

    public void setResourceUuid(String resourceUuid) {
        this.resourceUuid = resourceUuid;
    }

    public String getResourceCustomizationUuid() {
        return resourceCustomizationUuid;
    }

    public void setResourceCustomizationUuid(String resourceCustomizationUuid) {
        this.resourceCustomizationUuid = resourceCustomizationUuid;
    }

    public E2EParameters getParameters() {
        return parameters;
    }

    public void setParameters(E2EParameters parameters) {
        this.parameters = parameters;
    }
}
