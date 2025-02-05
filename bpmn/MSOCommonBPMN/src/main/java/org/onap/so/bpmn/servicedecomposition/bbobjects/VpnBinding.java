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
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Id;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;


@JsonRootName("vpn-binding")
public class VpnBinding implements Serializable, ShallowCopy<VpnBinding> {
    private static final long serialVersionUID = 3283413795353486924L;

    @Id
    @JsonProperty("vpn-id")
    private String vpnId;
    @JsonProperty("vpn-name")
    private String vpnName;
    @JsonProperty("vpn-platform")
    private String vpnPlatform;
    @JsonProperty("vpn-type")
    private String vpnType;
    @JsonProperty("vpn-region")
    private String vpnRegion;
    @JsonProperty("customer-vpn-id")
    private String customerVpnId;
    @JsonProperty("route-distinguisher")
    private String routeDistinguisher;
    @JsonProperty("resource-version")
    private String resourceVersion;
    @JsonProperty("route-targets")
    private List<RouteTarget> routeTargets = new ArrayList<>();

    public String getVpnId() {
        return vpnId;
    }

    public void setVpnId(String value) {
        this.vpnId = value;
    }

    public String getVpnName() {
        return vpnName;
    }

    public void setVpnName(String value) {
        this.vpnName = value;
    }

    public String getVpnPlatform() {
        return vpnPlatform;
    }

    public void setVpnPlatform(String value) {
        this.vpnPlatform = value;
    }

    public String getVpnType() {
        return vpnType;
    }

    public void setVpnType(String value) {
        this.vpnType = value;
    }

    public String getVpnRegion() {
        return vpnRegion;
    }

    public void setVpnRegion(String value) {
        this.vpnRegion = value;
    }

    public String getCustomerVpnId() {
        return customerVpnId;
    }

    public void setCustomerVpnId(String value) {
        this.customerVpnId = value;
    }

    public String getRouteDistinguisher() {
        return routeDistinguisher;
    }

    public void setRouteDistinguisher(String value) {
        this.routeDistinguisher = value;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String value) {
        this.resourceVersion = value;
    }

    public List<RouteTarget> getRouteTargets() {
        return routeTargets;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof VpnBinding)) {
            return false;
        }
        VpnBinding castOther = (VpnBinding) other;
        return new EqualsBuilder().append(vpnId, castOther.vpnId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(vpnId).toHashCode();
    }
}
