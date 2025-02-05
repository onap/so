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
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "collection_resource")
public class CollectionResource implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8612818857960992110L;

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

    @Column(name = "TOSCA_NODE_TYPE")
    private String toscaNodeType;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "collectionResource")
    private Set<CollectionResourceCustomization> collectionResourceCustomization;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "collectionResource")
    private InstanceGroup instanceGroup;

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof CollectionResource)) {
            return false;
        }
        CollectionResource castOther = (CollectionResource) other;
        return new EqualsBuilder().append(modelUUID, castOther.modelUUID).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(modelUUID).toHashCode();
    }

    public Set<CollectionResourceCustomization> getCollectionResourceCustomization() {
        return collectionResourceCustomization;
    }

    public void setCollectionResourceCustomization(
            Set<CollectionResourceCustomization> collectionResourceCustomization) {
        this.collectionResourceCustomization = collectionResourceCustomization;
    }

    public String getModelUUID() {
        return modelUUID;
    }

    public Date getCreated() {
        return this.created;
    }

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    public void setModelUUID(String modelUUID) {
        this.modelUUID = modelUUID;
    }

    public String getModelName() {
        return modelName;
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

    public InstanceGroup getInstanceGroup() {
        return instanceGroup;
    }

    public void setInstanceGroup(InstanceGroup instanceGroup) {
        this.instanceGroup = instanceGroup;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("modelUUID", modelUUID).append("modelName", modelName)
                .append("modelInvariantUUID", modelInvariantUUID).append("modelVersion", modelVersion)
                .append("toscaNodeType", toscaNodeType).append("description", description).append("created", created)
                .toString();
    }
}
