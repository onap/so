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

@JsonRootName("l3-interface-ipv4-address-list")
public class L3InterfaceIpv4AddressList implements Serializable, ShallowCopy<L3InterfaceIpv4AddressList> {

    private static final long serialVersionUID = -1566884527411610879L;

    @Id
    @JsonProperty("l3-interface-ipv4-address")
    private String l3InterfaceIpv4Address;
    @JsonProperty("l3-interface-ipv4-prefix-length")
    private Long l3InterfaceIpv4PrefixLength;
    @JsonProperty("vlan-id-inner")
    private Long vlanIdInner;
    @JsonProperty("vlan-id-outer")
    private Long vlanIdOuter;
    @JsonProperty("is-floating")
    private Boolean isFloating;
    @JsonProperty("neutron-network-id")
    private String neutronNetworkId;
    @JsonProperty("neutron-subnet-id")
    private String neutronSubnetId;

    public String getL3InterfaceIpv4Address() {
        return l3InterfaceIpv4Address;
    }

    public void setL3InterfaceIpv4Address(String l3InterfaceIpv4Address) {
        this.l3InterfaceIpv4Address = l3InterfaceIpv4Address;
    }

    public Long getL3InterfaceIpv4PrefixLength() {
        return l3InterfaceIpv4PrefixLength;
    }

    public void setL3InterfaceIpv4PrefixLength(Long l3InterfaceIpv4PrefixLength) {
        this.l3InterfaceIpv4PrefixLength = l3InterfaceIpv4PrefixLength;
    }

    public Long getVlanIdInner() {
        return vlanIdInner;
    }

    public void setVlanIdInner(Long vlanIdInner) {
        this.vlanIdInner = vlanIdInner;
    }

    public Long getVlanIdOuter() {
        return vlanIdOuter;
    }

    public void setVlanIdOuter(Long vlanIdOuter) {
        this.vlanIdOuter = vlanIdOuter;
    }

    public Boolean getIsFloating() {
        return isFloating;
    }

    public void setIsFloating(Boolean isFloating) {
        this.isFloating = isFloating;
    }

    public String getNeutronNetworkId() {
        return neutronNetworkId;
    }

    public void setNeutronNetworkId(String neutronNetworkId) {
        this.neutronNetworkId = neutronNetworkId;
    }

    public String getNeutronSubnetId() {
        return neutronSubnetId;
    }

    public void setNeutronSubnetId(String neutronSubnetId) {
        this.neutronSubnetId = neutronSubnetId;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof L3InterfaceIpv4AddressList)) {
            return false;
        }
        L3InterfaceIpv4AddressList castOther = (L3InterfaceIpv4AddressList) other;
        return new EqualsBuilder().append(l3InterfaceIpv4Address, castOther.l3InterfaceIpv4Address).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(l3InterfaceIpv4Address).toHashCode();
    }
}
