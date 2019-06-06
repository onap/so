/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.sniro.beans;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Demand implements Serializable {

    private static final long serialVersionUID = 5676094538091859816L;

    @JsonProperty("serviceResourceId")
    private String serviceResourceId;
    @JsonProperty("resourceModuleName")
    private String resourceModuleName;
    @JsonProperty("resourceModelInfo")
    private ModelInfo modelInfo;
    @JsonProperty("requiredCandidates")
    private List<Candidate> requiredCandidates;
    @JsonProperty("excludedCandidates")
    private List<Candidate> excludedCandidates;
    @JsonProperty("existingCandidates")
    private List<Candidate> existingCandidates;
    @JsonProperty("filteringAttributes")
    private List<Candidate> filteringAttributes;


    public List<Candidate> getRequiredCandidates() {
        return requiredCandidates;
    }

    public void setRequiredCandidates(List<Candidate> requiredCandidates) {
        this.requiredCandidates = requiredCandidates;
    }

    public List<Candidate> getExcludedCandidates() {
        return excludedCandidates;
    }

    public void setExcludedCandidates(List<Candidate> excludedCandidates) {
        this.excludedCandidates = excludedCandidates;
    }

    public String getServiceResourceId() {
        return serviceResourceId;
    }

    public void setServiceResourceId(String serviceResourceId) {
        this.serviceResourceId = serviceResourceId;
    }

    public String getResourceModuleName() {
        return resourceModuleName;
    }

    public void setResourceModuleName(String resourceModuleName) {
        this.resourceModuleName = resourceModuleName;
    }

    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    public void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

    public List<Candidate> getExistingCandidates() {
        return existingCandidates;
    }

    public void setExistingCandidates(List<Candidate> existingCandidates) {
        this.existingCandidates = existingCandidates;
    }

    public List<Candidate> getFilteringAttributes() {
        return filteringAttributes;
    }

    public void setFilteringAttributes(List<Candidate> filteringAttributes) {
        this.filteringAttributes = filteringAttributes;
    }

}
