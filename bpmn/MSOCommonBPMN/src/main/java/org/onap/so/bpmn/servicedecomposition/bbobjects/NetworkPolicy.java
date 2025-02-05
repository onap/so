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

package org.onap.so.bpmn.servicedecomposition.bbobjects;

import java.io.Serializable;
import jakarta.persistence.Id;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;

public class NetworkPolicy implements Serializable, ShallowCopy<NetworkPolicy> {

    private static final long serialVersionUID = 8925599588239522447L;

    @Id
    @JsonProperty("network-policy-id")
    private String networkPolicyId;
    @JsonProperty("network-policy-fqdn")
    private String networkPolicyFqdn;
    @JsonProperty("heat-stack-id")
    private String heatStackId;
    @JsonProperty("resource-version")
    private String resourceVersion;

    public String getNetworkPolicyId() {
        return this.networkPolicyId;
    }

    public void setNetworkPolicyId(String networkPolicyId) {
        this.networkPolicyId = networkPolicyId;
    }

    public String getNetworkPolicyFqdn() {
        return this.networkPolicyFqdn;
    }

    public void setNetworkPolicyFqdn(String networkPolicyFqdn) {
        this.networkPolicyFqdn = networkPolicyFqdn;
    }

    public String getHeatStackId() {
        return this.heatStackId;
    }

    public void setHeatStackId(String heatStackId) {
        this.heatStackId = heatStackId;
    }

    public String getResourceVersion() {
        return this.resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof NetworkPolicy)) {
            return false;
        }
        NetworkPolicy castOther = (NetworkPolicy) other;
        return new EqualsBuilder().append(networkPolicyId, castOther.networkPolicyId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(networkPolicyId).toHashCode();
    }
}
