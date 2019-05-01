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

package org.onap.so.rest.catalog.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_DEFAULT)
public class Vnf implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2956199674955504834L;

    private String modelName;
    private String modelVersionId;
    private String modelInvariantId;
    private String modelVersion;
    private String modelCustomizationId;
    private String modelInstanceName;
    private Integer minInstances;
    private Integer maxInstances;
    private Integer availabilityZoneMaxCount;
    private String toscaNodeType;
    private String nfFunction;
    private String nfRole;
    private String nfNamingCode;
    private String multiStageDesign;
    private String orchestrationMode;
    private String cloudVersionMin;
    private String cloudVersionMax;
    private String category;
    private String subCategory;
    private List<VfModule> vfModule = new ArrayList<>();

    public List<VfModule> getVfModule() {
        return vfModule;
    }

    public void setVfModule(List<VfModule> vfModule) {
        this.vfModule = vfModule;
    }

    public Integer getAvailabilityZoneMaxCount() {
        return availabilityZoneMaxCount;
    }

    public void setAvailabilityZoneMaxCount(Integer availabilityZoneMaxCount) {
        this.availabilityZoneMaxCount = availabilityZoneMaxCount;
    }

    public Integer getMinInstances() {
        return minInstances;
    }

    public void setMinInstances(Integer minInstances) {
        this.minInstances = minInstances;
    }

    public Integer getMaxInstances() {
        return maxInstances;
    }

    public void setMaxInstances(Integer maxInstances) {
        this.maxInstances = maxInstances;
    }

    public String getCloudVersionMin() {
        return cloudVersionMin;
    }

    public void setCloudVersionMin(String cloudVersionMin) {
        this.cloudVersionMin = cloudVersionMin;
    }

    public String getCloudVersionMax() {
        return cloudVersionMax;
    }

    public void setCloudVersionMax(String cloudVersionMax) {
        this.cloudVersionMax = cloudVersionMax;
    }

    public String getModelVersionId() {
        return modelVersionId;
    }

    public void setModelVersionId(String modelVersionId) {
        this.modelVersionId = modelVersionId;
    }

    public String getModelInvariantId() {
        return modelInvariantId;
    }

    public void setModelInvariantId(String modelInvariantId) {
        this.modelInvariantId = modelInvariantId;
    }

    public String getModelCustomizationId() {
        return modelCustomizationId;
    }

    public void setModelCustomizationId(String modelCustomizationId) {
        this.modelCustomizationId = modelCustomizationId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getOrchestrationMode() {
        return orchestrationMode;
    }

    public void setOrchestrationMode(String orchestrationMode) {
        this.orchestrationMode = orchestrationMode;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }


    public String getModelInstanceName() {
        return modelInstanceName;
    }

    public void setModelInstanceName(String modelInstanceName) {
        this.modelInstanceName = modelInstanceName;
    }

    public String getToscaNodeType() {
        return toscaNodeType;
    }

    public void setToscaNodeType(String toscaNodeType) {
        this.toscaNodeType = toscaNodeType;
    }

    public String getNfFunction() {
        return nfFunction;
    }

    public void setNfFunction(String nfFunction) {
        this.nfFunction = nfFunction;
    }

    public String getNfRole() {
        return nfRole;
    }

    public void setNfRole(String nfRole) {
        this.nfRole = nfRole;
    }

    public String getNfNamingCode() {
        return nfNamingCode;
    }

    public void setNfNamingCode(String nfNamingCode) {
        this.nfNamingCode = nfNamingCode;
    }

    public String getMultiStageDesign() {
        return multiStageDesign;
    }

    public void setMultiStageDesign(String multiStepDesign) {
        this.multiStageDesign = multiStepDesign;
    }
}
