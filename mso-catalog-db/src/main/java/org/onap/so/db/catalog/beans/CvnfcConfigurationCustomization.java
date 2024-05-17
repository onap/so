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
/*
 * import javax.persistence.CascadeType; import javax.persistence.Column; import javax.persistence.Entity; import
 * javax.persistence.FetchType; import javax.persistence.GeneratedValue; import javax.persistence.GenerationType; import
 * javax.persistence.Id; import javax.persistence.JoinColumn; import javax.persistence.ManyToOne; import
 * javax.persistence.PrePersist; import javax.persistence.Table; import javax.persistence.Temporal; import
 * javax.persistence.TemporalType;
 */
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
import com.fasterxml.jackson.annotation.JsonFormat;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/cvnfcConfigurationCustomization")
@Table(name = "cvnfc_configuration_customization")
public class CvnfcConfigurationCustomization implements Serializable {

    private static final long serialVersionUID = -3153216266280581103L;

    @Id
    @BusinessKey
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "MODEL_CUSTOMIZATION_UUID")
    private String modelCustomizationUUID;

    @Column(name = "MODEL_INSTANCE_NAME")
    private String modelInstanceName;

    @Column(name = "CONFIGURATION_TYPE")
    private String configurationType;

    @Column(name = "CONFIGURATION_ROLE")
    private String configurationRole;

    @Column(name = "CONFIGURATION_FUNCTION")
    private String configurationFunction;

    @Column(name = "POLICY_NAME")
    private String policyName;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "CONFIGURATION_MODEL_UUID")
    private ConfigurationResource configurationResource;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "CVNFC_CUSTOMIZATION_ID")
    private CvnfcCustomization cvnfcCustomization;


    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof CvnfcConfigurationCustomization)) {
            return false;
        }
        CvnfcConfigurationCustomization castOther = (CvnfcConfigurationCustomization) other;
        return new EqualsBuilder().append(id, castOther.id).isEquals();
    }



    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }



    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("modelCustomizationUUID", modelCustomizationUUID)
                .append("modelInstanceName", modelInstanceName).append("configurationType", configurationType)
                .append("configurationRole", configurationRole).append("configurationFunction", configurationFunction)
                .append("policyName", policyName).append("created", created)
                .append("configurationResource", configurationResource).append("cvnfcCustomization", cvnfcCustomization)
                .toString();
    }



    @PrePersist
    protected void onCreate() {
        this.created = new Date();
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

    public String getConfigurationType() {
        return configurationType;
    }

    public void setConfigurationType(String configurationType) {
        this.configurationType = configurationType;
    }

    public String getConfigurationRole() {
        return configurationRole;
    }

    public void setConfigurationRole(String configurationRole) {
        this.configurationRole = configurationRole;
    }

    public String getConfigurationFunction() {
        return configurationFunction;
    }

    public void setConfigurationFunction(String configurationFunction) {
        this.configurationFunction = configurationFunction;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @LinkedResource
    public ConfigurationResource getConfigurationResource() {
        return configurationResource;
    }

    public void setConfigurationResource(ConfigurationResource configurationResource) {
        this.configurationResource = configurationResource;
    }

    @LinkedResource
    public CvnfcCustomization getCvnfcCustomization() {
        return cvnfcCustomization;
    }

    public void setCvnfcCustomization(CvnfcCustomization cvnfcCustomization) {
        this.cvnfcCustomization = cvnfcCustomization;
    }

}
