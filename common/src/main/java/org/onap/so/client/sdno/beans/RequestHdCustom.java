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
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"request-client-name", "request-user-id", "request-id", "health-diagnostic-code", "operation-type",
        "send-detailed-cmd-response", "aai-param-list"})
public class RequestHdCustom implements Serializable {

    /**
    	 * 
    	 */
    private static final long serialVersionUID = -206110458275127710L;
    @JsonProperty("request-client-name")
    private String requestClientName;
    @JsonProperty("request-user-id")
    private String requestUserId;
    @JsonProperty("request-id")
    private String requestId;
    @JsonProperty("health-diagnostic-code")
    private String healthDiagnosticCode;
    @JsonProperty("operation-type")
    private String operationType;
    @JsonProperty("send-detailed-cmd-response")
    private String sendDetailedCmdResponse = "false";
    @JsonProperty("aai-param-list")
    private List<AAIParamList> aaiParamList = new ArrayList<>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public RequestHdCustom() {}

    /**
     * 
     * @param requestClientName
     * @param operationType
     * @param requestId
     * @param healthDiagnosticCode
     * @param aaiParamList
     * @param requestUserId
     */
    public RequestHdCustom(String requestClientName, String requestUserId, String requestId,
            String healthDiagnosticCode, String operationType, List<AAIParamList> aaiParamList) {
        super();
        this.requestClientName = requestClientName;
        this.requestUserId = requestUserId;
        this.requestId = requestId;
        this.healthDiagnosticCode = healthDiagnosticCode;
        this.operationType = operationType;
        this.aaiParamList = aaiParamList;
    }

    @JsonProperty("request-client-name")
    public String getRequestClientName() {
        return requestClientName;
    }

    @JsonProperty("request-client-name")
    public void setRequestClientName(String requestClientName) {
        this.requestClientName = requestClientName;
    }

    public RequestHdCustom withRequestClientName(String requestClientName) {
        this.requestClientName = requestClientName;
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

    public RequestHdCustom withRequestUserId(String requestUserId) {
        this.requestUserId = requestUserId;
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

    public RequestHdCustom withRequestId(String requestId) {
        this.requestId = requestId;
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

    public RequestHdCustom withHealthDiagnosticCode(String healthDiagnosticCode) {
        this.healthDiagnosticCode = healthDiagnosticCode;
        return this;
    }

    @JsonProperty("operation-type")
    public String getOperationType() {
        return operationType;
    }

    @JsonProperty("operation-type")
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public RequestHdCustom withOperationType(String operationType) {
        this.operationType = operationType;
        return this;
    }

    public void setSendDetailedCmdResponse(String sendDetailedCmdResponse) {
        this.sendDetailedCmdResponse = sendDetailedCmdResponse;
    }

    public String getSendDetailedCmdResponse() {
        return sendDetailedCmdResponse;
    }

    public RequestHdCustom withSendDetailedCmdResponse(String sendDetailedCmdResponse) {
        this.sendDetailedCmdResponse = sendDetailedCmdResponse;
        return this;
    }

    @JsonProperty("aai-param-list")
    public List<AAIParamList> getAaiParamList() {
        return aaiParamList;
    }

    @JsonProperty("aai-param-list")
    public void setAaiParamList(List<AAIParamList> aaiParamList) {
        this.aaiParamList = aaiParamList;
    }

    public RequestHdCustom withAaiParamList(List<AAIParamList> aaiParamList) {
        this.aaiParamList = aaiParamList;
        return this;
    }

}
