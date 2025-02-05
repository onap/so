/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.servicedecomposition.bbobjects;

import java.io.Serializable;
import jakarta.persistence.Id;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Tenant implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8704478668505531590L;
    @Id
    @JsonProperty("tenant-id")
    private String tenantId;
    @JsonProperty("tenant-name")
    private String tenantName;
    @JsonProperty("tenant-context")
    private String tenantContext;

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Tenant)) {
            return false;
        }
        Tenant castOther = (Tenant) other;
        return new EqualsBuilder().append(tenantId, castOther.tenantId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tenantId).toHashCode();
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantContext() {
        return tenantContext;
    }

    public void setTenantContext(String tenantContext) {
        this.tenantContext = tenantContext;
    }


}
