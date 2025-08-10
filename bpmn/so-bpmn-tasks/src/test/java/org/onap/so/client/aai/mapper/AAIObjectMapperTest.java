/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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

package org.onap.so.client.aai.mapper;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.onap.aai.domain.yang.RouteTargets;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CtagAssignment;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.HostRoute;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.NetworkPolicy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.OwningEntity;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Project;
import org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTarget;
import org.onap.so.bpmn.servicedecomposition.bbobjects.SegmentationAssignment;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Subnet;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VpnBinding;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoCollection;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoConfiguration;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoNetwork;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoPnf;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class AAIObjectMapperTest {
    private AAIObjectMapper aaiObjectMapper = new AAIObjectMapper();
    private final static String JSON_FILE_LOCATION = "src/test/resources/__files/BuildingBlocks/";


    @Test
    public void mapConfigurationTest() {
        Configuration configuration = new Configuration();
        configuration.setConfigurationId("configId");
        configuration.setConfigurationName("VNR");
        configuration.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);
        configuration.setManagementOption("managementOption");
        ModelInfoConfiguration modelInfoConfiguration = new ModelInfoConfiguration();
        modelInfoConfiguration.setModelCustomizationId("modelCustId");
        modelInfoConfiguration.setModelInvariantId("modelInvariantId");
        modelInfoConfiguration.setModelVersionId("modelVersionId");
        modelInfoConfiguration.setConfigurationType("5G");
        modelInfoConfiguration.setConfigurationRole("ConfigurationRole");
        configuration.setModelInfoConfiguration(modelInfoConfiguration);

        org.onap.aai.domain.yang.Configuration expectedConfiguration = new org.onap.aai.domain.yang.Configuration();
        expectedConfiguration.setConfigurationId(configuration.getConfigurationId());
        expectedConfiguration.setConfigurationName(configuration.getConfigurationName());
        expectedConfiguration.setConfigurationType(configuration.getModelInfoConfiguration().getConfigurationType());
        expectedConfiguration.setOrchestrationStatus(configuration.getOrchestrationStatus().toString());
        expectedConfiguration.setManagementOption(configuration.getManagementOption());
        expectedConfiguration.setModelInvariantId(configuration.getModelInfoConfiguration().getModelInvariantId());
        expectedConfiguration.setModelVersionId(configuration.getModelInfoConfiguration().getModelVersionId());
        expectedConfiguration
                .setModelCustomizationId(configuration.getModelInfoConfiguration().getModelCustomizationId());
        expectedConfiguration.setConfigurationSubType(configuration.getModelInfoConfiguration().getConfigurationRole());
        expectedConfiguration.setConfigPolicyName(configuration.getModelInfoConfiguration().getPolicyName());

        org.onap.aai.domain.yang.Configuration actualConfiguration = aaiObjectMapper.mapConfiguration(configuration);

        assertThat(actualConfiguration, sameBeanAs(expectedConfiguration));
    }

    @Test
    public void mapVolumeGroupTest() {
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setHeatStackId("heatStackId");
        volumeGroup.setModelInfoVfModule(new ModelInfoVfModule());
        volumeGroup.getModelInfoVfModule().setModelCustomizationUUID("modelCustomizationId");
        volumeGroup.setOrchestrationStatus(OrchestrationStatus.ACTIVE);
        volumeGroup.setVnfType("vnfType");
        volumeGroup.setVolumeGroupId("volumeGroupId");
        volumeGroup.setVolumeGroupName("volumeGroupName");

        org.onap.aai.domain.yang.VolumeGroup expectedVolumeGroup = new org.onap.aai.domain.yang.VolumeGroup();
        expectedVolumeGroup.setHeatStackId(volumeGroup.getHeatStackId());
        expectedVolumeGroup.setModelCustomizationId(volumeGroup.getModelInfoVfModule().getModelCustomizationUUID());
        expectedVolumeGroup.setOrchestrationStatus(volumeGroup.getOrchestrationStatus().toString());
        expectedVolumeGroup
                .setVfModuleModelCustomizationId(volumeGroup.getModelInfoVfModule().getModelCustomizationUUID());
        expectedVolumeGroup.setVnfType(volumeGroup.getVnfType());
        expectedVolumeGroup.setVolumeGroupId(volumeGroup.getVolumeGroupId());
        expectedVolumeGroup.setVolumeGroupName(volumeGroup.getVolumeGroupName());

        org.onap.aai.domain.yang.VolumeGroup actualVolumeGroup = aaiObjectMapper.mapVolumeGroup(volumeGroup);

        assertThat(actualVolumeGroup, sameBeanAs(expectedVolumeGroup));
    }

    @Test
    public void serviceInstanceMap() {
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("SIID");
        serviceInstance.setServiceInstanceName("SINAME");
        serviceInstance.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);
        ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
        modelInfoServiceInstance.setServiceType("SITYPE");
        modelInfoServiceInstance.setServiceRole("SIROLE");
        modelInfoServiceInstance.setServiceFunction("SIFUNCTION");
        modelInfoServiceInstance.setModelInvariantUuid("MIUUID");
        modelInfoServiceInstance.setModelUuid("MUUID");
        modelInfoServiceInstance.setEnvironmentContext("EC");
        modelInfoServiceInstance.setWorkloadContext("WC");
        serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);

        org.onap.aai.domain.yang.ServiceInstance AAIServiceInstance =
                aaiObjectMapper.mapServiceInstance(serviceInstance);

        assertEquals(AAIServiceInstance.getServiceInstanceId(), serviceInstance.getServiceInstanceId());
        assertEquals(AAIServiceInstance.getServiceInstanceName(), serviceInstance.getServiceInstanceName());
        assertEquals(AAIServiceInstance.getOrchestrationStatus().toString(),
                serviceInstance.getOrchestrationStatus().toString());
        assertEquals(AAIServiceInstance.getServiceType(),
                serviceInstance.getModelInfoServiceInstance().getServiceType());
        assertEquals(AAIServiceInstance.getServiceRole(),
                serviceInstance.getModelInfoServiceInstance().getServiceRole());
        assertEquals(AAIServiceInstance.getModelInvariantId(),
                serviceInstance.getModelInfoServiceInstance().getModelInvariantUuid());
        assertEquals(AAIServiceInstance.getModelVersionId(),
                serviceInstance.getModelInfoServiceInstance().getModelUuid());
        assertEquals(AAIServiceInstance.getEnvironmentContext(),
                serviceInstance.getModelInfoServiceInstance().getEnvironmentContext());
        assertEquals(AAIServiceInstance.getWorkloadContext(),
                serviceInstance.getModelInfoServiceInstance().getWorkloadContext());
        assertEquals(AAIServiceInstance.getServiceFunction(),
                serviceInstance.getModelInfoServiceInstance().getServiceFunction());
    }

    @Test
    public void projectMap() {
        Project project = new Project();
        project.setProjectName("abc");

        org.onap.aai.domain.yang.Project AAIProject = aaiObjectMapper.mapProject(project);

        assertEquals(AAIProject.getProjectName(), project.getProjectName());
    }

    @Test
    public void serviceSubscriptionMap() {
        ServiceSubscription serviceSubscription = new ServiceSubscription();
        serviceSubscription.setServiceType("IP-FLEX");
        serviceSubscription.setTempUbSubAccountId("Account-ID");
        org.onap.aai.domain.yang.ServiceSubscription serviceSubscriptionMapped =
                aaiObjectMapper.mapServiceSubscription(serviceSubscription);
        assertNotNull(serviceSubscriptionMapped);
        assertEquals(serviceSubscription.getTempUbSubAccountId(), serviceSubscriptionMapped.getTempUbSubAccountId());
        assertEquals(serviceSubscription.getServiceType(), serviceSubscriptionMapped.getServiceType());
    }

    @Test
    public void owningEntityMap() {
        OwningEntity oe = new OwningEntity();
        oe.setOwningEntityId("abc");
        oe.setOwningEntityName("bbb");

        org.onap.aai.domain.yang.OwningEntity AAIOwningEntity = aaiObjectMapper.mapOwningEntity(oe);

        assertEquals(AAIOwningEntity.getOwningEntityId(), oe.getOwningEntityId());
        assertEquals(AAIOwningEntity.getOwningEntityName(), oe.getOwningEntityName());

    }

    @Test
    public void vnfMap() {
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("a");
        vnf.setVnfName("b");
        vnf.setServiceId("c");
        vnf.setVnfType("d");
        vnf.setProvStatus("e");
        vnf.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);
        ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
        modelInfoGenericVnf.setModelCustomizationUuid("f");
        modelInfoGenericVnf.setModelInvariantUuid("g");
        modelInfoGenericVnf.setModelUuid("h");
        modelInfoGenericVnf.setNfRole("i");
        modelInfoGenericVnf.setNfType("j");
        modelInfoGenericVnf.setNfFunction("k");
        modelInfoGenericVnf.setNfNamingCode("l");
        vnf.setModelInfoGenericVnf(modelInfoGenericVnf);

        org.onap.aai.domain.yang.GenericVnf AAIVnf = aaiObjectMapper.mapVnf(vnf);

        assertEquals(AAIVnf.getVnfId(), vnf.getVnfId());
        assertEquals(AAIVnf.getVnfName(), vnf.getVnfName());
        assertEquals(AAIVnf.getServiceId(), vnf.getServiceId());
        assertEquals(AAIVnf.getVnfType(), vnf.getVnfType());
        assertEquals(AAIVnf.getProvStatus(), vnf.getProvStatus());
        assertEquals(AAIVnf.getOrchestrationStatus().toString(), vnf.getOrchestrationStatus().toString());
        assertEquals(AAIVnf.getModelCustomizationId(), vnf.getModelInfoGenericVnf().getModelCustomizationUuid());
        assertEquals(AAIVnf.getModelInvariantId(), vnf.getModelInfoGenericVnf().getModelInvariantUuid());
        assertEquals(AAIVnf.getModelVersionId(), vnf.getModelInfoGenericVnf().getModelUuid());
        assertEquals(AAIVnf.getModelVersionId(), vnf.getModelInfoGenericVnf().getModelUuid());
        assertEquals(AAIVnf.getNfType(), vnf.getModelInfoGenericVnf().getNfType());
        assertEquals(AAIVnf.getNfFunction(), vnf.getModelInfoGenericVnf().getNfFunction());
        assertEquals(AAIVnf.getNfNamingCode(), vnf.getModelInfoGenericVnf().getNfNamingCode());
    }

    @Test
    public void pnfMap() {
        final String pnfId = "PNF_id1";
        final String pnfName = "PNF_name1";
        final String modelCustomizationId = "8421fe03-fd1b-4bf7-845a-c3fe91edb03e";
        final String modelInvariantId = "341a6f84-2cf9-4942-8f9e-2472ffe4e1d8";
        final String modelVersionId = "b13a0706-46b9-4a98-a9f9-5b28431235e7";
        final OrchestrationStatus orchestrationStatus = OrchestrationStatus.PRECREATED;

        Pnf pnf = new Pnf();
        pnf.setPnfId(pnfId);
        pnf.setPnfName(pnfName);
        pnf.setModelInfoPnf(new ModelInfoPnf());
        pnf.getModelInfoPnf().setModelCustomizationUuid(modelCustomizationId);
        pnf.getModelInfoPnf().setModelInvariantUuid(modelInvariantId);
        pnf.getModelInfoPnf().setModelUuid(modelVersionId);
        pnf.setOrchestrationStatus(orchestrationStatus);

        org.onap.aai.domain.yang.Pnf aaiPnf = aaiObjectMapper.mapPnf(pnf);

        assertEquals(aaiPnf.getPnfId(), pnfId);
        assertEquals(aaiPnf.getPnfName(), pnfName);
        assertEquals(aaiPnf.getModelCustomizationId(), modelCustomizationId);
        assertEquals(aaiPnf.getModelInvariantId(), modelInvariantId);
        assertEquals(aaiPnf.getModelVersionId(), modelVersionId);
        assertEquals(aaiPnf.getOrchestrationStatus(), orchestrationStatus.toString());
    }

    @Test
    public void vfModuleMap() throws Exception {
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("a");
        vfModule.setVfModuleName("b");
        vfModule.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);
        ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
        modelInfoVfModule.setModelCustomizationUUID("f");
        modelInfoVfModule.setModelInvariantUUID("g");
        modelInfoVfModule.setModelUUID("h");
        modelInfoVfModule.setIsBaseBoolean(false);
        vfModule.setModelInfoVfModule(modelInfoVfModule);

        org.onap.aai.domain.yang.VfModule AAIVfModule = aaiObjectMapper.mapVfModule(vfModule);

        String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "aaiVfModuleMap.json")));

        ObjectMapper omapper = new ObjectMapper();
        org.onap.aai.domain.yang.VfModule reqMapper1 =
                omapper.readValue(jsonToCompare, org.onap.aai.domain.yang.VfModule.class);

        assertThat(reqMapper1, sameBeanAs(AAIVfModule));

    }

    @Test
    public void vfModuleBaseMap() throws Exception {
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("a");
        vfModule.setVfModuleName("b");
        vfModule.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);
        ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
        modelInfoVfModule.setModelCustomizationUUID("f");
        modelInfoVfModule.setModelInvariantUUID("g");
        modelInfoVfModule.setModelUUID("h");
        modelInfoVfModule.setIsBaseBoolean(true);
        vfModule.setModelInfoVfModule(modelInfoVfModule);

        org.onap.aai.domain.yang.VfModule AAIVfModule = aaiObjectMapper.mapVfModule(vfModule);

        String jsonToCompare =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "aaiVfModuleBaseMap.json")));

        ObjectMapper omapper = new ObjectMapper();
        org.onap.aai.domain.yang.VfModule reqMapper1 =
                omapper.readValue(jsonToCompare, org.onap.aai.domain.yang.VfModule.class);

        assertThat(reqMapper1, sameBeanAs(AAIVfModule));

    }

    @Test
    public void testMapInstanceGroup() {
        InstanceGroup instanceGroup = new InstanceGroup();
        instanceGroup.setId("test-id");
        instanceGroup.setDescription("test-description");
        instanceGroup.setInstanceGroupName("test-instanceGroupName");
        instanceGroup.setResourceVersion("test-resourceVersion");

        ModelInfoInstanceGroup model = new ModelInfoInstanceGroup();
        model.setFunction("test-function");
        model.setInstanceGroupRole("SUB-INTERFACE");
        model.setType("VNFC");
        model.setModelInvariantUUID("modelInvariantUUID-000");
        model.setModelUUID("modelUUID-000");
        model.setDescription("test-description");
        model.setInstanceGroupRole("SUB-INTERFACE");

        instanceGroup.setModelInfoInstanceGroup(model);


        org.onap.aai.domain.yang.InstanceGroup aaiInstanceGroup = aaiObjectMapper.mapInstanceGroup(instanceGroup);

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = mapper.writeValueAsString(aaiInstanceGroup);
            System.out.println("GGG - json:\n" + json);

        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(aaiInstanceGroup.getId(), instanceGroup.getId());
        assertEquals(aaiInstanceGroup.getDescription(), instanceGroup.getDescription());
        assertEquals(aaiInstanceGroup.getInstanceGroupRole(),
                instanceGroup.getModelInfoInstanceGroup().getInstanceGroupRole());
        assertEquals(aaiInstanceGroup.getModelInvariantId(),
                instanceGroup.getModelInfoInstanceGroup().getModelInvariantUUID());
        assertEquals(aaiInstanceGroup.getModelVersionId(), instanceGroup.getModelInfoInstanceGroup().getModelUUID());
        assertEquals(aaiInstanceGroup.getResourceVersion(), instanceGroup.getResourceVersion());
        assertEquals(aaiInstanceGroup.getInstanceGroupType(), instanceGroup.getModelInfoInstanceGroup().getType());
        assertEquals(aaiInstanceGroup.getInstanceGroupRole(),
                instanceGroup.getModelInfoInstanceGroup().getInstanceGroupRole());
    }

    @Test
    public void mapCustomerTest() {
        Customer customer = new Customer();
        customer.setGlobalCustomerId("globalCustomerId");
        customer.setSubscriberName("subscriberName");
        customer.setSubscriberType("subscriberType");

        org.onap.aai.domain.yang.Customer expectedCustomer = new org.onap.aai.domain.yang.Customer();
        expectedCustomer.setGlobalCustomerId("globalCustomerId");
        expectedCustomer.setSubscriberName("subscriberName");
        expectedCustomer.setSubscriberType("subscriberType");

        org.onap.aai.domain.yang.Customer actualCustomer = aaiObjectMapper.mapCustomer(customer);

        assertThat(actualCustomer, sameBeanAs(expectedCustomer));
    }

    @Test
    public void networkMap() throws Exception {
        L3Network l3Network = new L3Network();
        l3Network.setNetworkId("networkId");
        l3Network.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);
        l3Network.setNetworkName("networkName");
        l3Network.setNetworkRole("networkRole");
        l3Network.setNetworkTechnology("networkTechnology");
        l3Network.setNeutronNetworkId("neutronNetworkId");
        l3Network.setNetworkRoleInstance(0L);
        l3Network.setContrailNetworkFqdn("contrailNetworkFqdn");
        l3Network.setIsBoundToVpn(false);
        l3Network.setIsCascaded(false);
        l3Network.setIsSharedNetwork(false);
        l3Network.setHeatStackId("heatStackId");
        l3Network.setOperationalStatus("operationalStatus");
        l3Network.setPhysicalNetworkName("physicalNetworkName");
        l3Network.setIsProviderNetwork(false);
        l3Network.setSelflink("selflink");
        l3Network.setServiceId("serviceId");

        ModelInfoNetwork modelInfoNetwork = new ModelInfoNetwork();
        modelInfoNetwork.setModelCustomizationUUID("modelCustomizationUUID");
        modelInfoNetwork.setModelInvariantUUID("modelInvariantUUID");
        modelInfoNetwork.setModelUUID("modelUUID");

        l3Network.setModelInfoNetwork(modelInfoNetwork);

        org.onap.aai.domain.yang.L3Network aaiL3Network = aaiObjectMapper.mapNetwork(l3Network);

        ObjectMapper omapper = new ObjectMapper();
        org.onap.aai.domain.yang.L3Network network =
                omapper.readValue(getJson("aaiL3NetworkMapped.json"), org.onap.aai.domain.yang.L3Network.class);

        com.shazam.shazamcrest.MatcherAssert.assertThat(aaiL3Network, sameBeanAs(network));

    }

    @Test
    public void mapCollectionTest() {
        Collection networkCollection = new Collection();
        networkCollection.setId("networkCollectionId");
        ModelInfoCollection modelInfoCollection = new ModelInfoCollection();
        modelInfoCollection.setCollectionFunction("networkCollectionFunction");
        modelInfoCollection.setCollectionRole("networkCollectionRole");
        modelInfoCollection.setCollectionType("networkCollectionType");
        modelInfoCollection.setModelCustomizationUUID("modelCustomizationUUID");
        modelInfoCollection.setModelVersionId("modelVersionId");
        modelInfoCollection.setModelInvariantUUID("modelInvariantUUID");
        networkCollection.setModelInfoCollection(modelInfoCollection);
        networkCollection.setName("networkCollectionName");

        org.onap.aai.domain.yang.Collection expectedCollection = new org.onap.aai.domain.yang.Collection();
        expectedCollection.setCollectionId("networkCollectionId");
        expectedCollection.setCollectionType("networkCollectionType");
        expectedCollection.setCollectionCustomizationId("modelCustomizationUUID");
        expectedCollection.setModelVersionId("modelVersionId");
        expectedCollection.setModelInvariantId("modelInvariantUUID");
        expectedCollection.setCollectionFunction("networkCollectionFunction");
        expectedCollection.setCollectionRole("networkCollectionRole");
        expectedCollection.setCollectionName("networkCollectionName");

        org.onap.aai.domain.yang.Collection actualCollection = aaiObjectMapper.mapCollection(networkCollection);

        assertThat(actualCollection, sameBeanAs(expectedCollection));
    }

    /*
     * Helper method to load JSON data
     */
    private String getJson(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + filename)));
    }

    @Test
    public void mapNetworkTest() throws Exception {
        L3Network l3Network = new L3Network();
        ModelInfoNetwork modelInfoNetwork = new ModelInfoNetwork();
        modelInfoNetwork.setModelCustomizationUUID("modelCustomization_id");
        modelInfoNetwork.setModelInvariantUUID("modelInvariant_id");
        modelInfoNetwork.setModelUUID("modelCustomization_id");
        modelInfoNetwork.setNetworkType("CONTRAIL_EXTERNAL");
        modelInfoNetwork.setNetworkRole("dmz_direct");
        modelInfoNetwork.setNetworkTechnology("contrail");
        l3Network.setModelInfoNetwork(modelInfoNetwork);
        l3Network.setNetworkId("TESTING_ID");
        l3Network.setNetworkName("TESTING_NAME");
        l3Network.setIsBoundToVpn(true);
        l3Network.setServiceId("a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
        l3Network.setNetworkRoleInstance(1L);
        l3Network.setOrchestrationStatus(OrchestrationStatus.CREATED);
        l3Network.setHeatStackId("heatStack_id");
        l3Network.setContrailNetworkFqdn("contrailNetwork_fqdn");
        l3Network.setWidgetModelId("widgetModel_id");
        l3Network.setWidgetModelVersion("widgetModel_version");
        l3Network.setPhysicalNetworkName("physicalNetwork_name");
        l3Network.setIsProviderNetwork(true);
        l3Network.setIsSharedNetwork(false);
        l3Network.setIsExternalNetwork(true);
        l3Network.setSelflink("self_link");
        l3Network.setOperationalStatus("operationalStatus");

        List<Subnet> subnets = new ArrayList<Subnet>();
        Subnet subnet1 = new Subnet();
        subnet1.setSubnetId("57e9a1ff-d14f-4071-a828-b19ae98eb2fc");
        subnet1.setSubnetName("subnetName");
        subnet1.setGatewayAddress("192.168.52.1");
        subnet1.setNetworkStartAddress("192.168.52.0");
        subnet1.setCidrMask("24");
        subnet1.setIpVersion("4");
        subnet1.setOrchestrationStatus(OrchestrationStatus.CREATED);
        subnet1.setIpAssignmentDirection("true");
        subnet1.setDhcpEnabled(true);
        subnet1.setDhcpStart("dhcpStart");
        subnet1.setDhcpEnd("dhcpEnd");
        subnet1.setSubnetRole("subnetRole");
        subnet1.setIpAssignmentDirection("true");
        subnet1.setSubnetSequence(new Integer(3));

        List<HostRoute> hostRoutes = new ArrayList<HostRoute>();
        HostRoute hostRoute1 = new HostRoute();
        hostRoute1.setHostRouteId("string");
        hostRoute1.setRoutePrefix("192.10.16.0/24");
        hostRoute1.setNextHop("192.10.16.100/24");
        hostRoute1.setNextHopType("ip-address");
        HostRoute hostRoute2 = new HostRoute();
        hostRoute2.setHostRouteId("string");
        hostRoute2.setRoutePrefix("192.110.17.0/24");
        hostRoute2.setNextHop("192.110.17.110/24");
        hostRoute2.setNextHopType("ip-address");
        hostRoutes.add(hostRoute1);
        hostRoutes.add(hostRoute2);
        subnet1.getHostRoutes().addAll(hostRoutes);

        subnets.add(subnet1);
        subnets.add(subnet1);
        l3Network.getSubnets().addAll(subnets);

        List<CtagAssignment> ctagAssignments = new ArrayList<CtagAssignment>();
        CtagAssignment ctagAssignment1 = new CtagAssignment();
        ctagAssignment1.setVlanIdInner(1L);
        ctagAssignments.add(ctagAssignment1);
        l3Network.getCtagAssignments().addAll(ctagAssignments);

        List<SegmentationAssignment> segmentationAssignments = new ArrayList<SegmentationAssignment>();
        SegmentationAssignment segmentationAssignment1 = new SegmentationAssignment();
        segmentationAssignment1.setSegmentationId("segmentationId1");
        SegmentationAssignment segmentationAssignment2 = new SegmentationAssignment();
        segmentationAssignment2.setSegmentationId("segmentationId2");
        segmentationAssignments.add(segmentationAssignment1);
        segmentationAssignments.add(segmentationAssignment2);
        l3Network.getSegmentationAssignments().addAll(segmentationAssignments);

        AAIObjectMapper l3NetworkMapper = new AAIObjectMapper();
        org.onap.aai.domain.yang.L3Network v12L3Network = l3NetworkMapper.mapNetwork(l3Network);

        String jsonToCompare =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "aaiL3NetworkMapped_to_aai.json")));
        ObjectMapper omapper = new ObjectMapper();
        org.onap.aai.domain.yang.L3Network network =
                omapper.readValue(jsonToCompare, org.onap.aai.domain.yang.L3Network.class);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonGenerated = ow.writeValueAsString(v12L3Network);
        String jsonExpected = ow.writeValueAsString(network);
        assertEquals(jsonExpected, jsonGenerated);

    }

    @Test
    public void mapToAAISubNetsTest() throws Exception {
        List<Subnet> subnets = new ArrayList<Subnet>();
        Subnet subnet1 = new Subnet();
        subnet1.setSubnetId("57e9a1ff-d14f-4071-a828-b19ae98eb2fc");
        subnet1.setSubnetName("subnetName");
        subnet1.setGatewayAddress("192.168.52.1");
        subnet1.setNetworkStartAddress("192.168.52.0");
        subnet1.setCidrMask("24");
        subnet1.setIpVersion("4");
        subnet1.setOrchestrationStatus(OrchestrationStatus.CREATED);
        subnet1.setIpAssignmentDirection("true");
        subnet1.setDhcpEnabled(true);
        subnet1.setDhcpStart("dhcpStart");
        subnet1.setDhcpEnd("dhcpEnd");
        subnet1.setSubnetRole("subnetRole");
        subnet1.setIpAssignmentDirection("true");
        subnet1.setSubnetSequence(new Integer(3));

        List<HostRoute> hostRoutes = new ArrayList<HostRoute>();
        HostRoute hostRoute1 = new HostRoute();
        hostRoute1.setHostRouteId("string");
        hostRoute1.setRoutePrefix("192.10.16.0/24");
        hostRoute1.setNextHop("192.10.16.100/24");
        hostRoute1.setNextHopType("ip-address");
        HostRoute hostRoute2 = new HostRoute();
        hostRoute2.setHostRouteId("string");
        hostRoute2.setRoutePrefix("192.110.17.0/24");
        hostRoute2.setNextHop("192.110.17.110/24");
        hostRoute2.setNextHopType("ip-address");
        hostRoutes.add(hostRoute1);
        hostRoutes.add(hostRoute2);
        subnet1.getHostRoutes().addAll(hostRoutes);

        subnets.add(subnet1);
        subnets.add(subnet1);

        AAIObjectMapper aaiObjectMapper = new AAIObjectMapper();
        org.onap.aai.domain.yang.Subnets v12Subnets = aaiObjectMapper.mapToAAISubNets(subnets);

        assertEquals(subnets.get(0).getDhcpEnd(), v12Subnets.getSubnet().get(0).getDhcpEnd());
        assertEquals(subnets.get(0).getCidrMask(), v12Subnets.getSubnet().get(0).getCidrMask());

        String jsonToCompare =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "aaiSubnetsMapped_to_aai.json")));
        ObjectMapper omapper = new ObjectMapper();
        org.onap.aai.domain.yang.Subnets subnet =
                omapper.readValue(jsonToCompare, org.onap.aai.domain.yang.Subnets.class);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonGenerated = ow.writeValueAsString(v12Subnets);
        String jsonExpected = ow.writeValueAsString(subnet);
        assertEquals(jsonExpected, jsonGenerated);
    }

    @Test
    public void mapToAAICtagAssignmentListTest() throws Exception {
        List<CtagAssignment> ctagAssignments = new ArrayList<CtagAssignment>();
        CtagAssignment ctagAssignment1 = new CtagAssignment();
        ctagAssignment1.setVlanIdInner(1L);
        ctagAssignments.add(ctagAssignment1);

        AAIObjectMapper aaiObjectMapper = new AAIObjectMapper();
        org.onap.aai.domain.yang.CtagAssignments v12CtagAssingments =
                aaiObjectMapper.mapToAAICtagAssignmentList(ctagAssignments);

        assertEquals(new Long(ctagAssignments.get(0).getVlanIdInner().longValue()),
                new Long(v12CtagAssingments.getCtagAssignment().get(0).getVlanIdInner()));

        String jsonToCompare =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "aaiCtagAssingmentsMapped_to_aai.json")));
        ObjectMapper omapper = new ObjectMapper();
        org.onap.aai.domain.yang.CtagAssignments ctagAssignment =
                omapper.readValue(jsonToCompare, org.onap.aai.domain.yang.CtagAssignments.class);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonGenerated = ow.writeValueAsString(v12CtagAssingments);
        String jsonExpected = ow.writeValueAsString(ctagAssignment);
        assertEquals(jsonExpected, jsonGenerated);
    }

    @Test
    public void mapToAAISegmentationAssignmentListTest() throws Exception {
        List<SegmentationAssignment> segmentationAssignments = new ArrayList<SegmentationAssignment>();
        SegmentationAssignment segmentationAssignment1 = new SegmentationAssignment();
        segmentationAssignment1.setSegmentationId("segmentationId1");
        SegmentationAssignment segmentationAssignment2 = new SegmentationAssignment();
        segmentationAssignment2.setSegmentationId("segmentationId2");
        segmentationAssignments.add(segmentationAssignment1);
        segmentationAssignments.add(segmentationAssignment2);

        AAIObjectMapper aaiObjectMapper = new AAIObjectMapper();
        org.onap.aai.domain.yang.SegmentationAssignments v12SegmentationAssignments =
                aaiObjectMapper.mapToAAISegmentationAssignmentList(segmentationAssignments);

        assertEquals(segmentationAssignments.get(0).getSegmentationId(),
                v12SegmentationAssignments.getSegmentationAssignment().get(0).getSegmentationId());
        assertEquals(segmentationAssignments.get(1).getSegmentationId(),
                v12SegmentationAssignments.getSegmentationAssignment().get(1).getSegmentationId());

        String jsonToCompare = new String(
                Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "aaiSegmentationAssignmentsMapped_to_aai.json")));
        ObjectMapper omapper = new ObjectMapper();
        org.onap.aai.domain.yang.SegmentationAssignments segmentationAssignment =
                omapper.readValue(jsonToCompare, org.onap.aai.domain.yang.SegmentationAssignments.class);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonGenerated = ow.writeValueAsString(v12SegmentationAssignments);
        String jsonExpected = ow.writeValueAsString(segmentationAssignment);
        assertEquals(jsonExpected, jsonGenerated);

    }

    @Test
    public void mapVpnBindingTest() {
        VpnBinding vpnBinding = new VpnBinding();
        vpnBinding.setVpnId("testVpnId");
        vpnBinding.setVpnName("testVpn");
        vpnBinding.setVpnPlatform("AVPN");
        vpnBinding.setCustomerVpnId("testCustomerVpnId");
        vpnBinding.setVpnType("testVpnType");
        vpnBinding.setVpnRegion("testVpnRegion");
        vpnBinding.setRouteDistinguisher("testRD");
        RouteTarget routeTarget = new RouteTarget();
        routeTarget.setRouteTargetRole("testRtRole");
        routeTarget.setGlobalRouteTarget("testGrt");
        vpnBinding.getRouteTargets().add(routeTarget);


        org.onap.aai.domain.yang.VpnBinding expectedVpnBinding = new org.onap.aai.domain.yang.VpnBinding();
        expectedVpnBinding.setVpnId("testVpnId");
        expectedVpnBinding.setVpnName("testVpn");
        expectedVpnBinding.setVpnPlatform("AVPN");
        expectedVpnBinding.setCustomerVpnId("testCustomerVpnId");
        expectedVpnBinding.setVpnType("testVpnType");
        expectedVpnBinding.setVpnRegion("testVpnRegion");
        expectedVpnBinding.setRouteDistinguisher("testRD");

        org.onap.aai.domain.yang.RouteTarget expectedRouteTarget = new org.onap.aai.domain.yang.RouteTarget();
        expectedRouteTarget.setRouteTargetRole("testRtRole");
        expectedRouteTarget.setGlobalRouteTarget("testGrt");

        RouteTargets expectedRouteTargets = new RouteTargets();
        expectedRouteTargets.getRouteTarget().add(expectedRouteTarget);

        expectedVpnBinding.setRouteTargets(expectedRouteTargets);

        org.onap.aai.domain.yang.VpnBinding actualVpnBinding = aaiObjectMapper.mapVpnBinding(vpnBinding);

        assertThat(actualVpnBinding, sameBeanAs(expectedVpnBinding));
    }

    @Test
    public void mapRouteTargetTest() {
        RouteTarget routeTarget = new RouteTarget();
        routeTarget.setRouteTargetRole("testRtRole");
        routeTarget.setGlobalRouteTarget("testGrt");

        org.onap.aai.domain.yang.RouteTarget expectedRouteTarget = new org.onap.aai.domain.yang.RouteTarget();
        expectedRouteTarget.setRouteTargetRole("testRtRole");
        expectedRouteTarget.setGlobalRouteTarget("testGrt");

        org.onap.aai.domain.yang.RouteTarget actualRouteTarget = aaiObjectMapper.mapRouteTarget(routeTarget);

        assertThat(actualRouteTarget, sameBeanAs(expectedRouteTarget));
    }

    @Test
    public void mapSubnetTest() {
        Subnet subnet = new Subnet();
        subnet.setSubnetId("testSubnetId");
        subnet.setOrchestrationStatus(OrchestrationStatus.PENDING);
        subnet.setNeutronSubnetId("testNeutronSubnetId");

        org.onap.aai.domain.yang.Subnet expectedSubnet = new org.onap.aai.domain.yang.Subnet();
        expectedSubnet.setSubnetId("testSubnetId");
        expectedSubnet.setOrchestrationStatus("Pending");
        expectedSubnet.setNeutronSubnetId("testNeutronSubnetId");

        org.onap.aai.domain.yang.Subnet actualSubnet = aaiObjectMapper.mapSubnet(subnet);

        assertThat(actualSubnet, sameBeanAs(expectedSubnet));
    }

    @Test
    public void mapNetworkPolicyTest() {
        NetworkPolicy networkPolicy = new NetworkPolicy();
        networkPolicy.setNetworkPolicyId("testNetworkPolicyId");
        networkPolicy.setNetworkPolicyFqdn("testNetworkPolicyFqdn");
        networkPolicy.setHeatStackId("testHeatStackId");

        org.onap.aai.domain.yang.NetworkPolicy expectedNetworkPolicy = new org.onap.aai.domain.yang.NetworkPolicy();
        expectedNetworkPolicy.setNetworkPolicyId("testNetworkPolicyId");
        expectedNetworkPolicy.setNetworkPolicyFqdn("testNetworkPolicyFqdn");
        expectedNetworkPolicy.setHeatStackId("testHeatStackId");

        org.onap.aai.domain.yang.NetworkPolicy actualNetworkPolicy = aaiObjectMapper.mapNetworkPolicy(networkPolicy);

        assertThat(actualNetworkPolicy, sameBeanAs(expectedNetworkPolicy));
    }
}
