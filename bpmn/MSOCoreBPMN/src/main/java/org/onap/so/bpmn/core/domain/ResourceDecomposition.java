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

package org.onap.so.bpmn.core.domain;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Abstract superclass for all individual decomposition resources
 * 
 */
// @JsonIgnoreProperties
public abstract class ResourceDecomposition extends JsonWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String resourceType; // Enum of vnf or network or allotted resource
    private ModelInfo modelInfo;

    // private List modules;
    private ResourceInstance instanceData = new ResourceInstance();

    // GET and SET
    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    public void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

    public ResourceInstance getInstanceData() {
        return instanceData;
    }

    public void setInstanceData(ResourceInstance instanceData) {
        this.instanceData = instanceData;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    // Utility methods
    @JsonIgnore
    public ModelInfo getResourceModel() {
        return modelInfo;
    }

    @JsonIgnore
    public String getResourceInstanceId() {
        return this.getInstanceData().getInstanceId();
    }

    @JsonIgnore
    public String getResourceInstanceName() {
        return this.getInstanceData().getInstanceName();
    }
    // @JsonIgnore
    // public String getResourceHomingSolution() {
    // }

    public void setResourceInstanceId(String newInstanceId) {
        this.getInstanceData().setInstanceId(newInstanceId);
    }

    public void setResourceInstanceName(String newInstanceName) {
        this.getInstanceData().setInstanceName(newInstanceName);
    }
    // @JsonIgnore
    // public String setResourceHomingSolution() {
    // }
}
