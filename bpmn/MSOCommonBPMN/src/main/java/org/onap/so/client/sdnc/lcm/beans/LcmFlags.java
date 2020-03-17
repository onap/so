/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.client.sdnc.lcm.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"mode", "force", "ttl"})
public class LcmFlags {

    @JsonProperty("mode")
    private String mode;

    @JsonProperty("force")
    private String force;

    @JsonProperty("ttl")
    private Integer ttl;

    public String getMode() {
        return mode;
    }

    public void setMode(String value) {
        this.mode = value;
    }

    public String getForce() {
        return force;
    }

    public void setForce(String value) {
        this.force = value;
    }

    public Integer getTtl() {
        return ttl;
    }

    public void setTtl(Integer value) {
        this.ttl = value;
    }

}
