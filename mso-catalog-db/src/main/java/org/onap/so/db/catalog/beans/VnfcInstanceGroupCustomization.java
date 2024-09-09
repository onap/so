/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/vnfcInstanceGroupCustomization")

@Table(name = "vnfc_instance_group_customization")
public class VnfcInstanceGroupCustomization implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8288218040186901676L;

    @Id
    @BusinessKey
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "VNF_RESOURCE_CUSTOMIZATION_ID")
    private VnfResourceCustomization vnfResourceCust;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "INSTANCE_GROUP_MODEL_UUID")
    private InstanceGroup instanceGroup;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "vnfcInstanceGroupCustomization")
    private List<VnfcCustomization> vnfcCustomizations;

    @Column(name = "FUNCTION")
    private String function;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof VnfcInstanceGroupCustomization)) {
            return false;
        }
        VnfcInstanceGroupCustomization castOther = (VnfcInstanceGroupCustomization) other;
        return new EqualsBuilder().append(id, castOther.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("function", function).append("description", description)
                .append("created", created).toString();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    public Date getCreated() {
        return created;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @LinkedResource
    public VnfResourceCustomization getVnfResourceCust() {
        return vnfResourceCust;
    }

    public void setVnfResourceCust(VnfResourceCustomization vnfResourceCust) {
        this.vnfResourceCust = vnfResourceCust;
    }

    @LinkedResource
    public InstanceGroup getInstanceGroup() {
        return instanceGroup;
    }

    public void setInstanceGroup(InstanceGroup instanceGroup) {
        this.instanceGroup = instanceGroup;
    }

    public List<VnfcCustomization> getVnfcCustomizations() {
        return vnfcCustomizations;
    }

    public void setVnfcCustomizations(List<VnfcCustomization> vnfcCustomizations) {
        this.vnfcCustomizations = vnfcCustomizations;
    }
}
