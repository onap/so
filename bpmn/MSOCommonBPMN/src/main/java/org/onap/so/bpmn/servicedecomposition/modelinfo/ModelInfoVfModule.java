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

public class ModelInfoVfModule implements Serializable {

    private static final long serialVersionUID = 636556989022688657L;

    @JsonProperty("model-customization-uuid")
    private String ModelCustomizationUUID;
    @JsonProperty("model-name")
    private String ModelName;
    @JsonProperty("model-uuid")
    private String ModelUUID;
    @JsonProperty("model-invariant-uuid")
    private String ModelInvariantUUID;
    @JsonProperty("model-version")
    private String ModelVersion;
    @JsonProperty("description")
    private String Description;
    @JsonProperty("is-base-boolean")
    private Boolean IsBaseBoolean;
    @JsonProperty("min-instances")
    private String MinInstances;
    @JsonProperty("max-instances")
    private String MaxInstances;
    @JsonProperty("availability-zone-count")
    private String AvailabilityZoneCount;
    @JsonProperty("label")
    private String Label;
    @JsonProperty("initial-count")
    private String InitialCount;
    @JsonProperty("created")
    private String Created;

    public String getModelCustomizationUUID() {
        return ModelCustomizationUUID;
    }

    public void setModelCustomizationUUID(String modelCustomizationUUID) {
        ModelCustomizationUUID = modelCustomizationUUID;
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

    public String getModelInvariantUUID() {
        return ModelInvariantUUID;
    }

    public void setModelInvariantUUID(String modelInvariantUUID) {
        ModelInvariantUUID = modelInvariantUUID;
    }

    public String getModelVersion() {
        return ModelVersion;
    }

    public void setModelVersion(String modelVersion) {
        ModelVersion = modelVersion;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public Boolean getIsBaseBoolean() {
        return IsBaseBoolean;
    }

    public void setIsBaseBoolean(Boolean isBaseBoolean) {
        IsBaseBoolean = isBaseBoolean;
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

    public String getAvailabilityZoneCount() {
        return AvailabilityZoneCount;
    }

    public void setAvailabilityZoneCount(String availabilityZoneCount) {
        AvailabilityZoneCount = availabilityZoneCount;
    }

    public String getLabel() {
        return Label;
    }

    public void setLabel(String label) {
        Label = label;
    }

    public String getInitialCount() {
        return InitialCount;
    }

    public void setInitialCount(String initialCount) {
        InitialCount = initialCount;
    }

    public String getCreated() {
        return Created;
    }

    public void setCreated(String created) {
        Created = created;
    }
}
