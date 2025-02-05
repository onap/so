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

@JsonRootName("ctag-assignment")
public class CtagAssignment implements Serializable, ShallowCopy<CtagAssignment> {

    private static final long serialVersionUID = 2314290576523243571L;

    @Id
    @JsonProperty("vlan-id-inner")
    private Long vlanIdInner;

    public Long getVlanIdInner() {
        return vlanIdInner;
    }

    public void setVlanIdInner(Long vlanIdInner) {
        this.vlanIdInner = vlanIdInner;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof CtagAssignment)) {
            return false;
        }
        CtagAssignment castOther = (CtagAssignment) other;
        return new EqualsBuilder().append(vlanIdInner, castOther.vlanIdInner).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(vlanIdInner).toHashCode();
    }


}
