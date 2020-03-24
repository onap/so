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

package org.onap.so.bpmn.servicedecomposition.modelinfo;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelInfoServiceInstance extends ModelInfoMetadata implements Serializable {

    private static final long serialVersionUID = -1812787934683419141L;

    @JsonProperty("description")
    private String description;
    @JsonProperty("created")
    private String created;
    @JsonProperty("service-type")
    private String serviceType;
    @JsonProperty("service-role")
    private String serviceRole;
    @JsonProperty("service-function")
    private String serviceFunction;
    @JsonProperty("environment-context")
    private String environmentContext;
    @JsonProperty("workload-context")
    private String workloadContext;
    @JsonProperty("naming-policy")
    private String namingPolicy;
    @JsonProperty("onap-generated-naming")
    private Boolean onapGeneratedNaming;


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceRole() {
        return serviceRole;
    }

    public void setServiceRole(String serviceRole) {
        this.serviceRole = serviceRole;
    }

    public String getServiceFunction() {
        return serviceFunction;
    }

    public void setServiceFunction(String serviceFunction) {
        this.serviceFunction = serviceFunction;
    }

    public String getEnvironmentContext() {
        return environmentContext;
    }

    public void setEnvironmentContext(String environmentContext) {
        this.environmentContext = environmentContext;
    }

    public String getWorkloadContext() {
        return workloadContext;
    }

    public void setWorkloadContext(String workloadContext) {
        this.workloadContext = workloadContext;
    }


    public String getNamingPolicy() {
        return namingPolicy;
    }

    public void setNamingPolicy(String namingPolicy) {
        this.namingPolicy = namingPolicy;
    }

    public Boolean getOnapGeneratedNaming() {
        return onapGeneratedNaming;
    }

    public void setOnapGeneratedNaming(Boolean onapGeneratedNaming) {
        this.onapGeneratedNaming = onapGeneratedNaming;
    }
}
