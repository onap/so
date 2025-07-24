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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"configName", "riskLevel", "policyName", "policyScope", "guard", "description", "priority", "uuid",
        "version", "content", "riskType", "service", "location", "templateVersion"})
@JsonRootName(value = "config")
public class Config {

    @JsonProperty("configName")
    private String configName;
    @JsonProperty("riskLevel")
    private String riskLevel;
    @JsonProperty("policyName")
    private String policyName;
    @JsonProperty("policyScope")
    private String policyScope;
    @JsonProperty("guard")
    private String guard;
    @JsonProperty("description")
    private String description;
    @JsonProperty("priority")
    private String priority;
    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("version")
    private String version;
    @JsonProperty("content")
    private Content content;
    @JsonProperty("riskType")
    private String riskType;
    @JsonProperty("service")
    private String service;
    @JsonProperty("location")
    private String location;
    @JsonProperty("templateVersion")
    private String templateVersion;

    @JsonProperty("configName")
    public String getConfigName() {
        return configName;
    }

    @JsonProperty("configName")
    public void setConfigName(String configName) {
        this.configName = configName;
    }

    @JsonProperty("riskLevel")
    public String getRiskLevel() {
        return riskLevel;
    }

    @JsonProperty("riskLevel")
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    @JsonProperty("policyName")
    public String getPolicyName() {
        return policyName;
    }

    @JsonProperty("policyName")
    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    @JsonProperty("policyScope")
    public String getPolicyScope() {
        return policyScope;
    }

    @JsonProperty("policyScope")
    public void setPolicyScope(String policyScope) {
        this.policyScope = policyScope;
    }

    @JsonProperty("guard")
    public String getGuard() {
        return guard;
    }

    @JsonProperty("guard")
    public void setGuard(String guard) {
        this.guard = guard;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("priority")
    public String getPriority() {
        return priority;
    }

    @JsonProperty("priority")
    public void setPriority(String priority) {
        this.priority = priority;
    }

    @JsonProperty("uuid")
    public String getUuid() {
        return uuid;
    }

    @JsonProperty("uuid")
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("content")
    public Content getContent() {
        return content;
    }

    @JsonProperty("content")
    public void setContent(Content content) {
        this.content = content;
    }

    @JsonProperty("riskType")
    public String getRiskType() {
        return riskType;
    }

    @JsonProperty("riskType")
    public void setRiskType(String riskType) {
        this.riskType = riskType;
    }

    @JsonProperty("service")
    public String getService() {
        return service;
    }

    @JsonProperty("service")
    public void setService(String service) {
        this.service = service;
    }

    @JsonProperty("location")
    public String getLocation() {
        return location;
    }

    @JsonProperty("location")
    public void setLocation(String location) {
        this.location = location;
    }

    @JsonProperty("templateVersion")
    public String getTemplateVersion() {
        return templateVersion;
    }

    @JsonProperty("templateVersion")
    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
    }

}
