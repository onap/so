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

package org.onap.so.client.sdno.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"request-client-name", "request-node-name", "request-node-uuid", "request-node-ip", "request-id",
        "request-user-id", "request-node-type", "health-diagnostic-code"})
public class RequestHealthDiagnostic implements Serializable {

    @JsonProperty("request-client-name")
    private String requestClientName;
    @JsonProperty("request-node-name")
    private String requestNodeName;
    @JsonProperty("request-node-uuid")
    private String requestNodeUuid;
    @JsonProperty("request-node-ip")
    private String requestNodeIp;
    @JsonProperty("request-id")
    private String requestId;
    @JsonProperty("request-user-id")
    private String requestUserId;
    @JsonProperty("request-node-type")
    private String requestNodeType;
    @JsonProperty("health-diagnostic-code")
    private String healthDiagnosticCode;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private static final long serialVersionUID = 1166788526178388021L;

    @JsonProperty("request-client-name")
    public String getRequestClientName() {
        return requestClientName;
    }

    @JsonProperty("request-client-name")
    public void setRequestClientName(String requestClientName) {
        this.requestClientName = requestClientName;
    }

    public RequestHealthDiagnostic withRequestClientName(String requestClientName) {
        this.requestClientName = requestClientName;
        return this;
    }

    @JsonProperty("request-node-name")
    public String getRequestNodeName() {
        return requestNodeName;
    }

    @JsonProperty("request-node-name")
    public void setRequestNodeName(String requestNodeName) {
        this.requestNodeName = requestNodeName;
    }

    public RequestHealthDiagnostic withRequestNodeName(String requestNodeName) {
        this.requestNodeName = requestNodeName;
        return this;
    }

    @JsonProperty("request-node-uuid")
    public String getRequestNodeUuid() {
        return requestNodeUuid;
    }

    @JsonProperty("request-node-uuid")
    public void setRequestNodeUuid(String requestNodeUuid) {
        this.requestNodeUuid = requestNodeUuid;
    }

    public RequestHealthDiagnostic withRequestNodeUuid(String requestNodeUuid) {
        this.requestNodeUuid = requestNodeUuid;
        return this;
    }

    @JsonProperty("request-node-ip")
    public String getRequestNodeIp() {
        return requestNodeIp;
    }

    @JsonProperty("request-node-ip")
    public void setRequestNodeIp(String requestNodeIp) {
        this.requestNodeIp = requestNodeIp;
    }

    public RequestHealthDiagnostic withRequestNodeIp(String requestNodeIp) {
        this.requestNodeIp = requestNodeIp;
        return this;
    }

    @JsonProperty("request-id")
    public String getRequestId() {
        return requestId;
    }

    @JsonProperty("request-id")
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public RequestHealthDiagnostic withRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    @JsonProperty("request-user-id")
    public String getRequestUserId() {
        return requestUserId;
    }

    @JsonProperty("request-user-id")
    public void setRequestUserId(String requestUserId) {
        this.requestUserId = requestUserId;
    }

    public RequestHealthDiagnostic withRequestUserId(String requestUserId) {
        this.requestUserId = requestUserId;
        return this;
    }

    @JsonProperty("request-node-type")
    public String getRequestNodeType() {
        return requestNodeType;
    }

    @JsonProperty("request-node-type")
    public void setRequestNodeType(String requestNodeType) {
        this.requestNodeType = requestNodeType;
    }

    public RequestHealthDiagnostic withRequestNodeType(String requestNodeType) {
        this.requestNodeType = requestNodeType;
        return this;
    }

    @JsonProperty("health-diagnostic-code")
    public String getHealthDiagnosticCode() {
        return healthDiagnosticCode;
    }

    @JsonProperty("health-diagnostic-code")
    public void setHealthDiagnosticCode(String healthDiagnosticCode) {
        this.healthDiagnosticCode = healthDiagnosticCode;
    }

    public RequestHealthDiagnostic withHealthDiagnosticCode(String healthDiagnosticCode) {
        this.healthDiagnosticCode = healthDiagnosticCode;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public void setAdditionalProperties(Map<String, Object> map) {
        this.additionalProperties = map;
    }

    public RequestHealthDiagnostic withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
