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
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

@JsonRootName("aggregate-route")
public class AggregateRoute implements Serializable, ShallowCopy<AggregateRoute> {

    private static final long serialVersionUID = -1059128545462701696L;

    @Id
    @JsonProperty("route-id")
    private String routeId;
    @JsonProperty("network-start-address")
    private String networkStartAddress;
    @JsonProperty("cidr-mask")
    private String cidrMask;
    @JsonProperty("ip-version")
    private String ipVersion;


    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getNetworkStartAddress() {
        return networkStartAddress;
    }

    public void setNetworkStartAddress(String networkStartAddress) {
        this.networkStartAddress = networkStartAddress;
    }

    public String getCidrMask() {
        return cidrMask;
    }

    public void setCidrMask(String cidrMask) {
        this.cidrMask = cidrMask;
    }

    public String getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(String ipVersion) {
        this.ipVersion = ipVersion;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof AggregateRoute)) {
            return false;
        }
        AggregateRoute castOther = (AggregateRoute) other;
        return new EqualsBuilder().append(routeId, castOther.routeId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(routeId).toHashCode();
    }


}
