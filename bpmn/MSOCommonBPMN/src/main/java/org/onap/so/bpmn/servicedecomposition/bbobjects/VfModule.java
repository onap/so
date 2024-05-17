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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.persistence.Id;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("vf-module")
public class VfModule implements Serializable, ShallowCopy<VfModule> {

    private static final long serialVersionUID = 6570087672008609773L;

    @Id
    @JsonProperty("vf-module-id")
    private String vfModuleId;
    @JsonProperty("vf-module-name")
    private String vfModuleName;
    @JsonProperty("orchestration-status")
    private OrchestrationStatus orchestrationStatus;
    @JsonProperty("cloud-params")
    private Map<String, String> cloudParams = new HashMap<>();
    @JsonProperty("cascaded")
    private Boolean cascaded;
    @JsonProperty("heat-stack-id")
    private String heatStackId;
    @JsonProperty("contrail-service-instance-fqdn")
    private String contrailServiceInstanceFqdn;
    @JsonProperty("module-index")
    private Integer moduleIndex;
    @JsonProperty("selflink")
    private String selflink;
    @JsonProperty("vnfcs")
    private List<Vnfc> vnfcs = new ArrayList<>();
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

    public String getContrailServiceInstanceFqdn() {
        return contrailServiceInstanceFqdn;
    }

    public void setContrailServiceInstanceFqdn(String contrailServiceInstanceFqdn) {
        this.contrailServiceInstanceFqdn = contrailServiceInstanceFqdn;
    }

    public Integer getModuleIndex() {
        return moduleIndex;
    }

    public void setModuleIndex(Integer moduleIndex) {
        this.moduleIndex = moduleIndex;
    }

    public String getSelflink() {
        return selflink;
    }

    public void setSelflink(String selflink) {
        this.selflink = selflink;
    }

    public String getVfModuleId() {
        return vfModuleId;
    }

    public void setVfModuleId(String vfModuleId) {
        this.vfModuleId = vfModuleId;
    }

    public String getVfModuleName() {
        return vfModuleName;
    }

    public void setVfModuleName(String vfModuleName) {
        this.vfModuleName = vfModuleName;
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

    public List<Vnfc> getVnfcs() {
        return vnfcs;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof VfModule)) {
            return false;
        }
        VfModule castOther = (VfModule) other;
        return new EqualsBuilder().append(vfModuleId, castOther.vfModuleId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(vfModuleId).toHashCode();
    }
}
