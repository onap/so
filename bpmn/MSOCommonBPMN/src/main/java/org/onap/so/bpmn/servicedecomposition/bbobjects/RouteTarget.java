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

@JsonRootName("route-target")
public class RouteTarget implements Serializable, ShallowCopy<RouteTarget> {
    private static final long serialVersionUID = -4635525992843470461L;

    @Id
    @JsonProperty("global-route-target")
    protected String globalRouteTarget;
    @Id
    @JsonProperty("route-target-role")
    protected String routeTargetRole;
    @JsonProperty("resource-version")
    protected String resourceVersion;

    public String getGlobalRouteTarget() {
        return globalRouteTarget;
    }

    public void setGlobalRouteTarget(String value) {
        this.globalRouteTarget = value;
    }

    public String getRouteTargetRole() {
        return routeTargetRole;
    }

    public void setRouteTargetRole(String value) {
        this.routeTargetRole = value;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String value) {
        this.resourceVersion = value;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof RouteTarget)) {
            return false;
        }
        RouteTarget castOther = (RouteTarget) other;
        return new EqualsBuilder().append(globalRouteTarget, castOther.globalRouteTarget)
                .append(routeTargetRole, castOther.routeTargetRole).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(globalRouteTarget).append(routeTargetRole).toHashCode();
    }
}
