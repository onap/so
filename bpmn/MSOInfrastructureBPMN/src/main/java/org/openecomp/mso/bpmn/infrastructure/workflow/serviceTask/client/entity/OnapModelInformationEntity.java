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

package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class OnapModelInformationEntity {
    @JsonProperty("GENERIC-RESOURCE-API:model-invariant-uuid")
    private String modelInvariantUuid;

    @JsonProperty("GENERIC-RESOURCE-API:model-customization-uuid")
    private String modelCustomizationUuid;

    @JsonProperty("GENERIC-RESOURCE-API:model-uuid")
    private String modelUuid;

    @JsonProperty("GENERIC-RESOURCE-API:model-version")
    private String modelVersion;

    @JsonProperty("GENERIC-RESOURCE-API:model-name")
    private String modelName;

    public String getModelInvariantUuid() {
        return modelInvariantUuid;
    }

    public void setModelInvariantUuid(String modelInvariantUuid) {
        this.modelInvariantUuid = modelInvariantUuid;
    }

    public String getModelCustomizationUuid() {
        return modelCustomizationUuid;
    }

    public void setModelCustomizationUuid(String modelCustomizationUuid) {
        this.modelCustomizationUuid = modelCustomizationUuid;
    }

    public String getModelUuid() {
        return modelUuid;
    }

    public void setModelUuid(String modelUuid) {
        this.modelUuid = modelUuid;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
