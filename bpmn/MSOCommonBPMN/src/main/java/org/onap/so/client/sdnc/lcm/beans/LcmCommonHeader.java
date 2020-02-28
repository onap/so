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
@JsonPropertyOrder({"apiVer", "flags", "originatorId", "requestId", "subRequestId", "timestamp"})
public class LcmCommonHeader {

    @JsonProperty(value = "api-ver", required = true)
    private String apiVer;

    @JsonProperty(value = "flags")
    private LcmFlags flags;

    @JsonProperty(value = "originator-id", required = true)
    private String originatorId;

    @JsonProperty(value = "request-id", required = true)
    private String requestId;

    @JsonProperty(value = "sub-request-id")
    private String subRequestId;

    @JsonProperty(value = "timestamp", required = true)
    private String timestamp;

    public String getApiVer() {
        return apiVer;
    }

    public void setApiVer(String value) {
        this.apiVer = value;
    }

    public String getOriginatorId() {
        return originatorId;
    }

    public void setOriginatorId(String value) {
        this.originatorId = value;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String value) {
        this.requestId = value;
    }

    public String getSubRequestId() {
        return subRequestId;
    }

    public void setSubRequestId(String value) {
        this.subRequestId = value;
    }

    public LcmFlags getFlags() {
        return flags;
    }

    public void setFlags(LcmFlags value) {
        this.flags = value;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String value) {
        this.timestamp = value;
    }

}
