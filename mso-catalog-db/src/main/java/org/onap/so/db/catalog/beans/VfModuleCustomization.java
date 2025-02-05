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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "vf_module_customization")
public class VfModuleCustomization implements Serializable {

    public static final long serialVersionUID = -1322322139926390329L;

    @Id
    @BusinessKey
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "MODEL_CUSTOMIZATION_UUID")
    private String modelCustomizationUUID;

    @Column(name = "LABEL")
    private String label;

    @Column(name = "MIN_INSTANCES", nullable = false)
    private Integer minInstances = 0;

    @Column(name = "MAX_INSTANCES")
    private Integer maxInstances;

    @Column(name = "INITIAL_COUNT", nullable = false)
    private Integer initialCount = 0;

    @Column(name = "AVAILABILITY_ZONE_COUNT")
    private Integer availabilityZoneCount;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "VOL_ENVIRONMENT_ARTIFACT_UUID")
    HeatEnvironment volumeHeatEnv;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "HEAT_ENVIRONMENT_ARTIFACT_UUID")
    HeatEnvironment heatEnvironment;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "VF_MODULE_MODEL_UUID")
    private VfModule vfModule;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "VNF_RESOURCE_CUSTOMIZATION_ID")
    private VnfResourceCustomization vnfCustomization;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vfModuleCustomization")
    private List<CvnfcCustomization> cvnfcCustomization;

    @Column(name = "SKIP_POST_INSTANTIATION_CONFIGURATION", nullable = false)
    private Boolean skipPostInstConf = true;

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof VfModuleCustomization)) {
            return false;
        }
        VfModuleCustomization castOther = (VfModuleCustomization) other;
        return new EqualsBuilder().append(id, castOther.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("modelCustomizationUUID", modelCustomizationUUID).append("label", label)
                .append("minInstances", minInstances).append("maxInstances", maxInstances)
                .append("initialCount", initialCount).append("availabilityZoneCount", availabilityZoneCount)
                .append("created", created).append("volumeHeatEnv", volumeHeatEnv)
                .append("heatEnvironment", heatEnvironment).append("vfModule", vfModule).toString();
    }



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public VnfResourceCustomization getVnfCustomization() {
        return vnfCustomization;
    }

    public void setVnfCustomization(VnfResourceCustomization vnfCustomization) {
        this.vnfCustomization = vnfCustomization;
    }

    public VfModuleCustomization() {
        super();
    }

    public String getModelCustomizationUUID() {
        return this.modelCustomizationUUID;
    }

    public void setModelCustomizationUUID(String modelCustomizationUUID) {
        this.modelCustomizationUUID = modelCustomizationUUID;
    }

    public HeatEnvironment getVolumeHeatEnv() {
        return volumeHeatEnv;
    }

    public void setVolumeHeatEnv(HeatEnvironment volumeHeatEnv) {
        this.volumeHeatEnv = volumeHeatEnv;
    }

    public HeatEnvironment getHeatEnvironment() {
        return heatEnvironment;
    }

    public void setHeatEnvironment(HeatEnvironment heatEnvironment) {
        this.heatEnvironment = heatEnvironment;
    }

    public Integer getMinInstances() {
        return this.minInstances;
    }

    public void setMinInstances(Integer minInstances) {
        this.minInstances = minInstances;
    }

    public Integer getMaxInstances() {
        return this.maxInstances;
    }

    public void setMaxInstances(Integer maxInstances) {
        this.maxInstances = maxInstances;
    }

    public Integer getInitialCount() {
        return this.initialCount;
    }

    public void setInitialCount(Integer initialCount) {
        this.initialCount = initialCount;
    }

    public Integer getAvailabilityZoneCount() {
        return this.availabilityZoneCount;
    }

    public void setAvailabilityZoneCount(Integer availabilityZoneCount) {
        this.availabilityZoneCount = availabilityZoneCount;
    }

    public Date getCreated() {
        return created;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public VfModule getVfModule() {
        return this.vfModule;
    }

    public void setVfModule(VfModule vfModule) {
        this.vfModule = vfModule;
    }

    public List<CvnfcCustomization> getCvnfcCustomization() {
        if (cvnfcCustomization == null)
            cvnfcCustomization = new ArrayList<>();
        return cvnfcCustomization;
    }

    public void setCvnfcCustomization(List<CvnfcCustomization> cvnfcCustomization) {
        this.cvnfcCustomization = cvnfcCustomization;
    }

    public Boolean getSkipPostInstConf() {
        return skipPostInstConf;
    }

    public void setSkipPostInstConf(Boolean skipPostInstConf) {
        this.skipPostInstConf = skipPostInstConf;
    }
}
