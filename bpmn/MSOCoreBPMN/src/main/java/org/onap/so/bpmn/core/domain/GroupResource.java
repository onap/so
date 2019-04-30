/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Huawei Intellectual Property. All rights reserved.
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupResource extends Resource {
    private static final long serialVersionUID = 1L;

    @JsonProperty("vnfcs")
    private List<VnfcResource> vnfcs;

    public GroupResource() {
        resourceType = ResourceType.GROUP;
        setResourceId(UUID.randomUUID().toString());
    }

    public List<VnfcResource> getVnfcs() {
        return vnfcs;
    }

    public void setVnfcs(List<VnfcResource> vnfcs) {
        this.vnfcs = vnfcs;
    }
}
