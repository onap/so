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

package org.onap.so.openstack.beans;



import java.io.Serializable;
import java.util.List;
import jakarta.xml.bind.annotation.XmlTransient;

public class Subnet implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -530027355187604839L;

    private String subnetName;

    private String neutronId;

    private String subnetId; // value from aai

    private String cidr; // Only field required

    private String gatewayIp;

    private String ipVersion = "4";

    private Boolean enableDHCP = false;

    private Boolean addrFromStart = true;

    private List<HostRoute> hostRoutes;

    private List<Pool> allocationPools;

    private List<String> dnsNameServers;

    private Integer subnetSequence;

    public Integer getSubnetSequence() {
        return subnetSequence;
    }

    public void setSubnetSequence(Integer subnetSequence) {
        this.subnetSequence = subnetSequence;
    }

    public Subnet() {}

    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    public List<Pool> getAllocationPools() {
        return allocationPools;
    }

    /**
     * @return the cidr
     */
    public String getCidr() {
        return cidr;
    }

    /**
     * @return the dnsNames
     */
    public List<String> getDnsNameServers() {
        return dnsNameServers;
    }


    public Boolean getEnableDHCP() {
        return enableDHCP;
    }

    public Boolean getAddrFromStart() {
        return addrFromStart;
    }

    public void setAddrFromStart(Boolean addrFromStart) {
        this.addrFromStart = addrFromStart;
    }

    /**
     * @return the gw
     */
    public String getGatewayIp() {
        return gatewayIp;
    }

    /**
     * @return the hostRoutes
     */
    public List<HostRoute> getHostRoutes() {
        return hostRoutes;
    }

    /**
     * @return the NeutronId
     */
    @XmlTransient
    public String getNeutronId() {
        return neutronId;
    }

    /**
     * @return the ipversion
     */
    public String getIpVersion() {
        return ipVersion;
    }

    /**
     * @return the name
     */
    public String getSubnetId() {
        return subnetId;
    }

    public void setAllocationPools(List<Pool> allocationPools) {
        this.allocationPools = allocationPools;
    }

    /**
     * @param cidr the cidr to set
     */
    public void setCidr(String cidr) {
        this.cidr = cidr;
    }

    /**
     * @param dnsNames the dnsNames to set
     */
    public void setDnsNameServers(List<String> dnsNameServers) {
        this.dnsNameServers = dnsNameServers;
    }

    /**
     * @param enableDHCP the enableDHCP to set
     */
    public void setEnableDHCP(Boolean enableDHCP) {
        this.enableDHCP = enableDHCP;
    }

    /**
     * @param gw the gw to set
     */
    public void setGatewayIp(String gatewayIp) {
        this.gatewayIp = gatewayIp;
    }

    /**
     * @param hostRoutes the hostRoutes to set
     */
    public void setHostRoutes(List<HostRoute> hostRoutes) {
        this.hostRoutes = hostRoutes;
    }

    /**
     * @param neutronId the id to set
     */
    public void setNeutronId(String neutronId) {
        this.neutronId = neutronId;
    }

    /**
     * @param ipversion the ipversion to set
     */
    public void setIpVersion(String ipVersion) {
        this.ipVersion = ipVersion;
    }

    /**
     * @param name the name to set
     */
    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    @Override
    public String toString() {
        return "Subnet [subnetName=" + subnetName + ", neutronId=" + neutronId + ", subnetId=" + subnetId + ", cidr="
                + cidr + ", gatewayIp=" + gatewayIp + ", ipVersion=" + ipVersion + ", enableDHCP=" + enableDHCP
                + ", addrFromStart=" + addrFromStart + ", hostRoutes=" + hostRoutes + ", allocationPools="
                + allocationPools + ", dnsNameServers=" + dnsNameServers + "]";
    }

}
