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

public class ModelInfoInstanceGroup implements Serializable {

    private static final long serialVersionUID = -8279040393230356226L;

    public static final String TYPE_L3_NETWORK = "L3-NETWORK";
    public static final String TYPE_VNFC = "VNFC";

    @JsonProperty("model-uuid")
    private String ModelUUID;
    @JsonProperty("model-invariant-uuid")
    private String ModelInvariantUUID;
    @JsonProperty("type")
    private String type;
    @JsonProperty("instance-group-role")
    private String instanceGroupRole;
    @JsonProperty("function")
    private String function;
    @JsonProperty("description")
    private String description;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInstanceGroupRole() {
        return instanceGroupRole;
    }

    public void setInstanceGroupRole(String instanceGroupRole) {
        this.instanceGroupRole = instanceGroupRole;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
