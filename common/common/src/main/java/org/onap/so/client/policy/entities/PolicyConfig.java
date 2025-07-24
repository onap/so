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
@JsonPropertyOrder({"policyConfigMessage", "policyConfigStatus", "type", "config", "policyName", "policyType",
        "policyVersion", "matchingConditions", "responseAttributes", "property"})
public class PolicyConfig {

    @JsonProperty("policyConfigMessage")
    private String policyConfigMessage;
    @JsonProperty("policyConfigStatus")
    private String policyConfigStatus;
    @JsonProperty("type")
    private String type;
    @JsonProperty("config")
    private String config;
    @JsonProperty("policyName")
    private String policyName;
    @JsonProperty("policyType")
    private String policyType;
    @JsonProperty("policyVersion")
    private String policyVersion;
    @JsonProperty("matchingConditions")
    private Map<String, String> matchingConditions = new HashMap<>();
    @JsonProperty("responseAttributes")
    private Map<String, String> responseAttributes = new HashMap<>();
    @JsonProperty("property")
    private Object property;

    @JsonProperty("policyConfigMessage")
    public String getPolicyConfigMessage() {
        return policyConfigMessage;
    }

    @JsonProperty("policyConfigMessage")
    public void setPolicyConfigMessage(String policyConfigMessage) {
        this.policyConfigMessage = policyConfigMessage;
    }

    @JsonProperty("policyConfigStatus")
    public String getPolicyConfigStatus() {
        return policyConfigStatus;
    }

    @JsonProperty("policyConfigStatus")
    public void setPolicyConfigStatus(String policyConfigStatus) {
        this.policyConfigStatus = policyConfigStatus;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("config")
    public String getConfig() {
        return config;
    }

    @JsonProperty("config")
    public void setConfig(String config) {
        this.config = config;
    }

    @JsonProperty("policyName")
    public String getPolicyName() {
        return policyName;
    }

    @JsonProperty("policyName")
    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    @JsonProperty("policyType")
    public String getPolicyType() {
        return policyType;
    }

    @JsonProperty("policyType")
    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    @JsonProperty("policyVersion")
    public String getPolicyVersion() {
        return policyVersion;
    }

    @JsonProperty("policyVersion")
    public void setPolicyVersion(String policyVersion) {
        this.policyVersion = policyVersion;
    }

    @JsonProperty("matchingConditions")
    public Map<String, String> getMatchingConditions() {
        return matchingConditions;
    }

    @JsonProperty("matchingConditions")
    public void setMatchingConditions(Map<String, String> matchingConditions) {
        this.matchingConditions = matchingConditions;
    }

    @JsonProperty("responseAttributes")
    public Map<String, String> getResponseAttributes() {
        return responseAttributes;
    }

    @JsonProperty("responseAttributes")
    public void setResponseAttributes(Map<String, String> responseAttributes) {
        this.responseAttributes = responseAttributes;
    }

    @JsonProperty("property")
    public Object getProperty() {
        return property;
    }

    @JsonProperty("property")
    public void setProperty(Object property) {
        this.property = property;
    }

}
