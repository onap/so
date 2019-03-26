/*
 * Copyright (C) 2019 Verizon. All Rights Reserved Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onap.so.adapters.vfc.model;

import java.util.List;

public class NfpRule {
    private String etherDestinationAddress;
    private String etherSourceAddress;

    private enum etherType {
        IPV4, IPV6
    };

    private List<String> vlanTag;

    private enum protocol {
        TCP, UDP, ICMP
    }

    private String dscp;
    private PortRange sourcePortRange;
    private PortRange destinationPortRange;
    private String sourceIpAddressPrefix;
    private String destinationIpAddressPrefix;
    private List<Mask> extendedCriteria;

    public String getEtherDestinationAddress() {
        return etherDestinationAddress;
    }

    public void setEtherDestinationAddress(String etherDestinationAddress) {
        this.etherDestinationAddress = etherDestinationAddress;
    }

    public String getEtherSourceAddress() {
        return etherSourceAddress;
    }

    public void setEtherSourceAddress(String etherSourceAddress) {
        this.etherSourceAddress = etherSourceAddress;
    }

    public List<String> getVlanTag() {
        return vlanTag;
    }

    public void setVlanTag(List<String> vlanTag) {
        this.vlanTag = vlanTag;
    }

    public String getDscp() {
        return dscp;
    }

    public void setDscp(String dscp) {
        this.dscp = dscp;
    }

    public PortRange getSourcePortRange() {
        return sourcePortRange;
    }

    public void setSourcePortRange(PortRange sourcePortRange) {
        this.sourcePortRange = sourcePortRange;
    }

    public PortRange getDestinationPortRange() {
        return destinationPortRange;
    }

    public void setDestinationPortRange(PortRange destinationPortRange) {
        this.destinationPortRange = destinationPortRange;
    }

    public String getSourceIpAddressPrefix() {
        return sourceIpAddressPrefix;
    }

    public void setSourceIpAddressPrefix(String sourceIpAddressPrefix) {
        this.sourceIpAddressPrefix = sourceIpAddressPrefix;
    }

    public String getDestinationIpAddressPrefix() {
        return destinationIpAddressPrefix;
    }

    public void setDestinationIpAddressPrefix(String destinationIpAddressPrefix) {
        this.destinationIpAddressPrefix = destinationIpAddressPrefix;
    }

    public List<Mask> getExtendedCriteria() {
        return extendedCriteria;
    }

    public void setExtendedCriteria(List<Mask> extendedCriteria) {
        this.extendedCriteria = extendedCriteria;
    }
}
