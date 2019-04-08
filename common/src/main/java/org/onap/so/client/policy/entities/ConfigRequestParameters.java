/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.policy.entities;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"configAttributes", "configName", "ecompName", "onapName", "policyName", "requestID", "unique"})
public class ConfigRequestParameters {

    @JsonProperty("configAttributes")
    private Map<String, String> configAttributes = new HashMap<>();
    @JsonProperty("configName")
    private String configName;
    @JsonProperty("ecompName")
    private String ecompName;
    @JsonProperty("onapName")
    private String onapName;
    @JsonProperty("policyName")
    private String policyName;
    @JsonProperty("requestID")
    private String requestID;
    @JsonProperty("unique")
    private Boolean unique;

    @JsonProperty("configAttributes")
    public Map<String, String> getConfigAttributes() {
        return configAttributes;
    }

    @JsonProperty("configAttributes")
    public void setConfigAttributes(Map<String, String> configAttributes) {
        this.configAttributes = configAttributes;
    }

    @JsonProperty("configName")
    public String getConfigName() {
        return configName;
    }

    @JsonProperty("configName")
    public void setConfigName(String configName) {
        this.configName = configName;
    }

    @JsonProperty("ecompName")
    public String getEcompName() {
        return ecompName;
    }

    @JsonProperty("ecompName")
    public void setEcompName(String ecompName) {
        this.ecompName = ecompName;
    }

    @JsonProperty("onapName")
    public String getOnapName() {
        return onapName;
    }

    @JsonProperty("onapName")
    public void setOnapName(String onapName) {
        this.onapName = onapName;
    }

    @JsonProperty("policyName")
    public String getPolicyName() {
        return policyName;
    }

    @JsonProperty("policyName")
    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    @JsonProperty("requestID")
    public String getRequestID() {
        return requestID;
    }

    @JsonProperty("requestID")
    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    @JsonProperty("unique")
    public Boolean getUnique() {
        return unique;
    }

    @JsonProperty("unique")
    public void setUnique(Boolean unique) {
        this.unique = unique;
    }

}
