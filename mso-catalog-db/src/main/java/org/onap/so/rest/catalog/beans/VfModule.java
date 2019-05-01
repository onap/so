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

import java.util.Date;
import java.util.List;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_DEFAULT)
public class VfModule {

    // VfModule
    private String modelVersionId;
    private String modelInvariantId;
    private String modelName;
    private String modelVersion;
    private String description;
    private Boolean isBase;
    private HeatTemplate heatTemplate;
    private Date created;
    private List<HeatFile> heatFile;

    // Customization
    private String modelCustomizationId;
    private String label;
    private Integer minInstances;
    private Integer maxInstances;
    private Integer initialCount;
    private Integer availabilityZoneCount;
    private HeatEnvironment heatEnv;
    private Boolean isVolumeGroup;


    // Add in cvnfcCustomization



    public String getModelName() {
        return modelName;
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

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsBase() {
        return isBase;
    }

    public void setIsBase(Boolean isBase) {
        this.isBase = isBase;
    }

    public HeatTemplate getHeatTemplate() {
        return heatTemplate;
    }

    public void setHeatTemplate(HeatTemplate heatTemplate) {
        this.heatTemplate = heatTemplate;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public List<HeatFile> getHeatFile() {
        return heatFile;
    }

    public void setHeatFile(List<HeatFile> heatFile) {
        this.heatFile = heatFile;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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

    public Integer getInitialCount() {
        return initialCount;
    }

    public void setInitialCount(Integer integer) {
        this.initialCount = integer;
    }

    public Integer getAvailabilityZoneCount() {
        return availabilityZoneCount;
    }

    public void setAvailabilityZoneCount(Integer availabilityZoneCount) {
        this.availabilityZoneCount = availabilityZoneCount;
    }

    public HeatEnvironment getHeatEnv() {
        return heatEnv;
    }

    public void setHeatEnv(HeatEnvironment heatEnv) {
        this.heatEnv = heatEnv;
    }

    public Boolean getIsVolumeGroup() {
        return isVolumeGroup;
    }

    public void setIsVolumeGroup(Boolean isVolumeGroup) {
        this.isVolumeGroup = isVolumeGroup;
    }



}
