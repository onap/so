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

@JsonRootName("forwarder-evc")
public class ForwarderEvc implements Serializable, ShallowCopy<ForwarderEvc> {

    private static final long serialVersionUID = 3822241337439817708L;

    @Id
    @JsonProperty("forwarder-evc-id")
    private String forwarderEvcId;
    @JsonProperty("circuit-id")
    private String circuitId;
    @JsonProperty("ivlan")
    private String ivlan;
    @JsonProperty("svlan")
    private String svlan;
    @JsonProperty("cvlan")
    private String cvlan;

    public String getForwarderEvcId() {
        return forwarderEvcId;
    }

    public void setForwarderEvcId(String forwarderEvcId) {
        this.forwarderEvcId = forwarderEvcId;
    }

    public String getCircuitId() {
        return circuitId;
    }

    public void setCircuitId(String circuitId) {
        this.circuitId = circuitId;
    }

    public String getIvlan() {
        return ivlan;
    }

    public void setIvlan(String ivlan) {
        this.ivlan = ivlan;
    }

    public String getSvlan() {
        return svlan;
    }

    public void setSvlan(String svlan) {
        this.svlan = svlan;
    }

    public String getCvlan() {
        return cvlan;
    }

    public void setCvlan(String cvlan) {
        this.cvlan = cvlan;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ForwarderEvc)) {
            return false;
        }
        ForwarderEvc castOther = (ForwarderEvc) other;
        return new EqualsBuilder().append(forwarderEvcId, castOther.forwarderEvcId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(forwarderEvcId).toHashCode();
    }
}
