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
import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelInfoNetwork implements Serializable {

    private static final long serialVersionUID = -3612850497900637132L;

    @JsonProperty("model-customization-uuid")
    private String ModelCustomizationUUID;
    @JsonProperty("model-instance-name")
    private String ModelInstanceName;
    @JsonProperty("network-technology")
    private String NetworkTechnology;
    @JsonProperty("network-type")
    private String NetworkType;
    @JsonProperty("network-scope")
    private String NetworkScope;
    @JsonProperty("network-role")
    private String NetworkRole;
    @JsonProperty("description")
    private String Description;
    @JsonProperty("created")
    private Timestamp Created;
    @JsonProperty("model-version")
    private String ModelVersion;
    @JsonProperty("model-invariant-uuid")
    private String ModelInvariantUUID;
    @JsonProperty("model-name")
    private String ModelName;
    @JsonProperty("model-uuid")
    private String ModelUUID;
    @JsonProperty("neutron-network-type")
    private String NeutronNetworkType;
    @JsonProperty("aic-version-min")
    private String AicVersionMin;
    @JsonProperty("aic-version-max")
    private String AicVersionMax;
    @JsonProperty("orchestration-mode")
    private String OrchestrationMode;
    @JsonProperty("tosca-node-type")
    private String ToscaNodeType;

    public String getModelCustomizationUUID() {
        return ModelCustomizationUUID;
    }

    public void setModelCustomizationUUID(String modelCustomizationUUID) {
        ModelCustomizationUUID = modelCustomizationUUID;
    }

    public String getModelInstanceName() {
        return ModelInstanceName;
    }

    public void setModelInstanceName(String modelInstanceName) {
        ModelInstanceName = modelInstanceName;
    }

    public String getNetworkTechnology() {
        return NetworkTechnology;
    }

    public void setNetworkTechnology(String networkTechnology) {
        NetworkTechnology = networkTechnology;
    }

    public String getNetworkType() {
        return NetworkType;
    }

    public void setNetworkType(String networkType) {
        NetworkType = networkType;
    }

    public String getNetworkScope() {
        return NetworkScope;
    }

    public void setNetworkScope(String networkScope) {
        NetworkScope = networkScope;
    }

    public String getNetworkRole() {
        return NetworkRole;
    }

    public void setNetworkRole(String networkRole) {
        NetworkRole = networkRole;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public Timestamp getCreated() {
        return Created;
    }

    public void setCreated(Timestamp created) {
        Created = created;
    }

    public String getModelVersion() {
        return ModelVersion;
    }

    public void setModelVersion(String modelVersion) {
        ModelVersion = modelVersion;
    }

    public String getModelInvariantUUID() {
        return ModelInvariantUUID;
    }

    public void setModelInvariantUUID(String modelInvariantUUID) {
        ModelInvariantUUID = modelInvariantUUID;
    }

    public String getModelName() {
        return ModelName;
    }

    public void setModelName(String modelName) {
        ModelName = modelName;
    }

    public String getModelUUID() {
        return ModelUUID;
    }

    public void setModelUUID(String modelUUID) {
        ModelUUID = modelUUID;
    }

    public String getNeutronNetworkType() {
        return NeutronNetworkType;
    }

    public void setNeutronNetworkType(String neutronNetworkType) {
        NeutronNetworkType = neutronNetworkType;
    }

    public String getAicVersionMin() {
        return AicVersionMin;
    }

    public void setAicVersionMin(String aicVersionMin) {
        AicVersionMin = aicVersionMin;
    }

    public String getAicVersionMax() {
        return AicVersionMax;
    }

    public void setAicVersionMax(String aicVersionMax) {
        AicVersionMax = aicVersionMax;
    }

    public String getOrchestrationMode() {
        return OrchestrationMode;
    }

    public void setOrchestrationMode(String orchestrationMode) {
        OrchestrationMode = orchestrationMode;
    }

    public String getToscaNodeType() {
        return ToscaNodeType;
    }

    public void setToscaNodeType(String toscaNodeType) {
        ToscaNodeType = toscaNodeType;
    }
}
