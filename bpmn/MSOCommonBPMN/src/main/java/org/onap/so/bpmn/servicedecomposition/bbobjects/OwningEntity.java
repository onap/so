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

package org.onap.so.bpmn.servicedecomposition.bbobjects;

import java.io.Serializable;
import jakarta.persistence.Id;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;

@JsonRootName("owning-entity")
public class OwningEntity implements Serializable, ShallowCopy<OwningEntity> {

    private static final long serialVersionUID = -6565917570694869603L;

    @Id
    @JsonProperty("owning-entity-id")
    private String owningEntityId;
    @JsonProperty("owning-entity-name")
    private String owningEntityName;

    public String getOwningEntityId() {
        return owningEntityId;
    }

    public void setOwningEntityId(String owningEntityId) {
        this.owningEntityId = owningEntityId;
    }

    public String getOwningEntityName() {
        return owningEntityName;
    }

    public void setOwningEntityName(String owningEntityName) {
        this.owningEntityName = owningEntityName;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof OwningEntity)) {
            return false;
        }
        OwningEntity castOther = (OwningEntity) other;
        return new EqualsBuilder().append(owningEntityId, castOther.owningEntityId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(owningEntityId).toHashCode();
    }
}
