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

@JsonRootName("sriov-vf")
public class SriovVf implements Serializable, ShallowCopy<SriovVf> {

    private static final long serialVersionUID = -7790331637399859914L;

    @Id
    @JsonProperty("pci-id")
    private String pciId;
    @JsonProperty("vf-vlan-filter")
    private String vfVlanFilter;
    @JsonProperty("vf-mac-filter")
    private String vfMacFilter;
    @JsonProperty("vf-vlan-strip")
    private Boolean vfVlanStrip;
    @JsonProperty("vf-vlan-anti-spoof-check")
    private Boolean vfVlanAntiSpoofCheck;
    @JsonProperty("vf-mac-anti-spoof-check")
    private Boolean vfMacAntiSpoofCheck;
    @JsonProperty("vf-mirrors")
    private String vfMirrors;
    @JsonProperty("vf-broadcast-allow")
    private Boolean vfBroadcastAllow;
    @JsonProperty("vf-unknown-multicast-allow")
    private Boolean vfUnknownMulticastAllow;
    @JsonProperty("vf-unknown-unicast-allow")
    private Boolean vfUnknownUnicastAllow;
    @JsonProperty("vf-insert-stag")
    private Boolean vfInsertStag;
    @JsonProperty("vf-link-status")
    private String vfLinkStatus;
    @JsonProperty("neutron-network-id")
    private String neutronNetworkId;

    public String getPciId() {
        return pciId;
    }

    public void setPciId(String pciId) {
        this.pciId = pciId;
    }

    public String getVfVlanFilter() {
        return vfVlanFilter;
    }

    public void setVfVlanFilter(String vfVlanFilter) {
        this.vfVlanFilter = vfVlanFilter;
    }

    public String getVfMacFilter() {
        return vfMacFilter;
    }

    public void setVfMacFilter(String vfMacFilter) {
        this.vfMacFilter = vfMacFilter;
    }

    public Boolean getVfVlanStrip() {
        return vfVlanStrip;
    }

    public void setVfVlanStrip(Boolean vfVlanStrip) {
        this.vfVlanStrip = vfVlanStrip;
    }

    public Boolean getVfVlanAntiSpoofCheck() {
        return vfVlanAntiSpoofCheck;
    }

    public void setVfVlanAntiSpoofCheck(Boolean vfVlanAntiSpoofCheck) {
        this.vfVlanAntiSpoofCheck = vfVlanAntiSpoofCheck;
    }

    public Boolean getVfMacAntiSpoofCheck() {
        return vfMacAntiSpoofCheck;
    }

    public void setVfMacAntiSpoofCheck(Boolean vfMacAntiSpoofCheck) {
        this.vfMacAntiSpoofCheck = vfMacAntiSpoofCheck;
    }

    public String getVfMirrors() {
        return vfMirrors;
    }

    public void setVfMirrors(String vfMirrors) {
        this.vfMirrors = vfMirrors;
    }

    public Boolean getVfBroadcastAllow() {
        return vfBroadcastAllow;
    }

    public void setVfBroadcastAllow(Boolean vfBroadcastAllow) {
        this.vfBroadcastAllow = vfBroadcastAllow;
    }

    public Boolean getVfUnknownMulticastAllow() {
        return vfUnknownMulticastAllow;
    }

    public void setVfUnknownMulticastAllow(Boolean vfUnknownMulticastAllow) {
        this.vfUnknownMulticastAllow = vfUnknownMulticastAllow;
    }

    public Boolean getVfUnknownUnicastAllow() {
        return vfUnknownUnicastAllow;
    }

    public void setVfUnknownUnicastAllow(Boolean vfUnknownUnicastAllow) {
        this.vfUnknownUnicastAllow = vfUnknownUnicastAllow;
    }

    public Boolean getVfInsertStag() {
        return vfInsertStag;
    }

    public void setVfInsertStag(Boolean vfInsertStag) {
        this.vfInsertStag = vfInsertStag;
    }

    public String getVfLinkStatus() {
        return vfLinkStatus;
    }

    public void setVfLinkStatus(String vfLinkStatus) {
        this.vfLinkStatus = vfLinkStatus;
    }

    public String getNeutronNetworkId() {
        return neutronNetworkId;
    }

    public void setNeutronNetworkId(String neutronNetworkId) {
        this.neutronNetworkId = neutronNetworkId;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof SriovVf)) {
            return false;
        }
        SriovVf castOther = (SriovVf) other;
        return new EqualsBuilder().append(pciId, castOther.pciId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(pciId).toHashCode();
    }
}
