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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;
import jakarta.persistence.Id;

@JsonRootName("subnet")
public class Subnet implements Serializable, ShallowCopy<Subnet> {

    private static final long serialVersionUID = -6789344717555598319L;

    @Id
    @JsonProperty("subnet-id")
    private String subnetId;
    @JsonProperty("subnet-name")
    private String subnetName;
    @JsonProperty("neutron-subnet-id")
    private String neutronSubnetId;
    @JsonProperty("gateway-address")
    private String gatewayAddress;
    @JsonProperty("network-start-address")
    private String networkStartAddress;
    @JsonProperty("cidr-mask")
    private String cidrMask;
    @JsonProperty("ip-version")
    private String ipVersion;
    @JsonProperty("orchestration-status")
    private OrchestrationStatus orchestrationStatus;
    @JsonProperty("dhcp-enabled")
    private Boolean dhcpEnabled;
    @JsonProperty("dhcp-start")
    private String dhcpStart;
    @JsonProperty("dhcp-end")
    private String dhcpEnd;
    @JsonProperty("subnet-role")
    private String subnetRole;
    @JsonProperty("ip-assignment-direction")
    private String ipAssignmentDirection;
    @JsonProperty("subnet-sequence")
    private Integer subnetSequence;
    @JsonProperty("host-routes")
    private List<HostRoute> hostRoutes = new ArrayList<>();

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    public String getNeutronSubnetId() {
        return neutronSubnetId;
    }

    public void setNeutronSubnetId(String neutronSubnetId) {
        this.neutronSubnetId = neutronSubnetId;
    }

    public String getGatewayAddress() {
        return gatewayAddress;
    }

    public void setGatewayAddress(String gatewayAddress) {
        this.gatewayAddress = gatewayAddress;
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

    public OrchestrationStatus getOrchestrationStatus() {
        return orchestrationStatus;
    }

    public void setOrchestrationStatus(OrchestrationStatus orchestrationStatus) {
        this.orchestrationStatus = orchestrationStatus;
    }

    public Boolean isDhcpEnabled() {
        return dhcpEnabled;
    }

    public void setDhcpEnabled(Boolean dhcpEnabled) {
        this.dhcpEnabled = dhcpEnabled;
    }

    public String getDhcpStart() {
        return dhcpStart;
    }

    public void setDhcpStart(String dhcpStart) {
        this.dhcpStart = dhcpStart;
    }

    public String getDhcpEnd() {
        return dhcpEnd;
    }

    public void setDhcpEnd(String dhcpEnd) {
        this.dhcpEnd = dhcpEnd;
    }

    public String getSubnetRole() {
        return subnetRole;
    }

    public void setSubnetRole(String subnetRole) {
        this.subnetRole = subnetRole;
    }

    public String getIpAssignmentDirection() {
        return ipAssignmentDirection;
    }

    public void setIpAssignmentDirection(String ipAssignmentDirection) {
        this.ipAssignmentDirection = ipAssignmentDirection;
    }

    public Integer getSubnetSequence() {
        return subnetSequence;
    }

    public void setSubnetSequence(Integer subnetSequence) {
        this.subnetSequence = subnetSequence;
    }

    public List<HostRoute> getHostRoutes() {
        return hostRoutes;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Subnet)) {
            return false;
        }
        Subnet castOther = (Subnet) other;
        return new EqualsBuilder().append(subnetId, castOther.subnetId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(subnetId).toHashCode();
    }
}
