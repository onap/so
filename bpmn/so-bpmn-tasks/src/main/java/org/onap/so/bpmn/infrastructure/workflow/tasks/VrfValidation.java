/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import java.util.List;
import java.util.Optional;
import org.onap.aai.domain.yang.L3Network;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.db.catalog.beans.ConfigurationResourceCustomization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VrfValidation {

    @Autowired
    protected BBInputSetupUtils bbInputSetupUtils;

    public void setBbInputSetupUtils(BBInputSetupUtils bbInputSetupUtils) {
        this.bbInputSetupUtils = bbInputSetupUtils;
    }

    protected void vrfServiceValidation(org.onap.so.db.catalog.beans.Service service)
            throws VrfBondingServiceException {
        if (!"BONDING".equalsIgnoreCase(service.getServiceType())
                || !"INFRASTRUCTURE-VPN".equalsIgnoreCase(service.getServiceRole())) {
            throw new VrfBondingServiceException("Service: " + service.getModelName()
                    + " does not have service type of BONDING and does not have service role of INFRASTRUCTURE-VPN");
        }
    }

    protected void vrfCatalogDbChecks(org.onap.so.db.catalog.beans.Service service) throws VrfBondingServiceException {
        ConfigurationResourceCustomization configuration = getVrfConfiguration(service);
        if (configuration == null || configuration.getServiceProxyResourceCustomization() == null
                || configuration.getServiceProxyResourceCustomization().getSourceService() == null
                || !configuration.getServiceProxyResourceCustomization().getSourceService().getServiceType()
                        .equalsIgnoreCase("TRANSPORT")) {
            throw new VrfBondingServiceException("Service: " + service.getModelName()
                    + " does not have a configuration of type VRF-ENTRY and role INFRASTRUCTURE-CLOUD-VPN"
                    + ", and serviceProxy that does not have source service that has a serviceType of TRANSPORT)");
        }
    }

    protected ConfigurationResourceCustomization getVrfConfiguration(org.onap.so.db.catalog.beans.Service service) {
        for (ConfigurationResourceCustomization configuration : service.getConfigurationCustomizations()) {
            if (configuration.getType() != null && configuration.getType().equalsIgnoreCase("VRF-ENTRY")
                    && configuration.getRole() != null
                    && configuration.getRole().equalsIgnoreCase("INFRASTRUCTURE-CLOUD-VPN")) {
                return configuration;
            }
        }
        return null;
    }

    protected void aaiVpnBindingValidation(String relatedVpnId, org.onap.aai.domain.yang.VpnBinding aaiVpnBinding)
            throws VrfBondingServiceException {
        if (aaiVpnBinding == null) {
            throw new VrfBondingServiceException("The infrastructure vpn " + relatedVpnId + " does not exist in A&AI.");
        } else if (aaiVpnBinding.getVpnType() != null
                && !aaiVpnBinding.getVpnType().equalsIgnoreCase("SERVICE-INFRASTRUCTURE")) {
            throw new VrfBondingServiceException(
                    "VpnBinding: " + relatedVpnId + " does not have a vpn type of SERVICE-INFRASTRUCTURE.");
        }
    }

    protected void aaiAggregateRouteValidation(org.onap.aai.domain.yang.L3Network aaiLocalNetwork)
            throws VrfBondingServiceException {
        if (aaiLocalNetwork.getAggregateRoutes() == null
                || aaiLocalNetwork.getAggregateRoutes().getAggregateRoute() == null) {
            return;
        }
        if (aaiLocalNetwork.getAggregateRoutes().getAggregateRoute().size() == 1 && !aaiLocalNetwork
                .getAggregateRoutes().getAggregateRoute().get(0).getIpVersion().equalsIgnoreCase("4")) {
            throw new VrfBondingServiceException("LocalNetwork: " + aaiLocalNetwork.getNetworkId()
                    + " has 1 aggregate route but the Ip version of aggregate route is : "
                    + aaiLocalNetwork.getAggregateRoutes().getAggregateRoute().get(0).getIpVersion() + " and is not 4");
        } else if (aaiLocalNetwork.getAggregateRoutes().getAggregateRoute().size() == 2
                && !ipVersionValidation(aaiLocalNetwork.getAggregateRoutes().getAggregateRoute().get(0).getIpVersion(),
                        aaiLocalNetwork.getAggregateRoutes().getAggregateRoute().get(1).getIpVersion())) {
            throw new VrfBondingServiceException("LocalNetwork: " + aaiLocalNetwork.getNetworkId()
                    + " has 2 aggregate routes but the combination of the Ip versions for the aggregate routes did not match the ip version of one of them to be 4 and one to be 6");
        } else if (aaiLocalNetwork.getAggregateRoutes().getAggregateRoute().size() > 2) {
            throw new VrfBondingServiceException(
                    "LocalNetwork: " + aaiLocalNetwork.getNetworkId() + " either has more than 2 aggregate routes");
        }
    }

    protected void aaiNetworkValidation(String relatedNetworkid, org.onap.aai.domain.yang.L3Network aaiLocalNetwork)
            throws VrfBondingServiceException {
        if (aaiLocalNetwork == null) {
            throw new VrfBondingServiceException("The local network " + relatedNetworkid + " does not exist in A&AI.");
        }
    }

    protected void aaiSubnetValidation(org.onap.aai.domain.yang.L3Network aaiLocalNetwork)
            throws VrfBondingServiceException {
        if (aaiLocalNetwork.getSubnets() == null || aaiLocalNetwork.getSubnets().getSubnet() == null) {
            throw new VrfBondingServiceException("LocalNetwork: " + aaiLocalNetwork.getNetworkId() + " has no subnets");
        } else if (aaiLocalNetwork.getSubnets().getSubnet().size() == 1
                && !aaiLocalNetwork.getSubnets().getSubnet().get(0).getIpVersion().equalsIgnoreCase("4")) {
            throw new VrfBondingServiceException("LocalNetwork: " + aaiLocalNetwork.getNetworkId()
                    + " has 1 subnet but the Ip version of subnet is : "
                    + aaiLocalNetwork.getSubnets().getSubnet().get(0).getIpVersion() + " and is not 4");
        } else if (aaiLocalNetwork.getSubnets().getSubnet().size() == 2
                && !ipVersionValidation(aaiLocalNetwork.getSubnets().getSubnet().get(0).getIpVersion(),
                        aaiLocalNetwork.getSubnets().getSubnet().get(1).getIpVersion())) {
            throw new VrfBondingServiceException("LocalNetwork: " + aaiLocalNetwork.getNetworkId()
                    + " has 2 subnets but the combination of the Ip versions for the subnets did not match the ip version of one of them to be 4 and one to be 6");
        } else if (aaiLocalNetwork.getSubnets().getSubnet().isEmpty()
                || aaiLocalNetwork.getSubnets().getSubnet().size() > 2) {
            throw new VrfBondingServiceException("LocalNetwork: " + aaiLocalNetwork.getNetworkId()
                    + " either has no subnets or more than 2 subnets");
        }
    }

    protected boolean ipVersionValidation(String ipVersion1, String ipVersion2) {
        return (ipVersion1.equalsIgnoreCase("4") && ipVersion2.equalsIgnoreCase("6"))
                || (ipVersion1.equalsIgnoreCase("6") && ipVersion2.equalsIgnoreCase("4"));
    }

    protected void aaiRouteTargetValidation(L3Network aaiLocalNetwork) throws VrfBondingServiceException {
        AAIResultWrapper networkWrapper = new AAIResultWrapper(aaiLocalNetwork);
        if (networkWrapper.getRelationships().isPresent()) {
            List<AAIResourceUri> vpnBindingUris =
                    networkWrapper.getRelationships().get().getRelatedUris(AAIObjectType.VPN_BINDING);
            if (!vpnBindingUris.isEmpty()) {
                Optional<org.onap.aai.domain.yang.VpnBinding> vpnBindingOp =
                        bbInputSetupUtils.getAAIResourceDepthOne(vpnBindingUris.get(0))
                                .asBean(org.onap.aai.domain.yang.VpnBinding.class);
                if (vpnBindingOp.isPresent()) {
                    org.onap.aai.domain.yang.VpnBinding vpnBinding = vpnBindingOp.get();
                    if (vpnBinding.getRouteTargets() != null
                            && !vpnBinding.getRouteTargets().getRouteTarget().isEmpty()) {
                        return;
                    }
                }
            }
        }
        throw new VrfBondingServiceException("The Local Network: " + aaiLocalNetwork.getNetworkId()
                + " does not have vpn binding and/or RT information");
    }
}
