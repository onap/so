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
import jakarta.persistence.Id;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;
import org.onap.so.bpmn.servicedecomposition.homingobjects.SolutionCandidates;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoAllottedResource;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

@JsonRootName("allotted-resource")
public class AllottedResource extends SolutionCandidates implements Serializable, ShallowCopy<AllottedResource> {

    private static final long serialVersionUID = 8674239064804424306L;

    @Id
    @JsonProperty("id")
    private String id;
    @JsonProperty("target-network-role")
    private String targetNetworkRole;
    @JsonProperty("self-link")
    private String selflink;
    @JsonProperty("orchestration-status")
    private OrchestrationStatus orchestrationStatus;
    @JsonProperty("operational-status")
    private String operationalStatus;
    @JsonProperty("type")
    private String type;
    @JsonProperty("role")
    private String role;
    @JsonProperty("model-info-allotted-resource")
    private ModelInfoAllottedResource modelInfoAllottedResource;
    @JsonProperty("service-instance")
    private ServiceInstance parentServiceInstance;

    public ModelInfoAllottedResource getModelInfoAllottedResource() {
        return modelInfoAllottedResource;
    }

    public void setModelInfoAllottedResource(ModelInfoAllottedResource modelInfoAllottedResource) {
        this.modelInfoAllottedResource = modelInfoAllottedResource;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTargetNetworkRole() {
        return targetNetworkRole;
    }

    public void setTargetNetworkRole(String targetNetworkRole) {
        this.targetNetworkRole = targetNetworkRole;
    }

    public String getSelflink() {
        return selflink;
    }

    public void setSelflink(String selflink) {
        this.selflink = selflink;
    }

    public String getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(String operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    public OrchestrationStatus getOrchestrationStatus() {
        return orchestrationStatus;
    }

    public void setOrchestrationStatus(OrchestrationStatus orchestrationStatus) {
        this.orchestrationStatus = orchestrationStatus;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public ServiceInstance getParentServiceInstance() {
        return parentServiceInstance;
    }

    public void setParentServiceInstance(ServiceInstance parentServiceInstance) {
        this.parentServiceInstance = parentServiceInstance;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof AllottedResource)) {
            return false;
        }
        AllottedResource castOther = (AllottedResource) other;
        return new EqualsBuilder().append(id, castOther.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }

}
