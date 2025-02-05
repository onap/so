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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "collection_network_resource_customization")
public class CollectionNetworkResourceCustomization implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 11115611315801079L;

    @BusinessKey
    @Id
    @Column(name = "MODEL_CUSTOMIZATION_UUID")
    private String modelCustomizationUUID = null;

    @Column(name = "MODEL_INSTANCE_NAME")
    private String modelInstanceName;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @BusinessKey
    @Column(name = "NETWORK_TECHNOLOGY")
    private String networkTechnology;

    @BusinessKey
    @Column(name = "NETWORK_TYPE")
    private String networkType = null;

    @BusinessKey
    @Column(name = "NETWORK_SCOPE")
    private String networkScope;

    @BusinessKey
    @Column(name = "NETWORK_ROLE")
    private String networkRole;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "NETWORK_RESOURCE_MODEL_UUID")
    private NetworkResource networkResource = null;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "INSTANCE_GROUP_MODEL_UUID")
    private InstanceGroup instanceGroup = null;

    @BusinessKey
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "CRC_MODEL_CUSTOMIZATION_UUID")
    private NetworkCollectionResourceCustomization networkResourceCustomization = null;

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof CollectionNetworkResourceCustomization)) {
            return false;
        }
        CollectionNetworkResourceCustomization castOther = (CollectionNetworkResourceCustomization) other;
        return new EqualsBuilder().append(modelCustomizationUUID, castOther.modelCustomizationUUID)
                .append(networkTechnology, castOther.networkTechnology).append(networkType, castOther.networkType)
                .append(networkScope, castOther.networkScope).append(networkRole, castOther.networkRole)
                .append(networkResourceCustomization, castOther.networkResourceCustomization).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(modelCustomizationUUID).append(networkTechnology).append(networkType)
                .append(networkScope).append(networkRole).append(networkResourceCustomization).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("modelCustomizationUUID", modelCustomizationUUID)
                .append("modelInstanceName", modelInstanceName).append("created", created)
                .append("networkTechnology", networkTechnology).append("networkType", networkType)
                .append("networkScope", networkScope).append("networkRole", networkRole)
                .append("networkResource", networkResource).append("collectionResource", networkResourceCustomization)
                .toString();
    }

    public String getModelCustomizationUUID() {
        return this.modelCustomizationUUID;
    }

    public void setModelCustomizationUUID(String modelCustomizationUUID) {
        this.modelCustomizationUUID = modelCustomizationUUID;
    }

    public NetworkCollectionResourceCustomization getNetworkResourceCustomization() {
        return networkResourceCustomization;
    }

    public void setNetworkResourceCustomization(NetworkCollectionResourceCustomization networkResourceCustomization) {
        this.networkResourceCustomization = networkResourceCustomization;
    }

    public String getModelInstanceName() {
        return this.modelInstanceName;
    }

    public void setModelInstanceName(String modelInstanceName) {
        this.modelInstanceName = modelInstanceName;
    }

    public NetworkResource getNetworkResource() {
        return this.networkResource;
    }

    public void setNetworkResource(NetworkResource networkResource) {
        this.networkResource = networkResource;
    }

    public String getNetworkType() {
        return this.networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public Date getCreated() {
        return this.created;
    }

    public String getNetworkTechnology() {
        return this.networkTechnology;
    }

    public void setNetworkTechnology(String networkTechnology) {
        this.networkTechnology = networkTechnology;
    }

    public String getNetworkScope() {
        return this.networkScope;
    }

    public void setNetworkScope(String networkScope) {
        this.networkScope = networkScope;
    }

    public void setNetworkRole(String networkRole) {
        this.networkRole = networkRole;
    }

    public String getNetworkRole() {
        return this.networkRole;
    }

    public InstanceGroup getInstanceGroup() {
        return instanceGroup;
    }

    public void setInstanceGroup(InstanceGroup instanceGroup) {
        this.instanceGroup = instanceGroup;
    }

}
