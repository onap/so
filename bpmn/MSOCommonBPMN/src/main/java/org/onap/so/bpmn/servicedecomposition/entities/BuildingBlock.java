/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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
import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonRootName("buildingBlock")
@JsonDeserialize(builder = BuildingBlock.Builder.class)
public final class BuildingBlock implements Serializable {

    private static final long serialVersionUID = -1144315411128866053L;
    private final String msoId;
    private final String bpmnFlowName;
    private final String key;
    private final Boolean isVirtualLink;
    private final String virtualLinkKey;
    private final String bpmnScope;
    private final String bpmnAction;

    private BuildingBlock(Builder builder) {
        this.msoId = builder.msoId;
        this.bpmnFlowName = builder.bpmnFlowName;
        this.key = builder.key;
        this.isVirtualLink = builder.isVirtualLink;
        this.virtualLinkKey = builder.virtualLinkKey;
        this.bpmnScope = builder.bpmnScope;
        this.bpmnAction = builder.bpmnAction;
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

    public Boolean isVirtualLink() {
        return isVirtualLink;
    }

    public String getVirtualLinkKey() {
        return virtualLinkKey;
    }

    public String getBpmnScope() {
        return bpmnScope;
    }

    public String getBpmnAction() {
        return bpmnAction;
    }

    public BuildingBlock copyAndChangeBuildingBlock(Builder builder) {
        return new BuildingBlock.Builder()
                .withBpmnFlowName(Optional.ofNullable(builder.bpmnFlowName).orElse(this.bpmnFlowName))
                .withMsoId(Optional.ofNullable(builder.msoId).orElse(this.msoId))
                .withKey(Optional.ofNullable(builder.key).orElse(this.key))
                .withIsVirtualLink(Optional.ofNullable(builder.isVirtualLink).orElse(this.isVirtualLink))
                .withVirtualLinkKey(Optional.ofNullable(builder.virtualLinkKey).orElse(this.virtualLinkKey))
                .withBpmnScope(Optional.ofNullable(builder.bpmnScope).orElse(this.bpmnScope))
                .withBpmnAction(Optional.ofNullable(builder.bpmnAction).orElse(this.bpmnAction)).build();
    }

    public BuildingBlock copyAndChangeBuildingBlock(String bpmnFlowName) {
        return copyAndChangeBuildingBlock(new Builder().withBpmnFlowName(bpmnFlowName));
    }

    @JsonPOJOBuilder
    public static class Builder {
        private String msoId;
        private String bpmnFlowName;
        private String key;
        private Boolean isVirtualLink;
        private String virtualLinkKey;
        private String bpmnScope;
        private String bpmnAction;

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

        @JsonProperty("scope")
        public Builder withBpmnScope(String bpmnScope) {
            this.bpmnScope = bpmnScope;
            return this;
        }

        @JsonProperty("action")
        public Builder withBpmnAction(String bpmnAction) {
            this.bpmnAction = bpmnAction;
            return this;
        }

        public BuildingBlock build() {
            return new BuildingBlock(this);
        }
    }
}
