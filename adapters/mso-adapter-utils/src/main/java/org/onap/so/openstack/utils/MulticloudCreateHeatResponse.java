/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Intel Corp. All rights reserved.
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

package org.onap.so.openstack.utils;

import java.io.Serializable;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "links"})
public class MulticloudCreateHeatResponse implements Serializable {
    private static final long serialVersionUID = -5215028275577848311L;

    @JsonProperty("id")
    private String id;
    @JsonProperty("links")
    private List<MulticloudCreateLinkResponse> links;

    @JsonCreator
    public MulticloudCreateHeatResponse(@JsonProperty("id") String id,
            @JsonProperty("links") List<MulticloudCreateLinkResponse> links) {
        this.id = id;
        this.links = links;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("links")
    public List<MulticloudCreateLinkResponse> getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(List<MulticloudCreateLinkResponse> links) {
        this.links = links;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("links", links).toString();
    }
}
