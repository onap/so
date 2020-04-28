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

package org.onap.so.client.aai.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"configuration-id", "configuration-type", "configuration-sub-type", "model-invariant-id",
        "model-version-id", "orchestration-status", "operational-status", "configuration-selflink",
        "model-customization-id"})
public class Configuration {

    @JsonProperty("configuration-id")
    private String configurationId;
    @JsonProperty("configuration-name")
    private String configurationName;
    @JsonProperty("configuration-type")
    private String configurationType;
    @JsonProperty("configuration-sub-type")
    private String configurationSubType;
    @JsonProperty("model-invariant-id")
    private String modelInvariantId;
    @JsonProperty("model-version-id")
    private String modelVersionId;
    @JsonProperty("orchestration-status")
    private String orchestrationStatus;
    @JsonProperty("operational-status")
    private String operationalStatus;
    @JsonProperty("configuration-selflink")
    private String configurationSelflink;
    @JsonProperty("model-customization-id")
    private String modelCustomizationId;

    @JsonProperty("configuration-id")
    public String getConfigurationId() {
        return configurationId;
    }

    @JsonProperty("configuration-id")
    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
    }

    @JsonProperty("configuration-name")
    public String getConfigurationName() {
        return configurationName;
    }

    @JsonProperty("configuration-name")
    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    @JsonProperty("configuration-type")
    public String getConfigurationType() {
        return configurationType;
    }

    @JsonProperty("configuration-type")
    public void setConfigurationType(String configurationType) {
        this.configurationType = configurationType;
    }

    @JsonProperty("configuration-sub-type")
    public String getConfigurationSubType() {
        return configurationSubType;
    }

    @JsonProperty("configuration-sub-type")
    public void setConfigurationSubType(String configurationSubType) {
        this.configurationSubType = configurationSubType;
    }

    @JsonProperty("model-invariant-id")
    public String getModelInvariantId() {
        return modelInvariantId;
    }

    @JsonProperty("model-invariant-id")
    public void setModelInvariantId(String modelInvariantId) {
        this.modelInvariantId = modelInvariantId;
    }

    @JsonProperty("model-version-id")
    public String getModelVersionId() {
        return modelVersionId;
    }

    @JsonProperty("model-version-id")
    public void setModelVersionId(String modelVersionId) {
        this.modelVersionId = modelVersionId;
    }

    @JsonProperty("orchestration-status")
    public String getOrchestrationStatus() {
        return orchestrationStatus;
    }

    @JsonProperty("orchestration-status")
    public void setOrchestrationStatus(String orchestrationStatus) {
        this.orchestrationStatus = orchestrationStatus;
    }

    @JsonProperty("operational-status")
    public String getOperationalStatus() {
        return operationalStatus;
    }

    @JsonProperty("operational-status")
    public void setOperationalStatus(String operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    @JsonProperty("model-customization-id")
    public String getModelCustomizationId() {
        return modelCustomizationId;
    }

    @JsonProperty("model-customization-id")
    public void setModelCustomizationId(String modelCustomizationId) {
        this.modelCustomizationId = modelCustomizationId;
    }

    @JsonProperty("configuration-selflink")
    public String getConfigurationSelflink() {
        return configurationSelflink;
    }

    @JsonProperty("configuration-selflink")
    public void setConfigurationSelflink(String configurationSelflink) {
        this.configurationSelflink = configurationSelflink;
    }
}
