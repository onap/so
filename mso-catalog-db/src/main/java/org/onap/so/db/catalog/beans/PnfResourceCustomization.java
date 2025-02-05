/*
 * ============LICENSE_START======================================================= Copyright (C) 2019 Nordix
 * Foundation. ================================================================================ Licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 *
 * SPDX-License-Identifier: Apache-2.0 ============LICENSE_END=========================================================
 */

package org.onap.so.db.catalog.beans;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.openpojo.business.annotation.BusinessKey;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name = "pnf_resource_customization")
public class PnfResourceCustomization implements Serializable {

    private static final long serialVersionUID = 768026109321305415L;

    @BusinessKey
    @Id
    @Column(name = "MODEL_CUSTOMIZATION_UUID")
    private String modelCustomizationUUID;

    @Column(name = "MODEL_INSTANCE_NAME")
    private String modelInstanceName;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

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
    @JoinColumn(name = "PNF_RESOURCE_MODEL_UUID")
    private PnfResource pnfResources;

    @Column(name = "CDS_BLUEPRINT_NAME")
    private String blueprintName;

    @Column(name = "CDS_BLUEPRINT_VERSION")
    private String blueprintVersion;

    @Column(name = "SKIP_POST_INSTANTIATION_CONFIGURATION", nullable = false)
    private boolean skipPostInstConf = true;

    @Column(name = "CONTROLLER_ACTOR")
    private String controllerActor;

    @Column(name = "DEFAULT_SOFTWARE_VERSION")
    private String defaultSoftwareVersion;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("modelCustomizationUUID", modelCustomizationUUID)
                .append("modelInstanceName", modelInstanceName).append("created", created)
                .append("nfFunction", nfFunction).append("nfType", nfType).append("nfRole", nfRole)
                .append("nfNamingCode", nfNamingCode).append("multiStageDesign", multiStageDesign)
                .append("pnfResources", pnfResources).append("blueprintName", blueprintName)
                .append("blueprintVersion", blueprintVersion).append("controllerActor", controllerActor)
                .append("defaultSoftwareVersion", defaultSoftwareVersion).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof PnfResourceCustomization)) {
            return false;
        }
        PnfResourceCustomization castOther = (PnfResourceCustomization) other;
        return new EqualsBuilder().append(modelCustomizationUUID, castOther.modelCustomizationUUID).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(modelCustomizationUUID).toHashCode();
    }

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    public Date getCreationTimestamp() {
        return this.created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getModelCustomizationUUID() {
        return modelCustomizationUUID;
    }

    public void setModelCustomizationUUID(String modelCustomizationUUID) {
        this.modelCustomizationUUID = modelCustomizationUUID;
    }

    public String getModelInstanceName() {
        return this.modelInstanceName;
    }

    public void setModelInstanceName(String modelInstanceName) {
        this.modelInstanceName = modelInstanceName;
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

    public PnfResource getPnfResources() {
        return pnfResources;
    }

    public void setPnfResources(PnfResource pnfResources) {
        this.pnfResources = pnfResources;
    }

    public Date getCreated() {
        return created;
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

    public boolean getSkipPostInstConf() {
        return skipPostInstConf;
    }

    public void setSkipPostInstConf(boolean skipPostInstConf) {
        this.skipPostInstConf = skipPostInstConf;
    }

    public String getControllerActor() {
        return controllerActor;
    }

    public void setControllerActor(String controllerActor) {
        this.controllerActor = controllerActor;
    }

    public String getDefaultSoftwareVersion() {
        return defaultSoftwareVersion;
    }

    public void setDefaultSoftwareVersion(String defaultSoftwareVersion) {
        this.defaultSoftwareVersion = defaultSoftwareVersion;
    }
}
