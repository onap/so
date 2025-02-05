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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.persistence.Id;
import static org.apache.commons.lang3.StringUtils.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;

@JsonRootName("vpn-bonding-link")
public class VpnBondingLink implements Serializable, ShallowCopy<VpnBondingLink> {

    private static final long serialVersionUID = -8355973761714642727L;

    @Id
    @JsonProperty("vpn-bonding-link-id")
    private String vpnBondingLinkId;

    @JsonProperty("configurations")
    private List<Configuration> configurations = new ArrayList<>();

    @JsonProperty("service-proxies")
    private List<ServiceProxy> serviceProxies = new ArrayList<>();

    public String getVpnBondingLinkId() {
        return vpnBondingLinkId;
    }

    public void setVpnBondingLinkId(String vpnBondingLinkId) {
        this.vpnBondingLinkId = vpnBondingLinkId;
    }

    public List<Configuration> getConfigurations() {
        return configurations;
    }

    public List<ServiceProxy> getServiceProxies() {
        return serviceProxies;
    }

    public ServiceProxy getServiceProxy(String id) {
        ServiceProxy serviceProxy = null;
        for (ServiceProxy s : serviceProxies) {
            if (s.getId().equals(id)) {
                serviceProxy = s;
            }
        }
        return serviceProxy;
    }

    // TODO temp solution until references are updated to use getConfigurationByType
    public Configuration getVnrConfiguration() {
        Configuration configuration = null;
        for (Configuration c : configurations) {
            if (containsIgnoreCase(c.getConfigurationType(), "vlan")
                    || containsIgnoreCase(c.getConfigurationType(), "vnr")) {
                configuration = c;
            }
        }
        return configuration;
    }

    // TODO temp solution until references are updatedd
    public void setVnrConfiguration(Configuration vnrConfiguration) {
        if (vnrConfiguration.getConfigurationType() == null) {
            vnrConfiguration.setConfigurationType("vlan");
        }
        configurations.add(vnrConfiguration);
    }

    // TODO temp solution until references are updated to use getConfigurationByType
    public Configuration getVrfConfiguration() {
        Configuration configuration = null;
        for (Configuration c : configurations) {
            if (containsIgnoreCase(c.getConfigurationType(), "vrf")) {
                configuration = c;
            }
        }
        return configuration;
    }

    // TODO temp solution until references are updated
    public void setVrfConfiguration(Configuration vrfConfiguration) {
        if (vrfConfiguration.getConfigurationType() == null) {
            vrfConfiguration.setConfigurationType("vrf");
        }
        configurations.add(vrfConfiguration);
    }

    // TODO temp solution until references are updated to use getServiceProxyByType
    public ServiceProxy getInfrastructureServiceProxy() {
        ServiceProxy serviceProxy = null;
        for (ServiceProxy sp : serviceProxies) {
            if (sp.getType().equals("infrastructure")) {
                serviceProxy = sp;
            }
        }
        return serviceProxy;
    }

    // TODO temp solution until references are updated
    public void setInfrastructureServiceProxy(ServiceProxy infrastructureServiceProxy) {
        infrastructureServiceProxy.setType("infrastructure");
        serviceProxies.add(infrastructureServiceProxy);
    }

    // TODO temp solution until references are updated to use getServiceProxyByType
    public ServiceProxy getTransportServiceProxy() {
        ServiceProxy serviceProxy = null;
        for (ServiceProxy sp : serviceProxies) {
            if (sp != null) {
                if (sp.getType().equals("transport")) {
                    serviceProxy = sp;
                }
            }
        }
        return serviceProxy;
    }

    // TODO temp solution until references are updated
    public void setTransportServiceProxy(ServiceProxy transportServiceProxy) {
        transportServiceProxy.setType("transport");
        serviceProxies.add(transportServiceProxy);
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof VpnBondingLink)) {
            return false;
        }
        VpnBondingLink castOther = (VpnBondingLink) other;
        return new EqualsBuilder().append(vpnBondingLinkId, castOther.vpnBondingLinkId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(vpnBondingLinkId).toHashCode();
    }
}
