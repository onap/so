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

@Entity
@Table(name = "cvnfc_customization")
public class CvnfcCustomization implements Serializable {

    private static final long serialVersionUID = -3772469944364616486L;

    @Id
    @Column(name = "ID")
    @BusinessKey
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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

    @Column(name = "NFC_FUNCTION")
    private String nfcFunction;

    @Column(name = "NFC_NAMING_CODE")
    private String nfcNamingCode;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "VF_MODULE_CUSTOMIZATION_ID")
    private VfModuleCustomization vfModuleCustomization;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "VNFC_CUST_MODEL_CUSTOMIZATION_UUID")
    private VnfcCustomization vnfcCustomization;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cvnfcCustomization")
    private List<CvnfcConfigurationCustomization> cvnfcConfigurationCustomization;

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof CvnfcCustomization)) {
            return false;
        }
        CvnfcCustomization castOther = (CvnfcCustomization) other;
        return new EqualsBuilder().append(id, castOther.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("modelCustomizationUUID", modelCustomizationUUID)
                .append("modelInstanceName", modelInstanceName).append("modelUUID", modelUUID)
                .append("modelInvariantUUID", modelInvariantUUID).append("modelVersion", modelVersion)
                .append("modelName", modelName).append("toscaNodeType", toscaNodeType)
                .append("description", description).append("nfcFunction", nfcFunction)
                .append("nfcNamingCode", nfcNamingCode).append("created", created)
                .append("vnfVfmoduleCvnfcConfigurationCustomization", cvnfcConfigurationCustomization).toString();
    }

    public VnfcCustomization getVnfcCustomization() {
        return vnfcCustomization;
    }

    public void setVnfcCustomization(VnfcCustomization vnfcCustomization) {
        this.vnfcCustomization = vnfcCustomization;
    }

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    public List<CvnfcConfigurationCustomization> getCvnfcConfigurationCustomization() {
        return cvnfcConfigurationCustomization;
    }

    public void setCvnfcConfigurationCustomization(
            List<CvnfcConfigurationCustomization> cvnfcConfigurationCustomization) {
        this.cvnfcConfigurationCustomization = cvnfcConfigurationCustomization;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getNfcFunction() {
        return nfcFunction;
    }

    public void setNfcFunction(String nfcFunction) {
        this.nfcFunction = nfcFunction;
    }

    public String getNfcNamingCode() {
        return nfcNamingCode;
    }

    public void setNfcNamingCode(String nfcNamingCode) {
        this.nfcNamingCode = nfcNamingCode;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public VfModuleCustomization getVfModuleCustomization() {
        return vfModuleCustomization;
    }

    public void setVfModuleCustomization(VfModuleCustomization vfModuleCustomization) {
        this.vfModuleCustomization = vfModuleCustomization;
    }

}
