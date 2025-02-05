/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "vnf_resource_to_workflow")
public class VnfResourceWorkflow implements Serializable {

    private static final long serialVersionUID = -1326433350241927676L;

    @Id
    @Column(name = "ID", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ID;

    @BusinessKey
    @Column(name = "VNF_RESOURCE_MODEL_UUID")
    private String vnfResourceModelUUID;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "VNF_RESOURCE_MODEL_UUID", updatable = false, insertable = false)
    private VnfResource vnfResource;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "WORKFLOW_ID")
    private Workflow workflow;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("vnfResourceModelUUID", vnfResourceModelUUID).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof VnfResourceWorkflow)) {
            return false;
        }
        VnfResourceWorkflow castOther = (VnfResourceWorkflow) other;
        return new EqualsBuilder().append(vnfResourceModelUUID, castOther.vnfResourceModelUUID).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(vnfResourceModelUUID).toHashCode();
    }

    public Integer getID() {
        return ID;
    }

    public String getVnfResourceModelUUID() {
        return vnfResourceModelUUID;
    }

    public void setVnfResourceModelUUID(String vnfResourceModelUUID) {
        this.vnfResourceModelUUID = vnfResourceModelUUID;
    }

    public VnfResource getVnfResource() {
        return vnfResource;
    }

    public void setVnfResource(VnfResource vnfResource) {
        this.vnfResource = vnfResource;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

}
