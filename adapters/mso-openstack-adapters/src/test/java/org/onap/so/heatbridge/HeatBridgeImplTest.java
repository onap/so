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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.PInterface;
import org.onap.aai.domain.yang.SriovPf;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.AAISingleTransactionClient;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.graphinventory.exceptions.BulkProcessFailed;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.heatbridge.constants.HeatBridgeConstants;
import org.onap.so.heatbridge.openstack.api.OpenstackClient;
import org.onap.so.heatbridge.openstack.api.OpenstackClientException;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Image;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.Server.Status;
import org.openstack4j.model.heat.Resource;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.NetworkType;
import org.openstack4j.model.network.Port;
import org.openstack4j.openstack.heat.domain.HeatResource;
import org.openstack4j.openstack.heat.domain.HeatResource.Resources;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;


@RunWith(MockitoJUnitRunner.class)
public class HeatBridgeImplTest {

    private static final String CLOUD_OWNER = "CloudOwner";
    private static final String REGION_ID = "RegionOne";
    private static final String TENANT_ID = "7320ec4a5b9d4589ba7c4412ccfd290f";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private OpenstackClient osClient;

    private CloudIdentity cloudIdentity = new CloudIdentity();

    @Mock
    private AAIResourcesClient resourcesClient;
    @Mock
    private AAISingleTransactionClient transaction;

    private HeatBridgeImpl heatbridge;

    @Before
    public void setUp() throws HeatBridgeException, OpenstackClientException, BulkProcessFailed {

        when(resourcesClient.beginSingleTransaction()).thenReturn(transaction);
        heatbridge = new HeatBridgeImpl(resourcesClient, cloudIdentity, CLOUD_OWNER, REGION_ID, REGION_ID, TENANT_ID);
    }

    @Ignore
    @Test
    public void testQueryNestedHeatStackResources() throws HeatBridgeException {
        // Arrange
        String heatStackId = "1234567";
        List<Resource> expectedResourceList = (List<Resource>) extractTestStackResources();
        when(osClient.getStackBasedResources(heatStackId, HeatBridgeConstants.OS_DEFAULT_HEAT_NESTING))
                .thenReturn(expectedResourceList);

        // Act
        List<Resource> resourceList = heatbridge.queryNestedHeatStackResources(heatStackId);

        // Assert
        verify(osClient).getStackBasedResources(heatStackId, HeatBridgeConstants.OS_DEFAULT_HEAT_NESTING);
        assertEquals(resourceList, expectedResourceList);
    }

    @Test
    public void testExtractStackResourceIdsByResourceType() throws HeatBridgeException {
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

    @Ignore
    @Test
    public void testGetAllOpenstackServers() {
        // Arrange
        List<Resource> stackResources = (List<Resource>) extractTestStackResources();

        Server server1 = mock(Server.class);
        Server server2 = mock(Server.class);
        List<Server> expectedServers = Arrays.asList(server1, server2);

        when(osClient.getServerById("43c2159b-2c04-46ac-bda5-594110cae2d3")).thenReturn(server1);
        when(osClient.getServerById("7cff109a-b2b7-4933-97b4-ec44a8365568")).thenReturn(server2);

        // Act
        List<Server> servers = heatbridge.getAllOpenstackServers(stackResources);

        // Assert
        assertEquals(expectedServers, servers);
    }

    @Ignore
    @Test
    public void testExtractOpenstackImagesFromServers() {
        // Arrange
        Server server1 = mock(Server.class);
        Server server2 = mock(Server.class);
        List<Server> servers = Arrays.asList(server1, server2);

        Image image1 = mock(Image.class);
        Image image2 = mock(Image.class);
        when(image1.getId()).thenReturn("1");
        when(image2.getId()).thenReturn("1");
        List<Image> expectedDistinctImages = Collections.singletonList(image1);

        when(server1.getImage()).thenReturn(image1);
        when(server2.getImage()).thenReturn(image2);

        // Act
        List<Image> images = heatbridge.extractOpenstackImagesFromServers(servers);

        // Assert
        assertEquals(expectedDistinctImages, images);
    }

    @Ignore
    @Test
    public void testExtractOpenstackFlavorsFromServers() {
        // Arrange
        Server server1 = mock(Server.class);
        Server server2 = mock(Server.class);
        List<Server> servers = Arrays.asList(server1, server2);

        Flavor flavor1 = mock(Flavor.class);
        Flavor flavor2 = mock(Flavor.class);
        when(flavor1.getId()).thenReturn("1");
        when(flavor2.getId()).thenReturn("2");
        List<Flavor> expectedFlavors = Arrays.asList(flavor1, flavor2);

        when(server1.getFlavor()).thenReturn(flavor1);
        when(server2.getFlavor()).thenReturn(flavor2);

        // Act
        List<Flavor> flavors = heatbridge.extractOpenstackFlavorsFromServers(servers);

        // Assert
        assertEquals(expectedFlavors, flavors);
    }

    @Test
    public void testUpdateVserversToAai() throws HeatBridgeException {
        // Arrange
        Server server1 = mock(Server.class);

        when(server1.getId()).thenReturn("test-server1-id");
        when(server1.getHypervisorHostname()).thenReturn("test-hypervisor");
        when(server1.getName()).thenReturn("test-server1-name");
        when(server1.getStatus()).thenReturn(Status.ACTIVE);
        when(server1.getLinks()).thenReturn(new ArrayList<>());

        Server server2 = mock(Server.class);
        when(server2.getId()).thenReturn("test-server2-id");
        when(server2.getHypervisorHostname()).thenReturn("test-hypervisor");
        when(server2.getName()).thenReturn("test-server2-name");
        when(server2.getStatus()).thenReturn(Status.ACTIVE);
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
        verify(transaction, times(2)).create(captor.capture(), any(Vserver.class));

        List<AAIResourceUri> uris = captor.getAllValues();
        assertEquals(AAIUriFactory.createResourceUri(AAIObjectType.VSERVER, CLOUD_OWNER, REGION_ID, TENANT_ID,
                server1.getId()), uris.get(0));
        assertEquals(AAIUriFactory.createResourceUri(AAIObjectType.VSERVER, CLOUD_OWNER, REGION_ID, TENANT_ID,
                server2.getId()), uris.get(1));

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

    @Ignore
    @Test
    public void testUpdateVserverLInterfacesToAai() throws HeatBridgeException {
        // Arrange
        List<Resource> stackResources = (List<Resource>) extractTestStackResources();
        Port port = mock(Port.class);
        when(port.getId()).thenReturn("test-port-id");
        when(port.getName()).thenReturn("test-port-name");
        when(port.getvNicType()).thenReturn(HeatBridgeConstants.OS_SRIOV_PORT_TYPE);
        when(port.getMacAddress()).thenReturn("78:4f:43:68:e2:78");
        when(port.getNetworkId()).thenReturn("890a203a-23gg-56jh-df67-731656a8f13a");
        when(port.getDeviceId()).thenReturn("test-device-id");
        when(port.getVifDetails()).thenReturn(ImmutableMap.of(HeatBridgeConstants.OS_VLAN_NETWORK_KEY, "2345"));
        String pfPciId = "0000:08:00.0";
        when(port.getProfile()).thenReturn(ImmutableMap.of(HeatBridgeConstants.OS_PCI_SLOT_KEY, pfPciId,
                HeatBridgeConstants.OS_PHYSICAL_NETWORK_KEY, "physical_network_id"));

        Network network = mock(Network.class);
        when(network.getId()).thenReturn("test-network-id");
        when(network.getNetworkType()).thenReturn(NetworkType.VLAN);
        when(network.getProviderSegID()).thenReturn("2345");

        when(osClient.getPortById("212a203a-9764-4f42-84ea-731536a8f13a")).thenReturn(port);
        when(osClient.getPortById("387e3904-8948-43d1-8635-b6c2042b54da")).thenReturn(port);
        when(osClient.getPortById("70a09dfd-f1c5-4bc8-bd8f-dc539b8d662a")).thenReturn(port);
        when(osClient.getPortById("12f88b4d-c8a4-4fbd-bcb4-7e36af02430b")).thenReturn(port);
        when(osClient.getPortById("c54b9f45-b413-4937-bbe4-3c8a5689cfc9")).thenReturn(port);
        when(osClient.getNetworkById(anyString())).thenReturn(network);

        SriovPf sriovPf = new SriovPf();
        sriovPf.setPfPciId(pfPciId);
        PInterface pIf = mock(PInterface.class);
        when(pIf.getInterfaceName()).thenReturn("test-port-id");
        when(resourcesClient.get(eq(PInterface.class), any(AAIResourceUri.class))).thenReturn(Optional.of(pIf));

        // Act
        heatbridge.buildAddVserverLInterfacesToAaiAction(stackResources, Arrays.asList("1", "2"));

        // Assert
        verify(transaction, times(5)).create(any(AAIResourceUri.class), any(LInterface.class));
        verify(osClient, times(5)).getPortById(anyString());
        verify(osClient, times(5)).getNetworkById(anyString());
    }

    private List<? extends Resource> extractTestStackResources() {
        List<HeatResource> stackResources = null;
        try {
            stackResources = MAPPER.readValue(readTestResourceFile("stack-resources.json"), Resources.class).getList();
            assertNotNull(stackResources);
            assertFalse(stackResources.isEmpty());
        } catch (IOException e) {
            Assert.fail("Failed to extract test stack resources.");
        }
        return stackResources;
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
}
