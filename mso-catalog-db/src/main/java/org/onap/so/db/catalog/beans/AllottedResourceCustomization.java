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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/allottedResourceCustomization")
@Table(name = "allotted_resource_customization")
public class AllottedResourceCustomization implements Serializable {

    private static final long serialVersionUID = 768026109321305392L;

    @BusinessKey
    @Id
    @Column(name = "MODEL_CUSTOMIZATION_UUID")
    private String modelCustomizationUUID;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "MODEL_INSTANCE_NAME")
    private String modelInstanceName;

    @Column(name = "PROVIDING_SERVICE_MODEL_INVARIANT_UUID")
    private String providingServiceModelInvariantUUID;

    @Column(name = "PROVIDING_SERVICE_MODEL_UUID")
    private String providingServiceModelUUID;

    @Column(name = "PROVIDING_SERVICE_MODEL_NAME")
    private String providingServiceModelName;

    @Column(name = "TARGET_NETWORK_ROLE")
    private String targetNetworkRole;

    @Column(name = "NF_FUNCTION")
    private String nfFunction;

    @Column(name = "NF_TYPE")
    private String nfType;

    @Column(name = "NF_ROLE")
    private String nfRole;

    @Column(name = "NF_NAMING_CODE")
    private String nfNamingCode;

    @Column(name = "MIN_INSTANCES")
    private Integer minInstances;

    @Column(name = "MAX_INSTANCES")
    private Integer maxInstances;

    @Column(name = "RESOURCE_INPUT")
    private String resourceInput;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "AR_MODEL_UUID")
    private AllottedResource allottedResource;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("modelCustomizationUUID", modelCustomizationUUID)
                .append("created", created).append("modelInstanceName", modelInstanceName)
                .append("providingServiceModelInvariantUUID", providingServiceModelInvariantUUID)
                .append("providingServiceModelUUID", providingServiceModelUUID)
                .append("providingServiceModelName", providingServiceModelName)
                .append("targetNetworkRole", targetNetworkRole).append("nfFunction", nfFunction)
                .append("nfType", nfType).append("nfRole", nfRole).append("nfNamingCode", nfNamingCode)
                .append("minInstances", minInstances).append("maxInstances", maxInstances)
                .append("allottedResource", allottedResource).toString();
    }

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof AllottedResourceCustomization)) {
            return false;
        }
        AllottedResourceCustomization castOther = (AllottedResourceCustomization) other;
        return new EqualsBuilder().append(modelCustomizationUUID, castOther.modelCustomizationUUID).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(modelCustomizationUUID).toHashCode();
    }

    public String getProvidingServiceModelUUID() {
        return providingServiceModelUUID;
    }

    public void setProvidingServiceModelUUID(String providingServiceModelUUID) {
        this.providingServiceModelUUID = providingServiceModelUUID;
    }

    public String getProvidingServiceModelName() {
        return providingServiceModelName;
    }

    public void setProvidingServiceModelName(String providingServiceModelName) {
        this.providingServiceModelName = providingServiceModelName;
    }

    public AllottedResourceCustomization() {
        super();
    }

    public String getModelCustomizationUUID() {
        return this.modelCustomizationUUID;
    }

    public void setModelCustomizationUUID(String modelCustomizationUUID) {
        this.modelCustomizationUUID = modelCustomizationUUID;
    }

    public Date getCreated() {
        return this.created;
    }

    public String getModelInstanceName() {
        return this.modelInstanceName;
    }

    public void setModelInstanceName(String modelInstanceName) {
        this.modelInstanceName = modelInstanceName;
    }

    @LinkedResource
    public AllottedResource getAllottedResource() {
        return this.allottedResource;
    }

    public void setAllottedResource(AllottedResource allottedResource) {
        this.allottedResource = allottedResource;
    }

    public String getProvidingServiceModelInvariantUUID() {
        return this.providingServiceModelInvariantUUID;
    }

    public void setProvidingServiceModelInvariantUUID(String providingServiceModelInvariantUUID) {
        this.providingServiceModelInvariantUUID = providingServiceModelInvariantUUID;
    }

    public String getTargetNetworkRole() {
        return this.targetNetworkRole;
    }

    public void setTargetNetworkRole(String targetNetworkRole) {
        this.targetNetworkRole = targetNetworkRole;
    }

    public String getNfFunction() {
        return this.nfFunction;
    }

    public void setNfFunction(String nfFunction) {
        this.nfFunction = nfFunction;
    }

    public String getNfType() {
        return this.nfType;
    }

    public void setNfType(String nfType) {
        this.nfType = nfType;
    }

    public String getNfRole() {
        return this.nfRole;
    }

    public void setNfRole(String nfRole) {
        this.nfRole = nfRole;
    }

    public String getNfNamingCode() {
        return this.nfNamingCode;
    }

    public void setNfNamingCode(String nfNamingCode) {
        this.nfNamingCode = nfNamingCode;
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

    public String getResourceInput() {
        return resourceInput;
    }

    public void setResourceInput(String resourceInput) {
        this.resourceInput = resourceInput;
    }
}
