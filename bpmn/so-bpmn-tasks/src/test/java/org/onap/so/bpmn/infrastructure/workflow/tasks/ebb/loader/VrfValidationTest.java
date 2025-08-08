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

package org.onap.so.bpmn.infrastructure.workflow.tasks.ebb.loader;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.onap.aai.domain.yang.AggregateRoute;
import org.onap.aai.domain.yang.AggregateRoutes;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.RouteTarget;
import org.onap.aai.domain.yang.RouteTargets;
import org.onap.aai.domain.yang.Subnet;
import org.onap.aai.domain.yang.Subnets;
import org.onap.aai.domain.yang.VpnBinding;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.infrastructure.workflow.tasks.VrfBondingServiceException;
import org.onap.so.db.catalog.beans.ConfigurationResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceProxyResourceCustomization;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VrfValidationTest extends BaseTaskTest {

    protected ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    protected VrfValidation vrfValidation;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void before() {
        vrfValidation.setBbInputSetupUtils(bbSetupUtils);
    }

    @Test
    public void testVrfServiceValidation() throws VrfBondingServiceException {
        Service service = new Service();
        service.setModelName("modelName");
        service.setServiceType("BONDING");
        service.setServiceRole("VPN");
        exceptionRule.expect(VrfBondingServiceException.class);
        exceptionRule.expectMessage(
                "Service: modelName does not have service type of BONDING and does not have service role of INFRASTRUCTURE-VPN");
        vrfValidation.vrfServiceValidation(service);

        service.setServiceType("BOND");
        service.setServiceRole("INFRASTRUCTURE-VPN");
        exceptionRule.expect(VrfBondingServiceException.class);
        exceptionRule.expectMessage(
                "Service: modelName does not have service type of BONDING and does not have service role of INFRASTRUCTURE-VPN");
        vrfValidation.vrfServiceValidation(service);

        service.setServiceType("BONDING");
        service.setServiceRole("INFRASTRUCTURE-VPN");
        ExpectedException.none();
        vrfValidation.vrfServiceValidation(service);
    }

    @Test
    public void testVrfCatalogDbChecks() throws VrfBondingServiceException {
        Service service = new Service();
        service.setModelName("modelName");
        ConfigurationResourceCustomization configuration = new ConfigurationResourceCustomization();
        service.setConfigurationCustomizations(new ArrayList<>());
        service.getConfigurationCustomizations().add(configuration);
        ServiceProxyResourceCustomization serviceProxy = new ServiceProxyResourceCustomization();
        configuration.setServiceProxyResourceCustomization(serviceProxy);
        service.setServiceProxyCustomizations(new ArrayList<>());
        service.getServiceProxyCustomizations().add(serviceProxy);
        Service sourceService = new Service();
        sourceService.setServiceType("TRANSPORT");
        serviceProxy.setSourceService(sourceService);
        configuration.setType("VRF-ENTRY");
        configuration.setRole("INFRASTRUCTURE-CLOUD-VPN");
        ExpectedException.none();
        vrfValidation.vrfCatalogDbChecks(service);
    }

    @Test
    public void testAaiVpnBindingValidation() throws VrfBondingServiceException {
        org.onap.aai.domain.yang.VpnBinding aaiVpnBinding = new org.onap.aai.domain.yang.VpnBinding();
        aaiVpnBinding.setVpnType("SERVICE-INFRASTRUCTURE");
        ExpectedException.none();
        vrfValidation.aaiVpnBindingValidation("test-vpn", aaiVpnBinding);
    }

    @Test
    public void testAaiVpnBindingValidationVpnBindingIsNull() throws VrfBondingServiceException {
        exceptionRule.expect(VrfBondingServiceException.class);
        exceptionRule.expectMessage("The infrastructure vpn test-vpn does not exist in A&AI.");
        vrfValidation.aaiVpnBindingValidation("test-vpn", null);
    }

    @Test
    public void testAaiNetworkValidation() throws VrfBondingServiceException {
        org.onap.aai.domain.yang.L3Network aaiLocalNetwork = new org.onap.aai.domain.yang.L3Network();
        aaiLocalNetwork.setNetworkId("test-network");
        ExpectedException.none();
        vrfValidation.aaiNetworkValidation("test-network", aaiLocalNetwork);
    }

    @Test
    public void testAaiNetworkValidationNetworkIsNull() throws VrfBondingServiceException {
        exceptionRule.expect(VrfBondingServiceException.class);
        exceptionRule.expectMessage("The local network test-network does not exist in A&AI.");
        vrfValidation.aaiNetworkValidation("test-network", null);
    }

    @Test
    public void testAaiAggregateRouteValidation() throws VrfBondingServiceException {
        org.onap.aai.domain.yang.L3Network aaiLocalNetwork = new org.onap.aai.domain.yang.L3Network();
        aaiLocalNetwork.setAggregateRoutes(new AggregateRoutes());
        aaiLocalNetwork.getAggregateRoutes().getAggregateRoute().add(new AggregateRoute());
        aaiLocalNetwork.getAggregateRoutes().getAggregateRoute().get(0).setIpVersion("4");
        ExpectedException.none();
        vrfValidation.aaiAggregateRouteValidation(aaiLocalNetwork);

        aaiLocalNetwork.getAggregateRoutes().getAggregateRoute().add(new AggregateRoute());
        aaiLocalNetwork.getAggregateRoutes().getAggregateRoute().get(1).setIpVersion("6");
        ExpectedException.none();
        vrfValidation.aaiAggregateRouteValidation(aaiLocalNetwork);

        aaiLocalNetwork.setAggregateRoutes(null);
        ExpectedException.none();
        vrfValidation.aaiAggregateRouteValidation(aaiLocalNetwork);
    }

    @Test
    public void testAaiSubnetValidation() throws VrfBondingServiceException {
        org.onap.aai.domain.yang.L3Network aaiLocalNetwork = new org.onap.aai.domain.yang.L3Network();
        aaiLocalNetwork.setNetworkId("myNetworkID");
        aaiLocalNetwork.setSubnets(new Subnets());
        aaiLocalNetwork.getSubnets().getSubnet().add(new Subnet());
        aaiLocalNetwork.getSubnets().getSubnet().get(0).setIpVersion("4");
        ExpectedException.none();
        vrfValidation.aaiSubnetValidation(aaiLocalNetwork);

        aaiLocalNetwork.getSubnets().getSubnet().add(new Subnet());
        aaiLocalNetwork.getSubnets().getSubnet().get(1).setIpVersion("6");
        ExpectedException.none();
        vrfValidation.aaiSubnetValidation(aaiLocalNetwork);

        aaiLocalNetwork.setSubnets(null);
        exceptionRule.expect(VrfBondingServiceException.class);
        exceptionRule.expectMessage("LocalNetwork: myNetworkID has no subnets");
        vrfValidation.aaiSubnetValidation(aaiLocalNetwork);
    }

    @Test
    public void testIpVersionValidation() {
        String ipVersion1 = "4";
        String ipVersion2 = "6";
        boolean validation = vrfValidation.ipVersionValidation(ipVersion1, ipVersion2);
        assertEquals("Validation is correct", true, validation);


        validation = vrfValidation.ipVersionValidation(ipVersion2, ipVersion1);
        assertEquals("Validation is correct", true, validation);

        ipVersion1 = "6";
        validation = vrfValidation.ipVersionValidation(ipVersion1, ipVersion2);
        assertEquals("Validation is correct", false, validation);
    }

    @Test
    public void testAaiRouteTargetValidation() throws VrfBondingServiceException, IOException {
        L3Network l3Network = mapper.readValue(
                new File("src/test/resources/__files/BuildingBlocks/aaiNetworkWrapper.json"), L3Network.class);
        AAIResultWrapper networkWrapper = new AAIResultWrapper(l3Network);
        if (networkWrapper.getRelationships().isPresent()) {
            List<AAIResourceUri> vpnBindingUris =
                    networkWrapper.getRelationships().get().getRelatedUris(Types.VPN_BINDING);
            VpnBinding vpnBinding = new VpnBinding();
            vpnBinding.setRouteTargets(new RouteTargets());
            vpnBinding.getRouteTargets().getRouteTarget().add(new RouteTarget());
            AAIResultWrapper wrapper = Mockito.mock(AAIResultWrapper.class);
            doReturn(wrapper).when(bbSetupUtils).getAAIResourceDepthOne(vpnBindingUris.get(0));
            doReturn(Optional.of(vpnBinding)).when(wrapper).asBean(VpnBinding.class);
            ExpectedException.none();
            vrfValidation.aaiRouteTargetValidation(l3Network);
        }
    }
}
