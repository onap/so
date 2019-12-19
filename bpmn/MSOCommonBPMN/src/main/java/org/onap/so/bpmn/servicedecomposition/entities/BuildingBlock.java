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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonRootName("buildingBlock")
@JsonDeserialize(builder = BuildingBlock.Builder.class)
public final class BuildingBlock implements Serializable {

    private final String msoId;
    private final String bpmnFlowName;
    private final String key;
    private final Boolean isVirtualLink;
    private final String virtualLinkKey;

    private BuildingBlock(Builder builder) {
        this.msoId = builder.msoId;
        this.bpmnFlowName = builder.bpmnFlowName;
        this.key = builder.key;
        this.isVirtualLink = builder.isVirtualLink;
        this.virtualLinkKey = builder.virtualLinkKey;
    }

    public String getBpmnFlowName() {
        return bpmnFlowName;
    }

    public String getMsoId() {
        return msoId;
    }

    public String getKey() {
        return key;
    }

    public Boolean getIsVirtualLink() {
        return isVirtualLink;
    }

    public String getVirtualLinkKey() {
        return virtualLinkKey;
    }

    public BuildingBlock copyAndChangeBuildingBlock(String bpmnFlowName) {
        return new BuildingBlock.Builder().withBpmnFlowName(bpmnFlowName).withMsoId(msoId).withKey(key)
                .withIsVirtualLink(isVirtualLink).withVirtualLinkKey(virtualLinkKey).build();
    }

    @JsonPOJOBuilder
    public static class Builder {
        private String msoId;
        private String bpmnFlowName;
        private String key;
        private Boolean isVirtualLink;
        private String virtualLinkKey;

        @JsonProperty("mso-id")
        public Builder withMsoId(String msoId) {
            this.msoId = msoId;
            return this;
        }

        @JsonProperty("bpmn-flow-name")
        public Builder withBpmnFlowName(String bpmnFlowName) {
            this.bpmnFlowName = bpmnFlowName;
            return this;
        }

        @JsonProperty("key")
        public Builder withKey(String key) {
            this.key = key;
            return this;
        }

        @JsonProperty("is-virtual-link")
        public Builder withIsVirtualLink(Boolean virtualLink) {
            this.isVirtualLink = virtualLink;
            return this;
        }

        @JsonProperty("virtual-link-key")
        public Builder withVirtualLinkKey(String virtualLinkKey) {
            this.virtualLinkKey = virtualLinkKey;
            return this;
        }

        public BuildingBlock build() {
            return new BuildingBlock(this);
        }

    }
}
