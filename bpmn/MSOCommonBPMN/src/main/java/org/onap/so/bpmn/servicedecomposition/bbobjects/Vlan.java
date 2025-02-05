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

@JsonRootName("vlan")
public class Vlan implements Serializable, ShallowCopy<Vlan> {

    private static final long serialVersionUID = 1260512753640402946L;

    @Id
    @JsonProperty("vlan-interface")
    private String vlanInterface;
    @JsonProperty("vlan-id-inner")
    private Long vlanIdInner;
    @JsonProperty("vlan-id-outer")
    private Long vlanIdOuter;
    @JsonProperty("speed-value")
    private String speedValue;
    @JsonProperty("speed-units")
    private String speedUnits;
    @JsonProperty("vlan-description")
    private String vlanDescription;
    @JsonProperty("backdoor-connection")
    private String backdoorConnection;
    @JsonProperty("vpn-key")
    private String vpnKey;
    @JsonProperty("orchestration-status")
    private OrchestrationStatus orchestrationStatus;
    @JsonProperty("in-maint")
    private Boolean inMaint;
    @JsonProperty("prov-status")
    private String provStatus;
    @JsonProperty("is-ip-unnumbered")
    private Boolean isIpUnnumbered;
    @JsonProperty("l3-interface-ipv4-address-list")
    private List<L3InterfaceIpv4AddressList> l3InterfaceIpv4AddressList = new ArrayList<>();
    @JsonProperty("l3-interface-ipv6-address-list")
    private List<L3InterfaceIpv6AddressList> l3InterfaceIpv6AddressList = new ArrayList<>();

    public String getVlanInterface() {
        return vlanInterface;
    }

    public void setVlanInterface(String vlanInterface) {
        this.vlanInterface = vlanInterface;
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

    public String getSpeedValue() {
        return speedValue;
    }

    public void setSpeedValue(String speedValue) {
        this.speedValue = speedValue;
    }

    public String getSpeedUnits() {
        return speedUnits;
    }

    public void setSpeedUnits(String speedUnits) {
        this.speedUnits = speedUnits;
    }

    public String getVlanDescription() {
        return vlanDescription;
    }

    public void setVlanDescription(String vlanDescription) {
        this.vlanDescription = vlanDescription;
    }

    public String getBackdoorConnection() {
        return backdoorConnection;
    }

    public void setBackdoorConnection(String backdoorConnection) {
        this.backdoorConnection = backdoorConnection;
    }

    public String getVpnKey() {
        return vpnKey;
    }

    public void setVpnKey(String vpnKey) {
        this.vpnKey = vpnKey;
    }

    public OrchestrationStatus getOrchestrationStatus() {
        return orchestrationStatus;
    }

    public void setOrchestrationStatus(OrchestrationStatus orchestrationStatus) {
        this.orchestrationStatus = orchestrationStatus;
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

    public List<L3InterfaceIpv4AddressList> getL3InterfaceIpv4AddressList() {
        return l3InterfaceIpv4AddressList;
    }

    public List<L3InterfaceIpv6AddressList> getL3InterfaceIpv6AddressList() {
        return l3InterfaceIpv6AddressList;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Vlan)) {
            return false;
        }
        Vlan castOther = (Vlan) other;
        return new EqualsBuilder().append(vlanInterface, castOther.vlanInterface).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(vlanInterface).toHashCode();
    }
}
