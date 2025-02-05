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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;
import jakarta.persistence.Id;

@JsonRootName("entitlement")
public class Entitlement implements Serializable, ShallowCopy<Entitlement> {

    private static final long serialVersionUID = 5186878328988717088L;

    @Id
    @JsonProperty("group-uuid")
    private String groupUuid;
    @Id
    @JsonProperty("resource-uuid")
    private String resourceUuid;

    public String getGroupUuid() {
        return groupUuid;
    }

    public void setGroupUuid(String groupUuid) {
        this.groupUuid = groupUuid;
    }

    public String getResourceUuid() {
        return resourceUuid;
    }

    public void setResourceUuid(String resourceUuid) {
        this.resourceUuid = resourceUuid;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Entitlement)) {
            return false;
        }
        Entitlement castOther = (Entitlement) other;
        return new EqualsBuilder().append(groupUuid, castOther.groupUuid).append(resourceUuid, castOther.resourceUuid)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(groupUuid).append(resourceUuid).toHashCode();
    }
}
