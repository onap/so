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

public class RouteTableReference implements Serializable, ShallowCopy<RouteTableReference> {

    private static final long serialVersionUID = -698474994443040491L;

    @Id
    @JsonProperty("route-table-reference-id")
    private String routeTableReferenceId;
    @JsonProperty("route-table-reference-fqdn")
    private String routeTableReferenceFqdn;
    @JsonProperty("resource-version")
    private String resourceVersion;

    public String getRouteTableReferenceId() {
        return routeTableReferenceId;
    }

    public void setRouteTableReferenceId(String routeTableReferenceId) {
        this.routeTableReferenceId = routeTableReferenceId;
    }

    public String getRouteTableReferenceFqdn() {
        return routeTableReferenceFqdn;
    }

    public void setRouteTableReferenceFqdn(String routeTableReferenceFqdn) {
        this.routeTableReferenceFqdn = routeTableReferenceFqdn;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof RouteTableReference)) {
            return false;
        }
        RouteTableReference castOther = (RouteTableReference) other;
        return new EqualsBuilder().append(routeTableReferenceId, castOther.routeTableReferenceId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(routeTableReferenceId).toHashCode();
    }
}
