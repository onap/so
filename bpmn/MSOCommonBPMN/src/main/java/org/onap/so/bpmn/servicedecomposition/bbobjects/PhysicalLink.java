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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("physical-link")
public class PhysicalLink implements Serializable, ShallowCopy<PhysicalLink> {

    private static final long serialVersionUID = -6378347998443741227L;

    @Id
    @JsonProperty("link-name")
    private String linkName;
    @JsonProperty("interface-name")
    private String interfaceName;
    @JsonProperty("service-provider-name")
    private String serviceProviderName;
    @JsonProperty("circuit-id")
    private String circuitId;
    @JsonProperty("management-option")
    private String managementOption;
    @JsonProperty("bandwidth-up")
    private Integer bandwidthUp;
    @JsonProperty("bandwidth-down")
    private Integer bandwidthDown;
    @JsonProperty("bandwidth-units")
    private String bandwidthUnits;
    @JsonProperty("wan-port")
    private String wanPort;


    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }


    public String getServiceProviderName() {
        return serviceProviderName;
    }


    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public String getCircuitId() {
        return circuitId;
    }

    public void setCircuitId(String circuitId) {
        this.circuitId = circuitId;
    }

    public String getManagementOption() {
        return managementOption;
    }

    public void setManagementOption(String managementOption) {
        this.managementOption = managementOption;
    }

    public Integer getBandwidthUp() {
        return bandwidthUp;
    }

    public void setBandwidthUp(Integer bandwidthUp) {
        this.bandwidthUp = bandwidthUp;
    }

    public Integer getBandwidthDown() {
        return bandwidthDown;
    }

    public void setBandwidthDown(Integer bandwidthDown) {
        this.bandwidthDown = bandwidthDown;
    }

    public String getBandwidthUnits() {
        return bandwidthUnits;
    }

    public void setBandwidthUnits(String bandwidthUnits) {
        this.bandwidthUnits = bandwidthUnits;
    }

    public String getWanPort() {
        return wanPort;
    }

    public void setWanPort(String wanPort) {
        this.wanPort = wanPort;
    }


    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof PhysicalLink)) {
            return false;
        }
        PhysicalLink castOther = (PhysicalLink) other;
        return new EqualsBuilder().append(linkName, castOther.linkName).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(linkName).toHashCode();
    }

}
