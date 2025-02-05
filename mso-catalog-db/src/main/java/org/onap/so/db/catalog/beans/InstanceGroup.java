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
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
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
import com.openpojo.business.annotation.BusinessKey;

@Entity
@DiscriminatorColumn(name = "OBJECT_TYPE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "instance_group")
public class InstanceGroup implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -263859017727376578L;

    @BusinessKey
    @Id
    @Column(name = "MODEL_UUID")
    private String modelUUID;

    @Column(name = "MODEL_NAME")
    private String modelName;

    @Column(name = "MODEL_INVARIANT_UUID")
    private String modelInvariantUUID;

    @Column(name = "MODEL_VERSION")
    private String modelVersion;

    @Column(name = "ROLE")
    private String role;

    @Column(name = "TOSCA_NODE_TYPE")
    private String toscaNodeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "INSTANCE_GROUP_TYPE")
    private InstanceGroupType type;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "CR_MODEL_UUID")
    private CollectionResource collectionResource;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "instanceGroup")
    private List<CollectionResourceInstanceGroupCustomization> collectionInstanceGroupCustomizations;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "instanceGroup")
    private List<VnfcInstanceGroupCustomization> vnfcInstanceGroupCustomizations = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "instanceGroup")
    private List<CollectionNetworkResourceCustomization> collectionNetworkResourceCustomizations;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("modelUUID", modelUUID).append("modelName", modelName)
                .append("modelInvariantUUID", modelInvariantUUID).append("modelVersion", modelVersion)
                .append("role", role).append("toscaNodeType", toscaNodeType).append("type", type)
                .append("created", created).append("collectionResource", collectionResource)
                .append("collectionInstanceGroupCustomizations", collectionInstanceGroupCustomizations)
                .append("vnfcInstanceGroupCustomizations", vnfcInstanceGroupCustomizations).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof InstanceGroup)) {
            return false;
        }
        InstanceGroup castOther = (InstanceGroup) other;
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

    public String getModelUUID() {
        return modelUUID;
    }

    public Date getCreated() {
        return created;
    }

    public void setModelUUID(String modelUUID) {
        this.modelUUID = modelUUID;
    }

    public String getModelName() {
        return modelName;
    }

    public String getToscaNodeType() {
        return toscaNodeType;
    }

    public void setToscaNodeType(String toscaNodeType) {
        this.toscaNodeType = toscaNodeType;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public InstanceGroupType getType() {
        return type;
    }

    public void setType(InstanceGroupType type) {
        this.type = type;
    }

    public CollectionResource getCollectionResource() {
        return collectionResource;
    }

    public void setCollectionResource(CollectionResource collectionResource) {
        this.collectionResource = collectionResource;
    }

    public List<CollectionResourceInstanceGroupCustomization> getCollectionInstanceGroupCustomizations() {
        return collectionInstanceGroupCustomizations;
    }

    public void setCollectionInstanceGroupCustomizations(
            List<CollectionResourceInstanceGroupCustomization> collectionInstanceGroupCustomizations) {
        this.collectionInstanceGroupCustomizations = collectionInstanceGroupCustomizations;
    }

    public List<VnfcInstanceGroupCustomization> getVnfcInstanceGroupCustomizations() {
        return vnfcInstanceGroupCustomizations;
    }

    public void setVnfcInstanceGroupCustomizations(
            List<VnfcInstanceGroupCustomization> vnfcInstanceGroupCustomizations) {
        this.vnfcInstanceGroupCustomizations = vnfcInstanceGroupCustomizations;
    }

    public List<CollectionNetworkResourceCustomization> getCollectionNetworkResourceCustomizations() {
        return collectionNetworkResourceCustomizations;
    }

    public void setCollectionNetworkResourceCustomizations(
            List<CollectionNetworkResourceCustomization> collectionNetworkResourceCustomizations) {
        this.collectionNetworkResourceCustomizations = collectionNetworkResourceCustomizations;
    }
}
