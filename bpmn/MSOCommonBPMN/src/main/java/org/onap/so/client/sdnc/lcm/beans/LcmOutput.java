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
@JsonPropertyOrder({"commonHeader", "status", "payload"})
public class LcmOutput {

    @JsonProperty(value = "common-header", required = true)
    private LcmCommonHeader commonHeader;

    @JsonProperty(value = "status", required = true)
    private LcmStatus status;

    @JsonProperty(value = "payload")
    private String payload;

    public LcmCommonHeader getCommonHeader() {
        return commonHeader;
    }

    public void setCommonHeader(LcmCommonHeader value) {
        this.commonHeader = value;
    }

    public LcmStatus getStatus() {
        return status;
    }

    public void setStatus(LcmStatus value) {
        this.status = value;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String value) {
        this.payload = value;
    }

}
