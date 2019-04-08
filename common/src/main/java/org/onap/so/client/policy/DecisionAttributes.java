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

package org.onap.so.client.policy;



import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"ServiceType", "VNFType", "BB_ID", "WorkStep", "ErrorCode"})
public class DecisionAttributes {

    @JsonProperty("ServiceType")
    private String serviceType;
    @JsonProperty("VNFType")
    private String vNFType;
    @JsonProperty("BB_ID")
    private String bbID;
    @JsonProperty("WorkStep")
    private String workStep;
    @JsonProperty("ErrorCode")
    private String errorCode;

    @JsonProperty("ServiceType")
    public String getServiceType() {
        return serviceType;
    }

    @JsonProperty("ServiceType")
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    @JsonProperty("VNFType")
    public String getVNFType() {
        return vNFType;
    }

    @JsonProperty("VNFType")
    public void setVNFType(String vNFType) {
        this.vNFType = vNFType;
    }

    @JsonProperty("BB_ID")
    public String getBBID() {
        return bbID;
    }

    @JsonProperty("BB_ID")
    public void setBBID(String bBID) {
        this.bbID = bBID;
    }

    @JsonProperty("WorkStep")
    public String getWorkStep() {
        return workStep;
    }

    @JsonProperty("WorkStep")
    public void setWorkStep(String workStep) {
        this.workStep = workStep;
    }

    @JsonProperty("ErrorCode")
    public String getErrorCode() {
        return errorCode;
    }

    @JsonProperty("ErrorCode")
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
