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
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "configuration_customization")
public class ConfigurationResourceCustomization implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1230671937560638856L;

    @Id
    @BusinessKey
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "MODEL_CUSTOMIZATION_UUID")
    private String modelCustomizationUUID;

    @Column(name = "MODEL_INSTANCE_NAME")
    private String modelInstanceName;

    @Column(name = "CONFIGURATION_FUNCTION")
    private String function;

    @Column(name = "CONFIGURATION_TYPE")
    private String type;

    @Column(name = "CONFIGURATION_ROLE")
    private String role;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_PROXY_CUSTOMIZATION_MODEL_CUSTOMIZATION_UUID")
    private ServiceProxyResourceCustomization serviceProxyResourceCustomization;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "CONFIGURATION_CUSTOMIZATION_MODEL_CUSTOMIZATION_ID")
    private ConfigurationResourceCustomization configResourceCustomization;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "CONFIGURATION_MODEL_UUID")
    private ConfigurationResource configurationResource;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_MODEL_UUID")
    private Service service;

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

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Date getCreated() {
        return created;
    }

    public ServiceProxyResourceCustomization getServiceProxyResourceCustomization() {
        return serviceProxyResourceCustomization;
    }

    public void setServiceProxyResourceCustomization(
            ServiceProxyResourceCustomization serviceProxyResourceCustomization) {
        this.serviceProxyResourceCustomization = serviceProxyResourceCustomization;
    }


    public ConfigurationResourceCustomization getConfigResourceCustomization() {
        return configResourceCustomization;
    }

    public void setConfigResourceCustomization(ConfigurationResourceCustomization configResourceCustomization) {
        this.configResourceCustomization = configResourceCustomization;
    }

    public ConfigurationResource getConfigurationResource() {
        return configurationResource;
    }

    public void setConfigurationResource(ConfigurationResource configurationResource) {
        this.configurationResource = configurationResource;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("modelCustomizationUUID", modelCustomizationUUID)
                .append("modelInstanceName", modelInstanceName).append("function", function).append("type", type)
                .append("role", role).append("created", created)
                // .append("serviceProxyResourceCustomization", serviceProxyResourceCustomization)
                .append("configResourceCustomization", configResourceCustomization)
                .append("configurationResource", configurationResource).append("service", service).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ConfigurationResourceCustomization)) {
            return false;
        }
        ConfigurationResourceCustomization castOther = (ConfigurationResourceCustomization) other;
        return new EqualsBuilder().append(id, castOther.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }

}
