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

package org.onap.so.bpmn.servicedecomposition.entities;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("buildingBlock")
public class BuildingBlock implements Serializable {

    private static final long serialVersionUID = -1144315411128866053L;

    @JsonProperty("mso-id")
    private String msoId;
    @JsonProperty("bpmn-flow-name")
    private String bpmnFlowName;
    @JsonProperty("key")
    private String key;
    @JsonProperty("is-virtual-link")
    private Boolean isVirtualLink;
    @JsonProperty("virtual-link-key")
    private String virtualLinkKey;
    @JsonProperty("scope")
    private String bpmnScope;
    @JsonProperty("action")
    private String bpmnAction;

    public String getBpmnFlowName() {
        return bpmnFlowName;
    }

    public BuildingBlock setBpmnFlowName(String bpmnFlowName) {
        this.bpmnFlowName = bpmnFlowName;
        return this;
    }

    public String getMsoId() {
        return msoId;
    }

    public BuildingBlock setMsoId(String msoId) {
        this.msoId = msoId;
        return this;
    }

    public String getKey() {
        return key;
    }

    public BuildingBlock setKey(String key) {
        this.key = key;
        return this;
    }

    @JsonProperty("is-virtual-link")
    public Boolean isVirtualLink() {
        return isVirtualLink;
    }

    @JsonProperty("is-virtual-link")
    public BuildingBlock setIsVirtualLink(Boolean isVirtualLink) {
        this.isVirtualLink = isVirtualLink;
        return this;
    }

    public String getVirtualLinkKey() {
        return virtualLinkKey;
    }

    public BuildingBlock setVirtualLinkKey(String virtualLinkKey) {
        this.virtualLinkKey = virtualLinkKey;
        return this;
    }

    public String getBpmnScope() {
        return bpmnScope;
    }

    public BuildingBlock setBpmnScope(String scope) {
        this.bpmnScope = scope;
        return this;
    }

    public String getBpmnAction() {
        return bpmnAction;
    }

    public BuildingBlock setBpmnAction(String action) {
        this.bpmnAction = action;
        return this;
    }
}
