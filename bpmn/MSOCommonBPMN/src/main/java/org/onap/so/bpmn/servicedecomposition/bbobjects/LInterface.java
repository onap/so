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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;
import jakarta.persistence.Id;

@JsonRootName("l-interface")
public class LInterface implements Serializable, ShallowCopy<LInterface> {

    private static final long serialVersionUID = 5629921809747079453L;

    @Id
    @JsonProperty("interface-name")
    private String interfaceName;
    @JsonProperty("interface-role")
    private String interfaceRole;
    @JsonProperty("v6-wan-link-ip")
    private String v6WanLinkIp;
    @JsonProperty("self-link")
    private String selflink;
    @JsonProperty("interface-id")
    private String interfaceId;
    @JsonProperty("macaddr")
    private String macaddr;
    @JsonProperty("network-name")
    private String networkName;
    @JsonProperty("management-option")
    private String managementOption;
    @JsonProperty("interface-description")
    private String interfaceDescription;
    @JsonProperty("is-port-mirrored")
    private Boolean isPortMirrored;
    @JsonProperty("in-maint")
    private Boolean inMaint;
    @JsonProperty("prov-status")
    private String provStatus;
    @JsonProperty("is-ip-unnumbered")
    private Boolean isIpUnnumbered;
    @JsonProperty("allowed-address-pairs")
    private String allowedAddressPairs;
    @JsonProperty("vlans")
    private List<Vlan> vlans = new ArrayList<>();
    @JsonProperty("sriov-vfs")
    private List<SriovVf> sriovVfs = new ArrayList<>();
    @JsonProperty("l-interfaces")
    private List<LInterface> lInterfaces = new ArrayList<>();
    @JsonProperty("l3-interface-ipv4-address-list")
    private List<L3InterfaceIpv4AddressList> l3InterfaceIpv4AddressList = new ArrayList<>();
    @JsonProperty("l3-interface-ipv6-address-list")
    private List<L3InterfaceIpv6AddressList> l3InterfaceIpv6AddressList = new ArrayList<>();

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getInterfaceRole() {
        return interfaceRole;
    }

    public void setInterfaceRole(String interfaceRole) {
        this.interfaceRole = interfaceRole;
    }

    public String getV6WanLinkIp() {
        return v6WanLinkIp;
    }

    public void setV6WanLinkIp(String v6WanLinkIp) {
        this.v6WanLinkIp = v6WanLinkIp;
    }

    public String getSelflink() {
        return selflink;
    }

    public void setSelflink(String selflink) {
        this.selflink = selflink;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getMacaddr() {
        return macaddr;
    }

    public void setMacaddr(String macaddr) {
        this.macaddr = macaddr;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getManagementOption() {
        return managementOption;
    }

    public void setManagementOption(String managementOption) {
        this.managementOption = managementOption;
    }

    public String getInterfaceDescription() {
        return interfaceDescription;
    }

    public void setInterfaceDescription(String interfaceDescription) {
        this.interfaceDescription = interfaceDescription;
    }

    public Boolean isIsPortMirrored() {
        return isPortMirrored;
    }

    public void setPortMirrored(Boolean isPortMirrored) {
        this.isPortMirrored = isPortMirrored;
    }

    public Boolean isInMaint() {
        return inMaint;
    }

    public void setInMaint(boolean inMaint) {
        this.inMaint = inMaint;
    }

    public String getProvStatus() {
        return provStatus;
    }

    public void setProvStatus(String provStatus) {
        this.provStatus = provStatus;
    }

    public Boolean isIsIpUnnumbered() {
        return isIpUnnumbered;
    }

    public void setIpUnnumbered(Boolean isIpUnnumbered) {
        this.isIpUnnumbered = isIpUnnumbered;
    }

    public String getAllowedAddressPairs() {
        return allowedAddressPairs;
    }

    public void setAllowedAddressPairs(String allowedAddressPairs) {
        this.allowedAddressPairs = allowedAddressPairs;
    }

    public List<Vlan> getVlans() {
        return vlans;
    }

    public List<SriovVf> getSriovVfs() {
        return sriovVfs;
    }

    public List<LInterface> getlInterfaces() {
        return lInterfaces;
    }

    public List<L3InterfaceIpv4AddressList> getL3InterfaceIpv4AddressList() {
        return l3InterfaceIpv4AddressList;
    }

    public List<L3InterfaceIpv6AddressList> getL3InterfaceIpv6AddressList() {
        return l3InterfaceIpv6AddressList;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof LInterface)) {
            return false;
        }
        LInterface castOther = (LInterface) other;
        return new EqualsBuilder().append(interfaceName, castOther.interfaceName).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(interfaceName).toHashCode();
    }
}
