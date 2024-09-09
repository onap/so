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
import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/vnfResourceCustomization")
@Table(name = "vnf_resource_customization")
public class VnfResourceCustomization implements Serializable {

    private static final long serialVersionUID = 768026109321305392L;

    @Id
    @BusinessKey
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "MODEL_CUSTOMIZATION_UUID")
    private String modelCustomizationUUID;

    @Column(name = "MODEL_INSTANCE_NAME")
    private String modelInstanceName;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "MIN_INSTANCES")
    private Integer minInstances;

    @Column(name = "MAX_INSTANCES")
    private Integer maxInstances;

    @Column(name = "AVAILABILITY_ZONE_MAX_COUNT")
    private Integer availabilityZoneMaxCount;

    @Column(name = "NF_FUNCTION")
    private String nfFunction;

    @Column(name = "NF_TYPE")
    private String nfType;

    @Column(name = "NF_ROLE")
    private String nfRole;

    @Column(name = "NF_NAMING_CODE")
    private String nfNamingCode;

    @Column(name = "MULTI_STAGE_DESIGN")
    private String multiStageDesign;

    @Column(name = "RESOURCE_INPUT")
    private String resourceInput;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "VNF_RESOURCE_MODEL_UUID")
    private VnfResource vnfResources;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "SERVICE_MODEL_UUID")
    private Service service;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "vnfCustomization")
    private List<VfModuleCustomization> vfModuleCustomizations;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "vnfResourceCust")
    private List<VnfcInstanceGroupCustomization> vnfcInstanceGroupCustomizations = new ArrayList<>();

    @Column(name = "CDS_BLUEPRINT_NAME")
    private String blueprintName;

    @Column(name = "CDS_BLUEPRINT_VERSION")
    private String blueprintVersion;

    @Column(name = "SKIP_POST_INSTANTIATION_CONFIGURATION", nullable = false)
    private Boolean skipPostInstConf = true;

    @Column(name = "VNFCINSTANCEGROUP_ORDER")
    private String vnfcInstanceGroupOrder;

    @Column(name = "NF_DATA_VALID", nullable = false)
    private Boolean nfDataValid = false;

    @Column(name = "CONTROLLER_ACTOR")
    private String controllerActor;

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof VnfResourceCustomization)) {
            return false;
        }
        VnfResourceCustomization castOther = (VnfResourceCustomization) other;
        return new EqualsBuilder().append(id, castOther.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("modelCustomizationUUID", modelCustomizationUUID)
                .append("modelInstanceName", modelInstanceName).append("created", created)
                .append("minInstances", minInstances).append("maxInstances", maxInstances)
                .append("availabilityZoneMaxCount", availabilityZoneMaxCount).append("nfFunction", nfFunction)
                .append("nfType", nfType).append("nfRole", nfRole).append("nfNamingCode", nfNamingCode)
                .append("multiStageDesign", multiStageDesign).append("vnfResources", vnfResources)
                .append("vfModuleCustomizations", vfModuleCustomizations)
                .append("vnfcInstanceGroupOrder", vnfcInstanceGroupOrder)
                .append("vnfcInstanceGroupCustomizations", vnfcInstanceGroupCustomizations)
                .append("controllerActor", controllerActor).append("resourceInput", resourceInput).toString();
    }

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    public String getModelCustomizationUUID() {
        return modelCustomizationUUID;
    }

    public void setModelCustomizationUUID(String modelCustomizationUUID) {
        this.modelCustomizationUUID = modelCustomizationUUID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @LinkedResource
    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getModelInstanceName() {
        return this.modelInstanceName;
    }

    public void setModelInstanceName(String modelInstanceName) {
        this.modelInstanceName = modelInstanceName;
    }

    public Date getCreationTimestamp() {
        return this.created;
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

    public Integer getAvailabilityZoneMaxCount() {
        return this.availabilityZoneMaxCount;
    }

    public void setAvailabilityZoneMaxCount(Integer availabilityZoneMaxCount) {
        this.availabilityZoneMaxCount = availabilityZoneMaxCount;
    }

    public String getNfFunction() {
        return nfFunction;
    }

    public void setNfFunction(String nfFunction) {
        this.nfFunction = nfFunction;
    }

    public String getNfType() {
        return nfType;
    }

    public void setNfType(String nfType) {
        this.nfType = nfType;
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
        return this.multiStageDesign;
    }

    public void setMultiStageDesign(String multiStageDesign) {
        this.multiStageDesign = multiStageDesign;
    }

    @LinkedResource
    public List<VfModuleCustomization> getVfModuleCustomizations() {
        if (vfModuleCustomizations == null) {
            vfModuleCustomizations = new ArrayList<>();
        }
        return vfModuleCustomizations;
    }

    public void setVfModuleCustomizations(List<VfModuleCustomization> vfModuleCustomizations) {
        this.vfModuleCustomizations = vfModuleCustomizations;
    }

    @LinkedResource
    public VnfResource getVnfResources() {
        return vnfResources;
    }

    public void setVnfResources(VnfResource vnfResources) {
        this.vnfResources = vnfResources;
    }

    public Date getCreated() {
        return created;
    }

    @LinkedResource
    public List<VnfcInstanceGroupCustomization> getVnfcInstanceGroupCustomizations() {
        return vnfcInstanceGroupCustomizations;
    }

    public void setVnfcInstanceGroupCustomizations(
            List<VnfcInstanceGroupCustomization> vnfcInstanceGroupCustomizations) {
        this.vnfcInstanceGroupCustomizations = vnfcInstanceGroupCustomizations;
    }

    public String getResourceInput() {
        return resourceInput;
    }

    public void setResourceInput(String resourceInput) {
        this.resourceInput = resourceInput;
    }


    public String getBlueprintName() {
        return blueprintName;
    }

    public void setBlueprintName(String blueprintName) {
        this.blueprintName = blueprintName;
    }

    public String getBlueprintVersion() {
        return blueprintVersion;
    }

    public void setBlueprintVersion(String blueprintVersion) {
        this.blueprintVersion = blueprintVersion;
    }

    public Boolean getSkipPostInstConf() {
        return skipPostInstConf;
    }

    public void setSkipPostInstConf(Boolean skipPostInstConf) {
        this.skipPostInstConf = skipPostInstConf;
    }

    public String getVnfcInstanceGroupOrder() {
        return vnfcInstanceGroupOrder;
    }

    public void setVnfcInstanceGroupOrder(String vnfcInstanceGroupOrder) {
        this.vnfcInstanceGroupOrder = vnfcInstanceGroupOrder;
    }

    public Boolean getNfDataValid() {
        return nfDataValid;
    }

    public void setNfDataValid(Boolean nfDataValid) {
        this.nfDataValid = nfDataValid;
    }

    public String getControllerActor() {
        return controllerActor;
    }

    public void setControllerActor(String controllerActor) {
        this.controllerActor = controllerActor;
    }
}
