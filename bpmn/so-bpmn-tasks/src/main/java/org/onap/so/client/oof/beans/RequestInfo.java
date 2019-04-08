/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Intel Corp.  All rights reserved.
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

package org.onap.so.client.oof.beans;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"transactionId", "requestId", "callbackUrl", "sourceId", "requestType", "numSolutions",
        "optimizers", "timeout"})
@JsonRootName("requestInfo")
public class RequestInfo implements Serializable {

    private static final long serialVersionUID = -759180997599143791L;

    @JsonProperty("transactionId")
    private String transactionId;
    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("callbackUrl")
    private String callbackUrl;
    @JsonProperty("sourceId")
    private String sourceId;
    @JsonProperty("requestType")
    private String requestType;
    @JsonProperty("numSolutions")
    private Integer numSolutions;
    @JsonProperty("optimizers")
    private List<String> optimizers = null;
    @JsonProperty("timeout")
    private Long timeout;

    @JsonProperty("transactionId")
    public String getTransactionId() {
        return transactionId;
    }

    @JsonProperty("transactionId")
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @JsonProperty("requestId")
    public String getRequestId() {
        return requestId;
    }

    @JsonProperty("requestId")
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @JsonProperty("callbackUrl")
    public String getCallbackUrl() {
        return callbackUrl;
    }

    @JsonProperty("callbackUrl")
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    @JsonProperty("sourceId")
    public String getSourceId() {
        return sourceId;
    }

    @JsonProperty("sourceId")
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    @JsonProperty("requestType")
    public String getRequestType() {
        return requestType;
    }

    @JsonProperty("requestType")
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    @JsonProperty("numSolutions")
    public Integer getNumSolutions() {
        return numSolutions;
    }

    @JsonProperty("numSolutions")
    public void setNumSolutions(Integer numSolutions) {
        this.numSolutions = numSolutions;
    }

    @JsonProperty("optimizers")
    public List<String> getOptimizers() {
        return optimizers;
    }

    @JsonProperty("optimizers")
    public void setOptimizers(List<String> optimizers) {
        this.optimizers = optimizers;
    }

    @JsonProperty("timeout")
    public Long getTimeout() {
        return timeout;
    }

    @JsonProperty("timeout")
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

}
