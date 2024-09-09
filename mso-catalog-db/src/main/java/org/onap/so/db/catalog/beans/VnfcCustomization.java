/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2019 Huawei Intellectual Property. All rights reserved.
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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.openpojo.business.annotation.BusinessKey;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/vnfcCustomization")
@Table(name = "vnfc_customization")
public class VnfcCustomization implements Serializable {

    private static final long serialVersionUID = -3772469944364616486L;

    @BusinessKey
    @Id
    @Column(name = "MODEL_CUSTOMIZATION_UUID")
    private String modelCustomizationUUID;

    @Column(name = "MODEL_INSTANCE_NAME")
    private String modelInstanceName;

    @Column(name = "MODEL_UUID")
    private String modelUUID;

    @Column(name = "MODEL_INVARIANT_UUID")
    private String modelInvariantUUID;

    @Column(name = "MODEL_VERSION")
    private String modelVersion;

    @Column(name = "MODEL_NAME")
    private String modelName;

    @Column(name = "TOSCA_NODE_TYPE")
    private String toscaNodeType;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "RESOURCE_INPUT")
    private String resourceInput;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vnfcCustomization")
    private List<CvnfcCustomization> cvnfcCustomization;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "VNFC_INSTANCE_GROUP_CUSTOMIZATION_ID")
    private VnfcInstanceGroupCustomization vnfcInstanceGroupCustomization;

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof VnfcCustomization)) {
            return false;
        }
        VnfcCustomization castOther = (VnfcCustomization) other;
        return new EqualsBuilder().append(modelCustomizationUUID, castOther.modelCustomizationUUID).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(modelCustomizationUUID).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("modelCustomizationUUID", modelCustomizationUUID)
                .append("modelInstanceName", modelInstanceName).append("modelUUID", modelUUID)
                .append("modelInvariantUUID", modelInvariantUUID).append("modelVersion", modelVersion)
                .append("modelName", modelName).append("toscaNodeType", toscaNodeType)
                .append("description", description).append("created", created)
                .append("cvnfcCustomization", cvnfcCustomization).toString();
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

    public String getModelInstanceName() {
        return modelInstanceName;
    }

    public void setModelInstanceName(String modelInstanceName) {
        this.modelInstanceName = modelInstanceName;
    }

    public String getModelUUID() {
        return modelUUID;
    }

    public void setModelUUID(String modelUUID) {
        this.modelUUID = modelUUID;
    }

    public String getModelInvariantUUID() {
        return modelInvariantUUID;
    }

    public void setModelInvariantUUID(String modelInvariantUUID) {
        this.modelInvariantUUID = modelInvariantUUID;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getToscaNodeType() {
        return toscaNodeType;
    }

    public void setToscaNodeType(String toscaNodeType) {
        this.toscaNodeType = toscaNodeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public List<CvnfcCustomization> getCvnfcCustomization() {
        return cvnfcCustomization;
    }

    public void setCvnfcCustomization(List<CvnfcCustomization> cvnfcCustomization) {
        this.cvnfcCustomization = cvnfcCustomization;
    }

    public VnfcInstanceGroupCustomization getVnfcInstanceGroupCustomization() {
        return vnfcInstanceGroupCustomization;
    }

    public void setVnfcInstanceGroupCustomization(VnfcInstanceGroupCustomization vnfcInstanceGroupCustomization) {
        this.vnfcInstanceGroupCustomization = vnfcInstanceGroupCustomization;
    }

    public String getResourceInput() {
        return resourceInput;
    }

    public void setResourceInput(String resourceInput) {
        this.resourceInput = resourceInput;
    }
}
