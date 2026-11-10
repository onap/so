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

/*
 * Copyright (C) 2018 Bell Canada. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.onap.so.heatbridge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.L3InterfaceIpv4AddressList;
import org.onap.aai.domain.yang.L3InterfaceIpv6AddressList;
import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.PInterface;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipList;
import org.onap.aai.domain.yang.SriovPf;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aaiclient.client.aai.AAICommonObjectMapperProvider;
import org.onap.aaiclient.client.aai.AAIDSLQueryClient;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.AAISingleTransactionClient;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Results;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.graphinventory.entities.DSLQuery;
import org.onap.aaiclient.client.graphinventory.entities.Pathed;
import org.onap.so.cloud.resource.beans.NodeType;
import org.onap.so.heatbridge.constants.HeatBridgeConstants;
import org.onap.so.heatbridge.helpers.AaiHelper;
import org.onap.so.heatbridge.openstack.api.OpenstackClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;
import org.onap.so.spring.SpringContextHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.woorea.openstack.cinder.model.Volume;
import com.woorea.openstack.heat.model.Resource;
import com.woorea.openstack.heat.model.Resources;
import com.woorea.openstack.nova.model.Flavor;
import com.woorea.openstack.nova.model.Image;
import com.woorea.openstack.nova.model.Server;
import com.woorea.openstack.quantum.model.Network;
import com.woorea.openstack.quantum.model.Port;
import com.woorea.openstack.quantum.model.Subnet;

@RunWith(MockitoJUnitRunner.Silent.class)
public class HeatBridgeImplTest {

    private static final String CLOUD_OWNER = "CloudOwner";
    private static final String REGION_ID = "RegionOne";
    private static final String TENANT_ID = "7320ec4a5b9d4589ba7c4412ccfd290f";
    private static final ObjectMapper MAPPER =
            new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static Logger logger = LoggerFactory.getLogger(HeatBridgeImplTest.class);

    @Mock
    private OpenstackClient osClient;

    @Mock
    private AAIResourcesClient resourcesClient;

    @Mock
    private AAISingleTransactionClient transaction;

    @Mock
    private AAIDSLQueryClient mockDSLClient;

    @Mock
    private Environment env;

    @Mock
    private Server server;

    @Mock
    private Server server2;

    @Mock
    private Image image;

    @Mock
    private AAIDSLQueryClient dSLQueryClient;

    @Mock
    private AAIResourcesClient aaiResourcesClient;

    // Built as a spy in setUp(), after the mocks exist: under Mockito 5, @Spy @InjectMocks with an eager field
    // initializer can leave a real, unspied instance (NotAMockException on doReturn/doNothing/verify).
    private HeatBridgeImpl heatbridge;

    @Before
    public void setUp() {
        when(resourcesClient.beginSingleTransaction()).thenReturn(transaction);
        // HeatBridgeImpl's field initializer `new AaiHelper()` instantiates the RestProperties provider
        // (AaiClientPropertiesImpl), which reads SpringContextHelper's static context and unboxes
        // getProperty(key, type, default), so the mock context has to hand the default back.
        when(env.getProperty(anyString(), any(Class.class), any())).thenAnswer(invocation -> invocation.getArgument(2));
        ApplicationContext springContext = mock(ApplicationContext.class);
        when(springContext.getEnvironment()).thenReturn(env);
        ReflectionTestUtils.setField(SpringContextHelper.class, "context", springContext);
        heatbridge = spy(
                new HeatBridgeImpl(resourcesClient, osClient, CLOUD_OWNER, REGION_ID, TENANT_ID, NodeType.GREENFIELD));
    }

    @Test
    public void testExtractStackResourceIdsByResourceType() {
        // Arrange
        List<Resource> expectedResourceList = (List<Resource>) extractTestStackResources();
        List<String> expectedServerIds =
                Arrays.asList("43c2159b-2c04-46ac-bda5-594110cae2d3", "7cff109a-b2b7-4933-97b4-ec44a8365568");

        // Act
        List<String> serverIds = heatbridge.extractStackResourceIdsByResourceType(expectedResourceList,
                HeatBridgeConstants.OS_SERVER_RESOURCE_TYPE);

        // Assert
        assertEquals(expectedServerIds, serverIds);
    }

    @Test
    @Ignore
    public void testUpdateVserversToAai() throws HeatBridgeException, JsonProcessingException {
        AaiHelper helper = new AaiHelper();
        helper.setAAIResourcesClient(resourcesClient);
        heatbridge.setAAIHelper(helper);

        VfModule module = new VfModule();
        RelationshipList relationships = new RelationshipList();
        List<Relationship> listRelationships = relationships.getRelationship();
        Relationship vnfcRelationship = new Relationship();
        vnfcRelationship.setRelationshipLabel("org.onap.relationships.inventory.Uses");
        vnfcRelationship.setRelatedTo("vnfc");
        vnfcRelationship.setRelatedLink("/aai/v22/network/vnfcs/vnfc/test-server1-name");

        listRelationships.add(vnfcRelationship);

        module.setRelationshipList(relationships);
        AAIResultWrapper wrapper =
                new AAIResultWrapper(new AAICommonObjectMapperProvider().getMapper().writeValueAsString(module));

        when(aaiResourcesClient.get(AAIUriFactory.createResourceUri(
                AAIFluentTypeBuilder.network().genericVnf("test-genericVnf-id").vfModule("test-vfModule-id"))))
                        .thenReturn(wrapper);

        // Arrange
        Server server1 = mock(Server.class);

        when(server1.getId()).thenReturn("test-server1-id");
        when(server1.getHypervisorHostname()).thenReturn("test-hypervisor");
        when(server1.getName()).thenReturn("test-server1-name");
        when(server1.getLinks()).thenReturn(new ArrayList<>());

        Server server2 = mock(Server.class);
        when(server2.getId()).thenReturn("test-server2-id");
        when(server2.getHypervisorHostname()).thenReturn("");
        when(server2.getName()).thenReturn("test-server2-name");
        when(server2.getLinks()).thenReturn(new ArrayList<>());

        List<Server> servers = Arrays.asList(server1, server2);

        Image image = mock(Image.class);
        when(server1.getImage()).thenReturn(image);
        when(server2.getImage()).thenReturn(image);
        when(image.getId()).thenReturn("test-image-id");

        Flavor flavor = mock(Flavor.class);
        when(server1.getFlavor()).thenReturn(flavor);
        when(server2.getFlavor()).thenReturn(flavor);
        when(flavor.getId()).thenReturn("test-flavor-id");

        // Act
        heatbridge.buildAddVserversToAaiAction("test-genericVnf-id", "test-vfModule-id", servers);

        // Assert
        ArgumentCaptor<AAIResourceUri> captor = ArgumentCaptor.forClass(AAIResourceUri.class);
        verify(transaction, times(2)).create(captor.capture(), any());

        List<AAIResourceUri> uris = captor.getAllValues();
        assertEquals(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                .cloudRegion(CLOUD_OWNER, REGION_ID).tenant(TENANT_ID).vserver(server1.getId())), uris.get(0));
        assertEquals(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                .cloudRegion(CLOUD_OWNER, REGION_ID).tenant(TENANT_ID).vserver(server2.getId())), uris.get(1));

    }

    @Test
    @Ignore
    public void testUpdateVserversToAaiNoHypervisorName() throws HeatBridgeException, JsonProcessingException {
        AaiHelper aaiHelper = new AaiHelper();
        VfModule module = new VfModule();
        RelationshipList relationships = new RelationshipList();
        List<Relationship> listRelationships = relationships.getRelationship();
        Relationship vnfcRelationship = new Relationship();
        vnfcRelationship.setRelationshipLabel("org.onap.relationships.inventory.Uses");
        vnfcRelationship.setRelatedTo("vnfc");
        vnfcRelationship.setRelatedLink("/aai/v22/network/vnfcs/vnfc/test-server1-name");

        listRelationships.add(vnfcRelationship);

        module.setRelationshipList(relationships);
        AAIResultWrapper wrapper =
                new AAIResultWrapper(new AAICommonObjectMapperProvider().getMapper().writeValueAsString(module));
        when(aaiResourcesClient.get(AAIUriFactory.createResourceUri(
                AAIFluentTypeBuilder.network().genericVnf("test-genericVnf-id").vfModule("test-vfModule-id"))))
                        .thenReturn(wrapper);

        aaiHelper.setAAIResourcesClient(aaiResourcesClient);
        heatbridge.setAAIHelper(aaiHelper);

        // Arrange
        Server server1 = mock(Server.class);

        when(server1.getId()).thenReturn("test-server1-id");
        when(server1.getHypervisorHostname()).thenReturn("");
        when(server1.getName()).thenReturn("test-server1-name");
        when(server1.getLinks()).thenReturn(new ArrayList<>());

        Server server2 = mock(Server.class);
        when(server2.getId()).thenReturn("test-server2-id");
        when(server2.getName()).thenReturn("test-server2-name");
        when(server2.getLinks()).thenReturn(new ArrayList<>());

        List<Server> servers = Arrays.asList(server1, server2);

        Image image = mock(Image.class);
        when(server1.getImage()).thenReturn(image);
        when(server2.getImage()).thenReturn(image);
        when(image.getId()).thenReturn("test-image-id");

        Flavor flavor = mock(Flavor.class);
        when(server1.getFlavor()).thenReturn(flavor);
        when(server2.getFlavor()).thenReturn(flavor);
        when(flavor.getId()).thenReturn("test-flavor-id");

        // Act
        heatbridge.buildAddVserversToAaiAction("test-genericVnf-id", "test-vfModule-id", servers);

        // Assert
        ArgumentCaptor<AAIResourceUri> captor = ArgumentCaptor.forClass(AAIResourceUri.class);
        verify(transaction, times(2)).create(captor.capture(), any());

        List<AAIResourceUri> uris = captor.getAllValues();
        assertEquals(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                .cloudRegion(CLOUD_OWNER, REGION_ID).tenant(TENANT_ID).vserver(server1.getId())), uris.get(0));
        assertEquals(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                .cloudRegion(CLOUD_OWNER, REGION_ID).tenant(TENANT_ID).vserver(server2.getId())), uris.get(1));
    }

    @Test
    public void testCreateRelationships() throws HeatBridgeException, JsonProcessingException {
        AaiHelper aaiHelper = new AaiHelper();
        VfModule module = new VfModule();
        RelationshipList relationships = new RelationshipList();
        List<Relationship> listRelationships = relationships.getRelationship();
        Relationship vnfcRelationship = new Relationship();
        vnfcRelationship.setRelationshipLabel("org.onap.relationships.inventory.Uses");
        vnfcRelationship.setRelatedTo("vnfc");
        vnfcRelationship.setRelatedLink("/aai/v22/network/vnfcs/vnfc/test-server1-name");

        listRelationships.add(vnfcRelationship);

        module.setRelationshipList(relationships);
        AAIResultWrapper wrapper =
                new AAIResultWrapper(new AAICommonObjectMapperProvider().getMapper().writeValueAsString(module));
        when(aaiResourcesClient.get(AAIUriFactory.createResourceUri(
                AAIFluentTypeBuilder.network().genericVnf("test-genericVnf-id").vfModule("test-vfModule-id"))))
                        .thenReturn(wrapper);

        aaiHelper.setAAIResourcesClient(aaiResourcesClient);
        // Arrange
        Server server1 = mock(Server.class);

        when(server1.getId()).thenReturn("test-server1-id");
        when(server1.getHypervisorHostname()).thenReturn("test-hypervisor");
        when(server1.getName()).thenReturn("test-server1-name");
        when(server1.getLinks()).thenReturn(new ArrayList<>());

        // HypervisorHostname is not set
        Server server2 = mock(Server.class);
        when(server2.getId()).thenReturn("test-server1-id");
        when(server2.getName()).thenReturn("test-server1-name");
        when(server2.getLinks()).thenReturn(new ArrayList<>());

        // HypervisorHostname is empty string
        Server server3 = mock(Server.class);
        when(server3.getId()).thenReturn("test-server1-id");
        when(server3.getHypervisorHostname()).thenReturn("");
        when(server3.getName()).thenReturn("test-server1-name");
        when(server3.getLinks()).thenReturn(new ArrayList<>());

        org.onap.aai.domain.yang.RelationshipList relList = aaiHelper.getVserverRelationshipList(CLOUD_OWNER, REGION_ID,
                "test-genericVnf-id", "test-vfModule-id", server1);
        assertEquals(3, relList.getRelationship().size());

        org.onap.aai.domain.yang.RelationshipList relList2 = aaiHelper.getVserverRelationshipList(CLOUD_OWNER,
                REGION_ID, "test-genericVnf-id", "test-vfModule-id", server2);
        assertEquals(2, relList2.getRelationship().size());

        org.onap.aai.domain.yang.RelationshipList relList3 = aaiHelper.getVserverRelationshipList(CLOUD_OWNER,
                REGION_ID, "test-genericVnf-id", "test-vfModule-id", server3);
        assertEquals(2, relList3.getRelationship().size());
    }

    @Test
    public void testUpdateImagesToAai() throws HeatBridgeException {
        // Arrange
        Image image1 = mock(Image.class);
        when(image1.getId()).thenReturn("test-image1-id");
        when(image1.getName()).thenReturn("test-image1-name");
        when(image1.getLinks()).thenReturn(new ArrayList<>());

        Image image2 = mock(Image.class);
        when(image2.getId()).thenReturn("test-image2-id");
        when(image2.getName()).thenReturn("test-image2-name");
        when(image2.getLinks()).thenReturn(new ArrayList<>());

        List<Image> images = Arrays.asList(image1, image2);

        // Act #1
        heatbridge.buildAddImagesToAaiAction(images);

        // Assert #1
        verify(transaction, times(2)).create(any(AAIResourceUri.class), any(org.onap.aai.domain.yang.Image.class));

        // Act #2
        heatbridge.buildAddImagesToAaiAction(images);

        // Assert #2
        verify(transaction, times(4)).create(any(AAIResourceUri.class), any(org.onap.aai.domain.yang.Image.class));
    }

    @Test
    public void testUpdateFlavorsToAai() throws HeatBridgeException {
        // Arrange
        Flavor flavor1 = mock(Flavor.class);
        when(flavor1.getId()).thenReturn("test-flavor1-id");
        when(flavor1.getName()).thenReturn("test-flavor1-name");
        when(flavor1.getLinks()).thenReturn(new ArrayList<>());

        Flavor flavor2 = mock(Flavor.class);
        when(flavor2.getId()).thenReturn("test-flavor2-id");
        when(flavor2.getName()).thenReturn("test-flavor2-name");
        when(flavor2.getLinks()).thenReturn(new ArrayList<>());

        List<Flavor> flavors = Arrays.asList(flavor1, flavor2);

        // Act #1
        heatbridge.buildAddFlavorsToAaiAction(flavors);

        // Assert #1
        verify(transaction, times(2)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
    }

    @Test
    public void testUpdateVserverLInterfacesToAai() throws HeatBridgeException, JsonMappingException, IOException {
        // Arrange
        List<Resource> stackResources = (List<Resource>) extractTestStackResources();
        Port port = mock(Port.class);
        when(port.getId()).thenReturn("test-port-id");
        when(port.getName()).thenReturn("test-port-name");
        Port.Binding binding = new Port.Binding();
        binding.setVnicType(HeatBridgeConstants.OS_SRIOV_PORT_TYPE);
        binding.setHostId("pserverId");
        when(port.getBinding()).thenReturn(binding);
        when(port.getMacAddress()).thenReturn("78:4f:43:68:e2:78");
        when(port.getNetworkId()).thenReturn("890a203a-23gg-56jh-df67-731656a8f13a");
        when(port.getDeviceId()).thenReturn("test-device-id");

        when(osClient.getServerById("test-device-id")).thenReturn(server);
        when(server.getHypervisorHostname()).thenReturn("test.server.name");
        String pfPciId = "0000:08:00.0";
        binding.setProfile(ImmutableMap.of(HeatBridgeConstants.OS_PCI_SLOT_KEY, pfPciId,
                HeatBridgeConstants.OS_PF_PCI_SLOT_KEY, "testPfPciId"));

        Port.Ip ip = new Port.Ip();
        ip.setAddress("2606:ae00:2e60:100::226");
        ip.setSubnetId("testSubnetId");
        when(port.getList()).thenReturn(Arrays.asList(ip));

        Subnet subnet = mock(Subnet.class);
        when(subnet.getCidr()).thenReturn("169.254.100.0/24");
        when(osClient.getSubnetById("testSubnetId")).thenReturn(subnet);

        Network network = mock(Network.class);
        when(network.getId()).thenReturn("test-network-id");
        when(network.getProviderNetworkType()).thenReturn("vlan");
        when(network.getProviderSegmentationId()).thenReturn(2345);
        when(network.getProviderPhysicalNetwork()).thenReturn("ovsnet");

        when(osClient.getPortById("212a203a-9764-4f42-84ea-731536a8f13a")).thenReturn(port);
        when(osClient.getPortById("387e3904-8948-43d1-8635-b6c2042b54da")).thenReturn(port);
        when(osClient.getPortById("70a09dfd-f1c5-4bc8-bd8f-dc539b8d662a")).thenReturn(port);
        when(osClient.getPortById("12f88b4d-c8a4-4fbd-bcb4-7e36af02430b")).thenReturn(port);
        when(osClient.getPortById("c54b9f45-b413-4937-bbe4-3c8a5689cfc9")).thenReturn(port);
        when(osClient.getNetworkById(anyString())).thenReturn(network);

        SriovPf sriovPf = new SriovPf();
        sriovPf.setPfPciId(pfPciId);

        when(resourcesClient.exists(any(AAIResourceUri.class))).thenReturn(true);
        when(env.getProperty("mso.cloudOwner.included", "")).thenReturn("CloudOwner");
        doReturn(dSLQueryClient).when(heatbridge).getAAIDSLClient();
        List<Pathed> pathed = ((Results<Pathed>) MAPPER.readValue(getJson("pathed-sriov-pf.json"),
                new TypeReference<Results<Pathed>>() {})).getResult();
        when(dSLQueryClient.queryPathed(any(DSLQuery.class))).thenReturn(pathed);
        doReturn(false).when(heatbridge).sriovVfHasSriovPfRelationship(any());

        // Act
        heatbridge.buildAddVserverLInterfacesToAaiAction(stackResources, Arrays.asList("1", "2"), "CloudOwner");

        // Assert
        verify(transaction, times(20)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
        verify(osClient, times(5)).getPortById(anyString());
        verify(osClient, times(5)).getSubnetById("testSubnetId");
        verify(osClient, times(10)).getNetworkById(anyString());
        verify(transaction, times(5)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }

    @Test
    public void testUpdateNetworksToAai() {

        Subnet subnet1 = mock(Subnet.class);
        when(subnet1.getId()).thenReturn("test-subnet1-id");
        when(subnet1.getName()).thenReturn("test-subnet1-name");
        when(subnet1.getEnableDHCP()).thenReturn(Boolean.TRUE);
        when(subnet1.getGw()).thenReturn("test-subnet1-gateway");
        when(subnet1.getCidr()).thenReturn("test-subnet1-gateway");
        when(subnet1.getIpversion()).thenReturn(Subnet.IpVersion.IPV4);

        Subnet subnet2 = mock(Subnet.class);
        when(subnet2.getId()).thenReturn("test-subnet2-id");
        when(subnet2.getName()).thenReturn("test-subnet2-name");
        when(subnet2.getEnableDHCP()).thenReturn(Boolean.TRUE);
        when(subnet2.getGw()).thenReturn("test-subnet1-gateway");
        when(subnet2.getCidr()).thenReturn("test-subnet1-gateway");
        when(subnet2.getIpversion()).thenReturn(Subnet.IpVersion.IPV6);

        when(osClient.getSubnetById(subnet1.getId())).thenReturn(subnet1);
        when(osClient.getSubnetById(subnet2.getId())).thenReturn(subnet2);

        List<String> subnetIds = Arrays.asList(subnet1.getId(), subnet2.getId());

        // Arrange
        Network network1 = mock(Network.class);
        when(network1.getId()).thenReturn("test-network1-id");
        when(network1.getShared()).thenReturn(Boolean.TRUE);
        when(network1.getRouterExternal()).thenReturn(Boolean.TRUE);
        when(network1.getAdminStateUp()).thenReturn(Boolean.TRUE);
        when(network1.getProviderPhysicalNetwork()).thenReturn("sriov-network1");
        when(network1.getName()).thenReturn("network1");
        when(network1.getSubnets()).thenReturn(subnetIds);

        Network network2 = mock(Network.class);
        when(network2.getId()).thenReturn("test-network2-id");
        when(network2.getShared()).thenReturn(Boolean.TRUE);
        when(network2.getRouterExternal()).thenReturn(Boolean.TRUE);
        when(network2.getAdminStateUp()).thenReturn(Boolean.TRUE);
        when(network2.getProviderPhysicalNetwork()).thenReturn("sriov-network2");
        when(network2.getName()).thenReturn("network2");
        when(network2.getSubnets()).thenReturn(subnetIds);

        String vnfId = "some-uuiid-of-the-vnf";
        String vfModuleId = "some-uuiid-of-the-vf-module";

        Subnet subnet = mock(Subnet.class);

        List<Network> networks = Arrays.asList(network1, network2);

        // Act #1
        heatbridge.buildAddNetworksToAaiAction(vnfId, vfModuleId, networks);

        // Assert #1
        verify(transaction, times(2)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));

        // Act #2
        heatbridge.buildAddNetworksToAaiAction(vnfId, vfModuleId, networks);

        // Assert #2
        verify(transaction, times(4)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));

    }

    @Test
    public void testUpdateLInterfaceVlan() throws HeatBridgeException {
        // Arrange
        List<Resource> stackResources = (List<Resource>) extractTestStackResources();
        Port port = mock(Port.class);
        when(port.getId()).thenReturn("test-port-id");
        when(port.getName()).thenReturn("test-port-name");
        Port.Binding binding = new Port.Binding();
        binding.setVnicType(HeatBridgeConstants.OS_SRIOV_PORT_TYPE);
        when(port.getBinding()).thenReturn(binding);
        when(port.getMacAddress()).thenReturn("78:4f:43:68:e2:78");
        when(port.getNetworkId()).thenReturn("890a203a-23gg-56jh-df67-731656a8f13a");
        when(port.getDeviceId()).thenReturn("test-device-id");

        LInterface lIf = new LInterface();
        lIf.setInterfaceId("test-port-id");
        lIf.setInterfaceType("SRIOV");
        lIf.setInterfaceName("name");

        String pfPciId = "0000:08:00.0";
        binding.setProfile(ImmutableMap.of(HeatBridgeConstants.OS_PCI_SLOT_KEY, pfPciId,
                HeatBridgeConstants.OS_PHYSICAL_NETWORK_KEY, "physical_network_id"));

        Port.Ip ip = new Port.Ip();
        ip.setAddress("2606:ae00:2e60:100::226");
        ip.setSubnetId("testSubnetId");
        when(port.getList()).thenReturn(Arrays.asList(ip));

        Subnet subnet = mock(Subnet.class);
        when(subnet.getCidr()).thenReturn("169.254.100.0/24");
        when(osClient.getSubnetById("testSubnetId")).thenReturn(subnet);

        Network network = mock(Network.class);
        when(network.getId()).thenReturn("test-network-id");
        when(network.getProviderNetworkType()).thenReturn("vlan");
        when(network.getProviderSegmentationId()).thenReturn(2345);
        when(network.getProviderPhysicalNetwork()).thenReturn("ovsnet");
        doNothing().when(heatbridge).processOVS(any(), any(), any());

        when(osClient.getNetworkById(anyString())).thenReturn(network);

        SriovPf sriovPf = new SriovPf();
        sriovPf.setPfPciId(pfPciId);
        PInterface pIf = mock(PInterface.class);
        when(pIf.getInterfaceName()).thenReturn("test-port-id");
        when(resourcesClient.get(eq(PInterface.class), any(AAIResourceUri.class))).thenReturn(Optional.of(pIf));

        // Act
        heatbridge.createVlanAndSriovVF(port, lIf, "hostname");

        // Assert
        verify(transaction, times(2)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
        verify(osClient, times(1)).getNetworkById(anyString());
        verify(heatbridge, times(0)).processOVS(any(), any(), any());
    }

    @Test
    public void testUpdateLInterfaceIps() {

        Port port = mock(Port.class);
        when(port.getNetworkId()).thenReturn("890a203a-23gg-56jh-df67-731656a8f13a");
        when(port.getDeviceId()).thenReturn("test-device-id");

        Port.Ip ip = new Port.Ip();
        ip.setAddress("2606:ae00:2e60:100::226");
        ip.setSubnetId("testSubnetId");
        when(port.getList()).thenReturn(Arrays.asList(ip));

        Subnet subnet = mock(Subnet.class);
        when(subnet.getCidr()).thenReturn("169.254.100.0/24");
        when(osClient.getSubnetById("testSubnetId")).thenReturn(subnet);

        LInterface lIf = new LInterface();
        lIf.setInterfaceName("test-port-name");

        // Act
        heatbridge.updateLInterfaceIps(port, lIf);

        L3InterfaceIpv6AddressList ipv6 = new L3InterfaceIpv6AddressList();
        ipv6.setIsFloating(false);
        ipv6.setL3InterfaceIpv6Address("2606:ae00:2e60:100::226");
        ipv6.setNeutronNetworkId(port.getNetworkId());
        ipv6.setNeutronSubnetId(ip.getSubnetId());
        ipv6.setL3InterfaceIpv6PrefixLength(Long.parseLong("24"));

        ArgumentCaptor<Optional> argument = ArgumentCaptor.forClass(Optional.class);

        // Assert
        verify(transaction).createIfNotExists(
                eq(AAIUriFactory.createResourceUri(
                        AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion("CloudOwner", "RegionOne")
                                .tenant("7320ec4a5b9d4589ba7c4412ccfd290f").vserver("test-device-id")
                                .lInterface("test-port-name").l3InterfaceIpv6AddressList("2606:ae00:2e60:100::226"))),
                argument.capture());

        assertTrue(argument.getValue().isPresent());

        assertThat((L3InterfaceIpv6AddressList) argument.getValue().get()).usingRecursiveComparison().isEqualTo(ipv6);
    }

    @Test
    public void testUpdateLInterfaceIpsIpv4() {
        Port port = mockPortWithFixedIp("10.10.20.5", true);
        Subnet subnet = mock(Subnet.class);
        when(subnet.getCidr()).thenReturn("10.10.20.0/26");
        when(osClient.getSubnetById("testSubnetId")).thenReturn(subnet);
        LInterface lIf = new LInterface();
        lIf.setInterfaceName("test-port-name");

        heatbridge.updateLInterfaceIps(port, lIf);

        L3InterfaceIpv4AddressList ipv4 = new L3InterfaceIpv4AddressList();
        ipv4.setIsFloating(false);
        ipv4.setL3InterfaceIpv4Address("10.10.20.5");
        ipv4.setNeutronNetworkId("890a203a-23gg-56jh-df67-731656a8f13a");
        ipv4.setNeutronSubnetId("testSubnetId");
        ipv4.setL3InterfaceIpv4PrefixLength(26L);
        ArgumentCaptor<Optional> argument = ArgumentCaptor.forClass(Optional.class);
        verify(transaction).createIfNotExists(
                eq(AAIUriFactory.createResourceUri(
                        AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion("CloudOwner", "RegionOne")
                                .tenant("7320ec4a5b9d4589ba7c4412ccfd290f").vserver("test-device-id")
                                .lInterface("test-port-name").l3InterfaceIpv4AddressList("10.10.20.5"))),
                argument.capture());
        assertThat((L3InterfaceIpv4AddressList) argument.getValue().get()).usingRecursiveComparison().isEqualTo(ipv4);
    }

    @Test
    public void testUpdateLInterfaceIpsIpv4MappedIpv6() {
        Port port = mockPortWithFixedIp("::ffff:10.10.20.5", true);
        Subnet subnet = mock(Subnet.class);
        when(subnet.getCidr()).thenReturn("::ffff:10.10.20.0/120");
        when(osClient.getSubnetById("testSubnetId")).thenReturn(subnet);
        LInterface lIf = new LInterface();
        lIf.setInterfaceName("test-port-name");

        heatbridge.updateLInterfaceIps(port, lIf);

        ArgumentCaptor<Optional> argument = ArgumentCaptor.forClass(Optional.class);
        verify(transaction).createIfNotExists(any(AAIResourceUri.class), argument.capture());
        assertThat(argument.getValue().get()).isInstanceOf(L3InterfaceIpv6AddressList.class);
    }

    @Test
    public void testUpdateLInterfaceIpsSkipsInvalidAddresses() {
        for (String invalid : Arrays.asList("not-an-ip", "10.10.20.256", "10.10.20.0/24", "2606:ae00::226::1", "")) {
            Port port = mockPortWithFixedIp(invalid, false);
            LInterface lIf = new LInterface();
            lIf.setInterfaceName("test-port-name");

            heatbridge.updateLInterfaceIps(port, lIf);
        }

        verify(osClient, never()).getSubnetById(anyString());
        verify(transaction, never()).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
    }

    private Port mockPortWithFixedIp(String ipAddress, boolean valid) {
        Port port = mock(Port.class);
        Port.Ip ip = new Port.Ip();
        ip.setAddress(ipAddress);
        when(port.getList()).thenReturn(Arrays.asList(ip));
        if (valid) {
            ip.setSubnetId("testSubnetId");
            when(port.getNetworkId()).thenReturn("890a203a-23gg-56jh-df67-731656a8f13a");
            when(port.getDeviceId()).thenReturn("test-device-id");
        }
        return port;
    }

    @Test
    public void testUpdateVserverLInterfacesToAai_skipVlans() throws HeatBridgeException {
        // Arrange
        List<Resource> stackResources = (List<Resource>) extractTestStackResources();
        Port port = mock(Port.class);
        when(port.getId()).thenReturn("test-port-id");
        when(port.getName()).thenReturn("test-port-name");
        Port.Binding binding = new Port.Binding();
        binding.setVnicType(HeatBridgeConstants.OS_SRIOV_PORT_TYPE);
        when(port.getBinding()).thenReturn(binding);
        when(port.getMacAddress()).thenReturn("78:4f:43:68:e2:78");
        when(port.getNetworkId()).thenReturn("890a203a-23gg-56jh-df67-731656a8f13a");
        when(port.getDeviceId()).thenReturn("test-device-id");
        String pfPciId = "0000:08:00.0";
        binding.setProfile(ImmutableMap.of(HeatBridgeConstants.OS_PCI_SLOT_KEY, pfPciId,
                HeatBridgeConstants.OS_PHYSICAL_NETWORK_KEY, "physical_network_id"));

        Network network = mock(Network.class);
        when(network.getId()).thenReturn("test-network-id");
        when(network.getProviderNetworkType()).thenReturn("vlan");
        when(network.getProviderSegmentationId()).thenReturn(2345);

        when(osClient.getPortById("212a203a-9764-4f42-84ea-731536a8f13a")).thenReturn(port);
        when(osClient.getPortById("387e3904-8948-43d1-8635-b6c2042b54da")).thenReturn(port);
        when(osClient.getPortById("70a09dfd-f1c5-4bc8-bd8f-dc539b8d662a")).thenReturn(port);
        when(osClient.getPortById("12f88b4d-c8a4-4fbd-bcb4-7e36af02430b")).thenReturn(port);
        when(osClient.getPortById("c54b9f45-b413-4937-bbe4-3c8a5689cfc9")).thenReturn(port);
        when(osClient.getNetworkById(anyString())).thenReturn(network);

        PInterface pIf = mock(PInterface.class);
        when(pIf.getInterfaceName()).thenReturn("test-port-id");
        doNothing().when(heatbridge).updateSriovPfToSriovVF(any(), any());

        // Act
        heatbridge.buildAddVserverLInterfacesToAaiAction(stackResources, Arrays.asList("1", "2"), "CloudOwner");

        // Assert
        verify(transaction, times(5)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
        verify(osClient, times(5)).getPortById(anyString());
        verify(osClient, times(5)).getNetworkById(anyString());
    }

    @Test
    public void testBuildAddVserverLInterfacesToAaiAction_PortProfileNull() throws HeatBridgeException {
        // Arrange
        List<Resource> stackResources = (List<Resource>) extractTestStackResources();
        Port port = mock(Port.class);
        when(port.getId()).thenReturn("test-port-id");
        when(port.getName()).thenReturn("test-port-name");
        Port.Binding binding = new Port.Binding();
        binding.setVnicType(HeatBridgeConstants.OS_SRIOV_PORT_TYPE);
        when(port.getBinding()).thenReturn(binding);
        when(port.getMacAddress()).thenReturn("78:4f:43:68:e2:78");
        when(port.getNetworkId()).thenReturn("890a203a-23gg-56jh-df67-731656a8f13a");
        when(port.getDeviceId()).thenReturn("test-device-id");

        Network network = mock(Network.class);
        when(network.getId()).thenReturn("test-network-id");
        when(network.getProviderNetworkType()).thenReturn("vlan");
        when(network.getProviderSegmentationId()).thenReturn(2345);

        when(osClient.getPortById("212a203a-9764-4f42-84ea-731536a8f13a")).thenReturn(port);
        when(osClient.getPortById("387e3904-8948-43d1-8635-b6c2042b54da")).thenReturn(port);
        when(osClient.getPortById("70a09dfd-f1c5-4bc8-bd8f-dc539b8d662a")).thenReturn(port);
        when(osClient.getPortById("12f88b4d-c8a4-4fbd-bcb4-7e36af02430b")).thenReturn(port);
        when(osClient.getPortById("c54b9f45-b413-4937-bbe4-3c8a5689cfc9")).thenReturn(port);
        when(osClient.getNetworkById(anyString())).thenReturn(network);

        PInterface pIf = mock(PInterface.class);
        when(pIf.getInterfaceName()).thenReturn("test-port-id");
        doNothing().when(heatbridge).updateSriovPfToSriovVF(any(), any());

        // Act
        heatbridge.buildAddVserverLInterfacesToAaiAction(stackResources, Arrays.asList("1", "2"), "CloudOwner");

        // Assert
        ArgumentCaptor<Optional> argument = ArgumentCaptor.forClass(Optional.class);
        verify(transaction, times(5)).createIfNotExists(any(AAIResourceUri.class), argument.capture());
        assertEquals(false, ((LInterface) argument.getValue().get()).isL2Multicasting());
        verify(osClient, times(5)).getPortById(anyString());
        verify(osClient, times(5)).getNetworkById(anyString());
    }

    @Test
    public void testBuildAddVserverLInterfacesToAaiAction_DeviceIdNull() throws HeatBridgeException {
        // Arrange
        List<Resource> stackResources = (List<Resource>) extractTestStackResources();
        Port port = mock(Port.class);
        when(port.getId()).thenReturn("test-port-id");
        when(port.getName()).thenReturn("test-port-name");
        Port.Binding binding = new Port.Binding();
        binding.setVnicType(HeatBridgeConstants.OS_SRIOV_PORT_TYPE);
        when(port.getBinding()).thenReturn(binding);
        when(port.getMacAddress()).thenReturn("78:4f:43:68:e2:78");
        when(port.getNetworkId()).thenReturn("890a203a-23gg-56jh-df67-731656a8f13a");
        when(port.getDeviceId()).thenReturn(null);

        Network network = mock(Network.class);
        when(network.getId()).thenReturn("test-network-id");
        when(network.getProviderNetworkType()).thenReturn("vlan");
        when(network.getProviderSegmentationId()).thenReturn(2345);

        when(osClient.getPortById("212a203a-9764-4f42-84ea-731536a8f13a")).thenReturn(port);
        when(osClient.getPortById("387e3904-8948-43d1-8635-b6c2042b54da")).thenReturn(port);
        when(osClient.getPortById("70a09dfd-f1c5-4bc8-bd8f-dc539b8d662a")).thenReturn(port);
        when(osClient.getPortById("12f88b4d-c8a4-4fbd-bcb4-7e36af02430b")).thenReturn(port);
        when(osClient.getPortById("c54b9f45-b413-4937-bbe4-3c8a5689cfc9")).thenReturn(port);
        when(osClient.getNetworkById(anyString())).thenReturn(network);

        PInterface pIf = mock(PInterface.class);
        when(pIf.getInterfaceName()).thenReturn("test-port-id");
        doNothing().when(heatbridge).updateSriovPfToSriovVF(any(), any());

        // Act
        heatbridge.buildAddVserverLInterfacesToAaiAction(stackResources, Arrays.asList("1", "2"), "CloudOwner");

        // Assert
        verify(transaction, times(0)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
        verify(osClient, times(5)).getPortById(anyString());
        verify(osClient, times(5)).getNetworkById(anyString());
    }

    @Test
    public void testExtractOpenstackImagesFromServers() {
        // Arrange
        List<Server> serverList = new ArrayList<>();
        serverList.add(server);
        serverList.add(server2);
        when(server.getImage()).thenReturn(null);
        when(server.getImage()).thenReturn(image);
        when(image.getId()).thenReturn("imageId");
        // Act

        List<Image> images = heatbridge.extractOpenstackImagesFromServers(serverList);

        // Assert
        assertEquals(1, images.size());
    }

    @Test
    public void testBuildAddVolumes() throws HeatBridgeException {
        List<Resource> stackResources = (List<Resource>) extractTestStackResources();
        Volume volume = mock(Volume.class);
        Volume.Attachment attachment = new Volume.Attachment();
        attachment.setServerId("vserverIdTest");
        when(volume.getAttachments()).thenReturn(Arrays.asList(attachment));

        when(osClient.getVolumeById("5ad95036-8daf-4379-a59c-865f35976ca3")).thenReturn(volume);

        heatbridge.buildAddVolumes(stackResources);

        verify(transaction, times(1)).createIfNotExists(
                eq(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                        .cloudRegion("CloudOwner", "RegionOne").tenant("7320ec4a5b9d4589ba7c4412ccfd290f")
                        .vserver("vserverIdTest").volume("5ad95036-8daf-4379-a59c-865f35976ca3"))),
                any(Optional.class));
        verify(osClient, times(1)).getVolumeById(eq("5ad95036-8daf-4379-a59c-865f35976ca3"));
    }

    private List<Resource> extractTestStackResources() {
        List<Resource> stackResources = null;
        try {
            stackResources = MAPPER.readValue(readTestResourceFile("stack-resources.json"), Resources.class).getList();
            assertNotNull(stackResources);
            assertFalse(stackResources.isEmpty());
        } catch (IOException e) {
            Assert.fail("Failed to extract test stack resources.");
        }
        return stackResources;
    }

    @Test
    public void testGetAllOpenstackServers_bootFromVolume() {
        Server bootFromVolume = new Server();
        Flavor flavorRef = new Flavor();
        flavorRef.setId("f1");
        bootFromVolume.setFlavor(flavorRef);
        Flavor fullFlavor = new Flavor();
        fullFlavor.setId("f1");
        fullFlavor.setName("m1.small");
        when(osClient.getServerById(anyString())).thenReturn(bootFromVolume);
        when(osClient.getFlavorById("f1")).thenReturn(fullFlavor);

        List<Server> servers = heatbridge.getAllOpenstackServers(extractTestStackResources());

        assertNull(servers.get(0).getImage());
        assertEquals("m1.small", servers.get(0).getFlavor().getName());
        verify(osClient, never()).getImageById(anyString());
    }

    @Test
    public void testExtractOpenstackFlavorsFromServers_skipsMissingFlavor() {
        Server withoutFlavor = new Server();
        Server withFlavor = new Server();
        Flavor flavor = new Flavor();
        flavor.setId("f1");
        withFlavor.setFlavor(flavor);

        List<Flavor> flavors = heatbridge.extractOpenstackFlavorsFromServers(Arrays.asList(withoutFlavor, withFlavor));

        assertEquals(1, flavors.size());
    }

    @Test
    public void testCreatePserversAndPinterfaces_portWithoutBinding() throws HeatBridgeException {
        Port port = new Port();
        port.setBinding(new Port.Binding());
        when(osClient.getPortById(anyString())).thenReturn(port);
        when(osClient.getServerById(anyString())).thenReturn(new Server());

        heatbridge.createPserversAndPinterfacesIfNotPresentInAai(extractTestStackResources());

        verify(resourcesClient, never()).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
    }

    private String readTestResourceFile(String filePath) {
        String content = null;
        String pathname = Objects.requireNonNull(getClass().getClassLoader().getResource(filePath)).getFile();
        File file = new File(pathname);
        try {
            content = Objects.requireNonNull(FileUtils.readFileToString(file, Charset.defaultCharset()));
        } catch (IOException e) {
            Assert.fail(String.format("Failed to read test resource file (%s)", filePath));
        }
        return content;
    }

    private String getJson(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/__files/" + filename)));
    }

}
