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
/*
 * import javax.persistence.CascadeType; import javax.persistence.Column; import javax.persistence.DiscriminatorColumn;
 * import javax.persistence.Entity; import javax.persistence.FetchType; import javax.persistence.Id; import
 * javax.persistence.Inheritance; import javax.persistence.InheritanceType; import javax.persistence.JoinColumn; import
 * javax.persistence.ManyToOne; import javax.persistence.OneToMany; import javax.persistence.PrePersist; import
 * javax.persistence.Table; import javax.persistence.Temporal; import javax.persistence.TemporalType;
 */
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
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
import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/collectionResourceCustomization")
@DiscriminatorColumn(name = "OBJECT_TYPE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "collection_resource_customization")
public class CollectionResourceCustomization implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8328823396870652841L;

    @BusinessKey
    @Id
    @Column(name = "MODEL_CUSTOMIZATION_UUID")
    private String modelCustomizationUUID;

    @Column(name = "MODEL_INSTANCE_NAME")
    private String modelInstanceName;

    @Column(name = "COLLECTION_RESOURCE_TYPE")
    private String type;

    @Column(name = "ROLE")
    private String role;

    @Column(name = "FUNCTION")
    private String function;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "CR_MODEL_UUID")
    private CollectionResource collectionResource;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "collectionResourceCust")
    private List<CollectionResourceInstanceGroupCustomization> collectionInstanceGroupCustomizations;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("modelCustomizationUUID", modelCustomizationUUID)
                .append("modelInstanceName", modelInstanceName).append("type", type).append("role", role)
                .append("function", function).append("created", created)
                .append("collectionResource", collectionResource)
                .append("collectionInstanceGroupCustomizations", collectionInstanceGroupCustomizations).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof CollectionResourceCustomization)) {
            return false;
        }
        CollectionResourceCustomization castOther = (CollectionResourceCustomization) other;
        return new EqualsBuilder().append(modelCustomizationUUID, castOther.modelCustomizationUUID).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(modelCustomizationUUID).toHashCode();
    }

    public Date getCreated() {
        return this.created;
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

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    @LinkedResource
    public CollectionResource getCollectionResource() {
        return collectionResource;
    }

    public void setCollectionResource(CollectionResource collectionResource) {
        this.collectionResource = collectionResource;
    }

    @LinkedResource
    public List<CollectionResourceInstanceGroupCustomization> getCollectionInstanceGroupCustomizations() {
        return collectionInstanceGroupCustomizations;
    }

    public void setCollectionInstanceGroupCustomizations(
            List<CollectionResourceInstanceGroupCustomization> collectionInstanceGroupCustomizations) {
        this.collectionInstanceGroupCustomizations = collectionInstanceGroupCustomizations;
    }
}
