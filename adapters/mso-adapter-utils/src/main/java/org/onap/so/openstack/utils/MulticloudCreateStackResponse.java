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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"stack"})
public class MulticloudCreateStackResponse implements Serializable {
    private static final long serialVersionUID = -5215028275577848311L;

    @JsonProperty("stack")
    private MulticloudCreateHeatResponse stack;

    @JsonCreator
    public MulticloudCreateStackResponse(@JsonProperty("stack") MulticloudCreateHeatResponse stack) {
        this.stack = stack;
    }

    @JsonProperty("stack")
    public MulticloudCreateHeatResponse getStack() {
        return stack;
    }

    @JsonProperty("stack")
    public void setStack(MulticloudCreateHeatResponse stack) {
        this.stack = stack;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("stack", stack).toString();
    }
}
