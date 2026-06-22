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

package org.onap.so.db.catalog.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/vfModule")
@Table(name = "vf_module")
public class VfModule implements Serializable {

    private static final long serialVersionUID = 768026109321305392L;

    @Id
    @BusinessKey
    @Column(name = "MODEL_UUID")
    private String modelUUID;

    @Column(name = "MODEL_INVARIANT_UUID")
    private String modelInvariantUUID;

    @Column(name = "MODEL_NAME")
    private String modelName;

    @Column(name = "MODEL_VERSION")
    private String modelVersion;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "IS_BASE")
    private Boolean isBase;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "VOL_HEAT_TEMPLATE_ARTIFACT_UUID")
    private HeatTemplate volumeHeatTemplate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "HEAT_TEMPLATE_ARTIFACT_UUID")
    private HeatTemplate moduleHeatTemplate;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "vf_module_to_heat_files", joinColumns = @JoinColumn(name = "VF_MODULE_MODEL_UUID"),
            inverseJoinColumns = @JoinColumn(name = "HEAT_FILES_ARTIFACT_UUID"))
    private List<HeatFiles> heatFiles;

    @OneToMany(mappedBy = "vfModule")
    private List<VfModuleCustomization> vfModuleCustomization;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "VNF_RESOURCE_MODEL_UUID")
    private VnfResource vnfResources;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("modelUUID", modelUUID).append("modelInvariantUUID", modelInvariantUUID)
                .append("modelName", modelName).append("modelVersion", modelVersion).append("description", description)
                .append("isBase", isBase).append("volumeHeatTemplate", volumeHeatTemplate)
                .append("moduleHeatTemplate", moduleHeatTemplate).append("created", created)
                .append("heatFiles", heatFiles).append("vfModuleCustomization", vfModuleCustomization)
                .append("vnfResources", vnfResources).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof VfModule)) {
            return false;
        }
        VfModule castOther = (VfModule) other;
        return new EqualsBuilder().append(modelUUID, castOther.modelUUID).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(modelUUID).toHashCode();
    }

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    public String getModelInvariantUUID() {
        return modelInvariantUUID;
    }

    public void setModelInvariantUUID(String modelInvariantUUID) {
        this.modelInvariantUUID = modelInvariantUUID;
    }

    @LinkedResource
    public List<VfModuleCustomization> getVfModuleCustomization() {
        if (vfModuleCustomization == null)
            vfModuleCustomization = new ArrayList<>();

        return vfModuleCustomization;
    }

    public void setVfModuleCustomization(List<VfModuleCustomization> vfModuleCustomization) {
        this.vfModuleCustomization = vfModuleCustomization;
    }

    public String getModelName() {
        return this.modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Boolean getIsBase() {
        return isBase;
    }

    public void setIsBase(Boolean isBase) {
        this.isBase = isBase;
    }

    @LinkedResource
    public List<HeatFiles> getHeatFiles() {
        if (heatFiles == null)
            heatFiles = new ArrayList<>();
        return heatFiles;
    }

    public void setHeatFiles(List<HeatFiles> heatFiles) {
        this.heatFiles = heatFiles;
    }

    @LinkedResource
    public VnfResource getVnfResources() {
        return vnfResources;
    }

    public void setVnfResources(VnfResource vnfResources) {
        this.vnfResources = vnfResources;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreated() {
        return created;
    }

    public String getModelUUID() {
        return modelUUID;
    }

    public void setModelUUID(String modelUUID) {
        this.modelUUID = modelUUID;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    @LinkedResource
    public HeatTemplate getVolumeHeatTemplate() {
        return volumeHeatTemplate;
    }

    public void setVolumeHeatTemplate(HeatTemplate volumeHeatTemplate) {
        this.volumeHeatTemplate = volumeHeatTemplate;
    }

    @LinkedResource
    public HeatTemplate getModuleHeatTemplate() {
        return moduleHeatTemplate;
    }

    public void setModuleHeatTemplate(HeatTemplate moduleHeatTemplate) {
        this.moduleHeatTemplate = moduleHeatTemplate;
    }
}
