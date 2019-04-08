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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;

public class VnfResourceWorkflowId implements Serializable {

    private static final long serialVersionUID = -594459957997483601L;

    private Integer ID;

    @BusinessKey
    private String vnfResourceModelUUID;
    @BusinessKey
    private Integer workflowId;

    public String getVnfResourceModelUUID() {
        return vnfResourceModelUUID;
    }

    public void setVnfResourceModelUUID(String vnfResourceModelUUID) {
        this.vnfResourceModelUUID = vnfResourceModelUUID;
    }

    public Integer getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Integer workflowId) {
        this.workflowId = workflowId;
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("vnfResourceModelUUID", vnfResourceModelUUID)
                .append("workflowId", workflowId).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof VnfResourceWorkflowId)) {
            return false;
        }
        VnfResourceWorkflowId castOther = (VnfResourceWorkflowId) other;
        return new EqualsBuilder().append(vnfResourceModelUUID, castOther.vnfResourceModelUUID)
                .append(workflowId, castOther.workflowId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(vnfResourceModelUUID).append(workflowId).toHashCode();
    }
}
