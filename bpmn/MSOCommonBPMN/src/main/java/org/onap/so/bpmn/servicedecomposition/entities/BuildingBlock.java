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

    private static final long serialVersionUID = -1144315411128866052L;

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

    public String getBpmnFlowName() {
        return bpmnFlowName;
    }

    public void setBpmnFlowName(String bpmnFlowName) {
        this.bpmnFlowName = bpmnFlowName;
    }

    public String getMsoId() {
        return msoId;
    }

    public void setMsoId(String msoId) {
        this.msoId = msoId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Boolean getIsVirtualLink() {
        return isVirtualLink;
    }

    public void setIsVirtualLink(Boolean isVirtualLink) {
        this.isVirtualLink = isVirtualLink;
    }

    public String getVirtualLinkKey() {
        return virtualLinkKey;
    }

    public void setVirtualLinkKey(String virtualLinkKey) {
        this.virtualLinkKey = virtualLinkKey;
    }
}
