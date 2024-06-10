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
import java.util.List;
import jakarta.persistence.Id;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("instance-group")
public class InstanceGroup implements Serializable, ShallowCopy<InstanceGroup> {

    private static final long serialVersionUID = -2330859693128099141L;

    @Id
    @JsonProperty("id")
    private String id;
    @JsonProperty("description")
    private String description;
    @JsonProperty("resource-version")
    private String resourceVersion;
    @JsonProperty("instance-group-name")
    private String instanceGroupName;
    @JsonProperty("orchestration-status")
    private OrchestrationStatus orchestrationStatus = OrchestrationStatus.PRECREATED;
    @JsonProperty("model-info-instance-group")
    private ModelInfoInstanceGroup modelInfoInstanceGroup;
    @JsonProperty("instance-group-function")
    private String instanceGroupFunction;
    @JsonProperty("vnfs")
    private List<GenericVnf> vnfs = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    public String getInstanceGroupName() {
        return instanceGroupName;
    }

    public void setInstanceGroupName(String instanceGroupName) {
        this.instanceGroupName = instanceGroupName;
    }

    public OrchestrationStatus getOrchestrationStatus() {
        return orchestrationStatus;
    }

    public void setOrchestrationStatus(OrchestrationStatus orchestrationStatus) {
        this.orchestrationStatus = orchestrationStatus;
    }

    public ModelInfoInstanceGroup getModelInfoInstanceGroup() {
        return modelInfoInstanceGroup;
    }

    public void setModelInfoInstanceGroup(ModelInfoInstanceGroup modelInfoInstanceGroup) {
        this.modelInfoInstanceGroup = modelInfoInstanceGroup;
    }

    public String getInstanceGroupFunction() {
        return instanceGroupFunction;
    }

    public void setInstanceGroupFunction(String instanceGroupFunction) {
        this.instanceGroupFunction = instanceGroupFunction;
    }

    public List<GenericVnf> getVnfs() {
        return vnfs;
    }

    public void setVnfs(List<GenericVnf> vnfs) {
        this.vnfs = vnfs;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof InstanceGroup)) {
            return false;
        }
        InstanceGroup castOther = (InstanceGroup) other;
        return new EqualsBuilder().append(id, castOther.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }
}
