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

@JsonRootName("host-route")
public class HostRoute implements Serializable, ShallowCopy<HostRoute> {

    private static final long serialVersionUID = -2468793257174064133L;

    @Id
    @JsonProperty("host-route-id")
    private String hostRouteId;
    @JsonProperty("route-prefix")
    private String routePrefix;
    @JsonProperty("next-hop")
    private String nextHop;
    @JsonProperty("next-hop-type")
    private String nextHopType;

    public String getHostRouteId() {
        return hostRouteId;
    }

    public void setHostRouteId(String hostRouteId) {
        this.hostRouteId = hostRouteId;
    }

    public String getRoutePrefix() {
        return routePrefix;
    }

    public void setRoutePrefix(String routePrefix) {
        this.routePrefix = routePrefix;
    }

    public String getNextHop() {
        return nextHop;
    }

    public void setNextHop(String nextHop) {
        this.nextHop = nextHop;
    }

    public String getNextHopType() {
        return nextHopType;
    }

    public void setNextHopType(String nextHopType) {
        this.nextHopType = nextHopType;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof HostRoute)) {
            return false;
        }
        HostRoute castOther = (HostRoute) other;
        return new EqualsBuilder().append(hostRouteId, castOther.hostRouteId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hostRouteId).toHashCode();
    }
}
