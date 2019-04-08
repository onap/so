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

public class ModelInfoGenericVnf extends ModelInfoMetadata implements Serializable {

    private static final long serialVersionUID = -5963022750248280669L;

    @JsonProperty("tosca-node-type")
    private String ToscaNodeType;
    @JsonProperty("description")
    private String Description;
    @JsonProperty("orchestration-mode")
    private String OrchestrationMode;
    @JsonProperty("aic-version-min")
    private String AicVersionMin;
    @JsonProperty("aic-version-max")
    private String AicVersionMax;
    @JsonProperty("min-instances")
    private String MinInstances;
    @JsonProperty("max-instances")
    private String MaxInstances;
    @JsonProperty("availability-zone-max-count")
    private String AvailabilityZoneMaxCount;
    @JsonProperty("nf-function")
    private String NfFunction;
    @JsonProperty("nf-type")
    private String NfType;
    @JsonProperty("nf-role")
    private String NfRole;
    @JsonProperty("nf-naming-code")
    private String NfNamingCode;
    @JsonProperty("multi-stage-design")
    private String MultiStageDesign;
    @JsonProperty("created")
    private String Created;


    public String getToscaNodeType() {
        return ToscaNodeType;
    }

    public void setToscaNodeType(String toscaNodeType) {
        ToscaNodeType = toscaNodeType;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getOrchestrationMode() {
        return OrchestrationMode;
    }

    public void setOrchestrationMode(String orchestrationMode) {
        OrchestrationMode = orchestrationMode;
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

    public String getMinInstances() {
        return MinInstances;
    }

    public void setMinInstances(String minInstances) {
        MinInstances = minInstances;
    }

    public String getMaxInstances() {
        return MaxInstances;
    }

    public void setMaxInstances(String maxInstances) {
        MaxInstances = maxInstances;
    }

    public String getAvailabilityZoneMaxCount() {
        return AvailabilityZoneMaxCount;
    }

    public void setAvailabilityZoneMaxCount(String availabilityZoneMaxCount) {
        AvailabilityZoneMaxCount = availabilityZoneMaxCount;
    }

    public String getNfFunction() {
        return NfFunction;
    }

    public void setNfFunction(String nfFunction) {
        NfFunction = nfFunction;
    }

    public String getNfType() {
        return NfType;
    }

    public void setNfType(String nfType) {
        NfType = nfType;
    }

    public String getNfRole() {
        return NfRole;
    }

    public void setNfRole(String nfRole) {
        NfRole = nfRole;
    }

    public String getNfNamingCode() {
        return NfNamingCode;
    }

    public void setNfNamingCode(String nfNamingCode) {
        NfNamingCode = nfNamingCode;
    }

    public String getMultiStageDesign() {
        return MultiStageDesign;
    }

    public void setMultiStageDesign(String multiStageDesign) {
        MultiStageDesign = multiStageDesign;
    }

    public String getCreated() {
        return Created;
    }

    public void setCreated(String created) {
        Created = created;
    }
}
