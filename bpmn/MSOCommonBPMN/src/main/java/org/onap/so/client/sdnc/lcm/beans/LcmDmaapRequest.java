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
@JsonPropertyOrder({"version", "type", "cambriaPartition", "correlationId", "rpcName", "body"})
public class LcmDmaapRequest {

    @JsonProperty(value = "version", required = true)
    private String version;

    @JsonProperty(value = "type", required = true)
    private String type;

    @JsonProperty(value = "cambria.partition", required = true)
    private String cambriaPartition;

    @JsonProperty(value = "correlation-id", required = true)
    private String correlationId;

    @JsonProperty(value = "rpc-name", required = true)
    private String rpcName;

    @JsonProperty(value = "body")
    private LcmRestRequest body;

    public String getVersion() {
        return version;
    }

    public void setVersion(String value) {
        this.version = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public String getCambriaPartition() {
        return cambriaPartition;
    }

    public void setCambriaPartition(String value) {
        this.cambriaPartition = value;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String value) {
        this.correlationId = value;
    }

    public String getRpcName() {
        return rpcName;
    }

    public void setRpcName(String value) {
        this.rpcName = value;
    }

    public LcmRestRequest getBody() {
        return body;
    }

    public void setBody(LcmRestRequest value) {
        this.body = value;
    }

}
