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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;
import jakarta.persistence.Id;

public class Metadatum implements Serializable, ShallowCopy<Metadatum> {

    private static final long serialVersionUID = -2259570072414712965L;

    @Id
    @JsonProperty("metaname")
    private String metaname;
    @JsonProperty("metaval")
    private String metaval;

    public String getMetaname() {
        return metaname;
    }

    public void setMetaname(String metaname) {
        this.metaname = metaname;
    }

    public String getMetaval() {
        return metaval;
    }

    public void setMetaval(String metaval) {
        this.metaval = metaval;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Metadatum)) {
            return false;
        }
        Metadatum castOther = (Metadatum) other;
        return new EqualsBuilder().append(metaname, castOther.metaname).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(metaname).toHashCode();
    }
}
