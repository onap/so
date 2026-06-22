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

package org.onap.so.bpmn.servicedecomposition.bbobjects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import jakarta.persistence.Id;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;

@JsonRootName("volume-group")
public class VolumeGroup implements Serializable, ShallowCopy<VolumeGroup> {

    private static final long serialVersionUID = 870124265764370922L;

    @Id
    @JsonProperty("volume-group-id")
    private String volumeGroupId;
    @JsonProperty("volume-group-name")
    private String volumeGroupName;
    @JsonProperty("vnf-type")
    private String vnfType;
    @JsonProperty("orchestration-status")
    private OrchestrationStatus orchestrationStatus;
    @JsonProperty("cloud-params")
    private Map<String, String> cloudParams = new HashMap<>();
    @JsonProperty("cascaded")
    private Boolean cascaded;
    @JsonProperty("heat-stack-id")
    private String heatStackId;
    @JsonProperty("model-info-vf-module")
    private ModelInfoVfModule modelInfoVfModule;

    public ModelInfoVfModule getModelInfoVfModule() {
        return modelInfoVfModule;
    }

    public void setModelInfoVfModule(ModelInfoVfModule modelInfoVfModule) {
        this.modelInfoVfModule = modelInfoVfModule;
    }

    public String getHeatStackId() {
        return heatStackId;
    }

    public void setHeatStackId(String heatStackId) {
        this.heatStackId = heatStackId;
    }

    public String getVolumeGroupId() {
        return volumeGroupId;
    }

    public void setVolumeGroupId(String volumeGroupId) {
        this.volumeGroupId = volumeGroupId;
    }

    public String getVolumeGroupName() {
        return volumeGroupName;
    }

    public void setVolumeGroupName(String volumeGroupName) {
        this.volumeGroupName = volumeGroupName;
    }

    public String getVnfType() {
        return vnfType;
    }

    public void setVnfType(String vnfType) {
        this.vnfType = vnfType;
    }

    public OrchestrationStatus getOrchestrationStatus() {
        return orchestrationStatus;
    }

    public void setOrchestrationStatus(OrchestrationStatus orchestrationStatus) {
        this.orchestrationStatus = orchestrationStatus;
    }

    public Map<String, String> getCloudParams() {
        return cloudParams;
    }

    public void setCloudParams(Map<String, String> cloudParams) {
        this.cloudParams = cloudParams;
    }

    public Boolean isCascaded() {
        return cascaded;
    }

    public void setCascaded(boolean cascaded) {
        this.cascaded = cascaded;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof VolumeGroup)) {
            return false;
        }
        VolumeGroup castOther = (VolumeGroup) other;
        return new EqualsBuilder().append(volumeGroupId, castOther.volumeGroupId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(volumeGroupId).toHashCode();
    }
}
