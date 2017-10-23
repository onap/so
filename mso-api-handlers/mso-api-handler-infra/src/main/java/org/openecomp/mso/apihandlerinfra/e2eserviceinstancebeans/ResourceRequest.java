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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class ResourceRequest {

    @JsonProperty("resourceName")
    private String resourceName;

    @JsonProperty("resourceDefId")
    private String resourceDefId;

    @JsonProperty("resourceId")
    private String resourceId;

    @JsonProperty("nsParameters")
    private NsParameters nsParameters = null;

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

    /**
     * @return Returns the resourceDefId.
     */
    public String getResourceDefId() {
        return resourceDefId;
    }

    /**
     * @param resourceDefId The resourceDefId to set.
     */
    public void setResourceDefId(String resourceDefId) {
        this.resourceDefId = resourceDefId;
    }

    /**
     * @return Returns the resourceId.
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * @param resourceId The resourceId to set.
     */
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    /**
     * @return Returns the nsParameters.
     */
    public NsParameters getNsParameters() {
        return nsParameters;
    }

    /**
     * @param nsParameters The nsParameters to set.
     */
    public void setNsParameters(NsParameters nsParameters) {
        this.nsParameters = nsParameters;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

}
