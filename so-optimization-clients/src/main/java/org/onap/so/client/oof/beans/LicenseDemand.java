/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Intel Corp.  All rights reserved.
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

package org.onap.so.client.oof.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"resourceModuleName", "serviceResourceId", "tenantId", "resourceModelInfo"})
@JsonRootName("licenseDemand")
public class LicenseDemand implements Serializable {

    private static final long serialVersionUID = -759180997599143791L;

    @JsonProperty("resourceModuleName")
    private String resourceModuleName;
    @JsonProperty("serviceResourceId")
    private String serviceResourceId;
    @JsonProperty("tenantId")
    private String tenantId;
    @JsonProperty("resourceModelInfo")
    private ResourceModelInfo resourceModelInfo;

    @JsonProperty("resourceModuleName")
    public String getResourceModuleName() {
        return resourceModuleName;
    }

    @JsonProperty("resourceModuleName")
    public void setResourceModuleName(String resourceModuleName) {
        this.resourceModuleName = resourceModuleName;
    }

    @JsonProperty("serviceResourceId")
    public String getServiceResourceId() {
        return serviceResourceId;
    }

    @JsonProperty("serviceResourceId")
    public void setServiceResourceId(String serviceResourceId) {
        this.serviceResourceId = serviceResourceId;
    }

    @JsonProperty("tenantId")
    public String getTenantId() {
        return tenantId;
    }

    @JsonProperty("tenantId")
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @JsonProperty("resourceModelInfo")
    public ResourceModelInfo getResourceModelInfo() {
        return resourceModelInfo;
    }

    @JsonProperty("resourceModelInfo")
    public void setResourceModelInfo(ResourceModelInfo resourceModelInfo) {
        this.resourceModelInfo = resourceModelInfo;
    }

    public void setResourceModelInfo(ModelInfo modelInfo) {
        ResourceModelInfo localResourceModelInfo = new ResourceModelInfo();
        localResourceModelInfo.setModelVersionId(modelInfo.getModelVersionId());
        localResourceModelInfo.setModelVersionId(modelInfo.getModelVersionId());
        localResourceModelInfo.setModelVersion(modelInfo.getModelVersion());
        localResourceModelInfo.setModelName(modelInfo.getModelName());
        localResourceModelInfo.setModelType(modelInfo.getModelType());
        localResourceModelInfo.setModelInvariantId(modelInfo.getModelInvariantId());
        localResourceModelInfo.setModelCustomizationName(modelInfo.getModelCustomizationName());
        this.resourceModelInfo = localResourceModelInfo;
    }

}
