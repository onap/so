/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.GenericVnfs;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.ServiceInstances;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VfModules;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.bpmn.servicedecomposition.tasks.exceptions.DuplicateNameException;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.SubscriberInfo;

@RunWith(MockitoJUnitRunner.class)
public class AaiResourceIdValidatorTest {

    @Mock
    private BBInputSetupUtils bbInputSetupUtilsMock;

    @InjectMocks
    private AaiResourceIdValidator testedObject;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void validateResourceIdInAAIVnfTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("id123");
        vnf.setModelCustomizationId("1234567");
        Optional<GenericVnf> opVnf = Optional.of(vnf);
        GenericVnf vnf2 = new GenericVnf();
        vnf2.setVnfId("id123");
        vnf2.setModelCustomizationId("222");
        Optional<GenericVnf> opVnf2 = Optional.of(vnf2);
        when(bbInputSetupUtilsMock.getRelatedVnfByNameFromServiceInstance("siId123", "vnfName123")).thenReturn(opVnf);
        when(bbInputSetupUtilsMock.getRelatedVnfByNameFromServiceInstance("siId123", "vnfName222")).thenReturn(opVnf2);
        String id = testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.VNF, "vnfName123", reqDetails,
                workflowResourceIds);
        assertEquals("id123", id);
        String id2 = testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.VNF, "nameTest", reqDetails,
                workflowResourceIds);
        assertEquals("generatedId123", id2);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "generic-vnf with name (vnfName222), same parent and different customization id (222) already exists. The name must be unique."));
        testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.VNF, "vnfName222", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateResourceIdInAAIVnfNotGloballyUniqueTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        GenericVnfs genericVnfs = new GenericVnfs();
        GenericVnf vnf3 = new GenericVnf();
        vnf3.setVnfId("id123");

        genericVnfs.getGenericVnf().add(vnf3);
        when(bbInputSetupUtilsMock.getAAIVnfsGloballyByName("vnfName333")).thenReturn(genericVnfs);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "generic-vnf with name (vnfName333) id (id123) and different parent relationship already exists. The name must be unique."));
        testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.VNF, "vnfName333", reqDetails,
                new WorkflowResourceIds());
    }

    @Test
    public void validateResourceIdInAAINetworkTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");
        L3Network network = new L3Network();
        network.setNetworkId("id123");
        network.setModelCustomizationId("1234567");
        Optional<L3Network> opNetwork = Optional.of(network);
        L3Network network2 = new L3Network();
        network2.setNetworkId("id123");
        network2.setModelCustomizationId("222");
        Optional<L3Network> opNetwork2 = Optional.of(network2);

        when(bbInputSetupUtilsMock.getRelatedNetworkByNameFromServiceInstance("siId123", "name123"))
                .thenReturn(opNetwork);
        when(bbInputSetupUtilsMock.getRelatedNetworkByNameFromServiceInstance("siId123", "networkName222"))
                .thenReturn(opNetwork2);
        String id = testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.NETWORK, "name123", reqDetails,
                workflowResourceIds);
        assertEquals("id123", id);
        String id2 = testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.NETWORK, "111111", reqDetails,
                workflowResourceIds);
        assertEquals("generatedId123", id2);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "l3Network with name (networkName222), same parent and different customization id (222) already exists. The name must be unique."));
        testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.NETWORK, "networkName222", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateNetworkResourceNameExistsInAAITest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        when(bbInputSetupUtilsMock.existsAAINetworksGloballyByName("networkName333")).thenReturn(true);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "l3Network with name (networkName333) id (siId123) and different parent relationship already exists. The name must be unique."));
        testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.NETWORK, "networkName333", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateResourceIdInAAIVfModuleTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setVnfId("id123");

        GenericVnf vnf = new GenericVnf();
        VfModules vfModules = new VfModules();
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("id123");
        vfModule.setVfModuleName("name123");
        vfModule.setModelCustomizationId("1234567");
        vfModules.getVfModule().add(vfModule);
        vnf.setVfModules(vfModules);

        when(bbInputSetupUtilsMock.getAAIGenericVnf("id123")).thenReturn(vnf);
        String id = testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.VFMODULE, "name123", reqDetails,
                workflowResourceIds);
        assertEquals("id123", id);

        GenericVnf vnf1 = new GenericVnf();
        VfModules vfModules2 = new VfModules();
        VfModule vfModule2 = new VfModule();
        vfModule2.setVfModuleName("vFModName222");
        vfModule2.setModelCustomizationId("222");
        vfModules2.getVfModule().add(vfModule2);
        vnf1.setVfModules(vfModules2);
        workflowResourceIds.setVnfId("id111");
        when(bbInputSetupUtilsMock.getAAIGenericVnf("id111")).thenReturn(vnf1);
        String id2 = testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.VFMODULE, "111111", reqDetails,
                workflowResourceIds);
        assertEquals("generatedId123", id2);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "vfModule with name (vFModName222), same parent and different customization id (1234567) already exists. The name must be unique."));
        testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.VFMODULE, "vFModName222", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateResourceIdInAAIVfModuleNotGloballyUniqueTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();

        when(bbInputSetupUtilsMock.existsAAIVfModuleGloballyByName("vFModName333")).thenReturn(true);
        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(
                containsString("vfModule with name vFModName333 already exists. The name must be unique."));
        testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.VFMODULE, "vFModName333", reqDetails,
                new WorkflowResourceIds());
    }

    @Test
    public void validateResourceIdInAAIVolumeGroupTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setVnfId("id123");
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("id123");
        volumeGroup.setVolumeGroupName("name123");
        volumeGroup.setVfModuleModelCustomizationId("1234567");
        Optional<VolumeGroup> opVolumeGroup = Optional.of(volumeGroup);

        when(bbInputSetupUtilsMock.getRelatedVolumeGroupByNameFromVnf("id123", "name123")).thenReturn(opVolumeGroup);
        String id = testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.VOLUMEGROUP, "name123",
                reqDetails, workflowResourceIds);
        assertEquals("id123", id);

        String id2 = testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.VOLUMEGROUP, "111111",
                reqDetails, workflowResourceIds);
        assertEquals("generatedId123", id2);
    }


    @Test
    public void validateSourceIdInAAIVolumeGroupNotGloballyUniqueTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        when(bbInputSetupUtilsMock.existsAAIVolumeGroupGloballyByName("testVolumeGroup")).thenReturn(true);
        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(
                containsString("volumeGroup with name testVolumeGroup already exists. The name must be unique."));
        testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.VOLUMEGROUP, "testVolumeGroup", reqDetails,
                new WorkflowResourceIds());
    }

    @Test
    public void validateResourceIdInAAIConfigurationTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        org.onap.aai.domain.yang.Configuration configuration = new org.onap.aai.domain.yang.Configuration();
        configuration.setConfigurationId("id123");
        configuration.setModelCustomizationId("1234567");
        Optional<org.onap.aai.domain.yang.Configuration> opConfiguration = Optional.of(configuration);

        org.onap.aai.domain.yang.Configuration configuration2 = new org.onap.aai.domain.yang.Configuration();
        configuration2.setConfigurationId("id123");
        configuration2.setModelCustomizationId("222");
        Optional<org.onap.aai.domain.yang.Configuration> opConfiguration2 = Optional.of(configuration2);

        when(bbInputSetupUtilsMock.getRelatedConfigurationByNameFromServiceInstance("siId123", "name123"))
                .thenReturn(opConfiguration);
        String id = testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.CONFIGURATION, "name123",
                reqDetails, workflowResourceIds);
        assertEquals("id123", id);

        String id2 = testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.CONFIGURATION, "111111",
                reqDetails, workflowResourceIds);
        assertEquals("generatedId123", id2);

        when(bbInputSetupUtilsMock.getRelatedConfigurationByNameFromServiceInstance("siId123", "name222"))
                .thenReturn(opConfiguration2);
        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "configuration with name (name222), same parent and different customization id (id123) already exists. The name must be unique."));
        testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.CONFIGURATION, "name222", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateResourceIdInAAIConfigurationNotGloballyUniqueTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        when(bbInputSetupUtilsMock.existsAAIConfigurationGloballyByName("testConfig")).thenReturn(true);
        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(
                containsString("configuration with name testConfig already exists. The name must be unique."));
        testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.CONFIGURATION, "testConfig", reqDetails,
                new WorkflowResourceIds());
    }

    @Test
    public void validateResourceIdInAAISITest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        reqDetails.getModelInfo().setModelVersionId("1234567");

        ServiceInstance si = new ServiceInstance();
        si.setServiceInstanceId("siId123");
        si.setModelVersionId("1234567");
        ServiceInstances serviceInstances = new ServiceInstances();
        serviceInstances.getServiceInstance().add(si);
        Optional<ServiceInstance> siOp = Optional.of(si);
        ServiceInstance si2 = new ServiceInstance();
        si2.setServiceInstanceId("siId222");
        si2.setModelVersionId("22222");
        si2.setServiceInstanceName("siName222");
        Optional<ServiceInstance> siOp2 = Optional.of(si2);
        ServiceInstances serviceInstances2 = new ServiceInstances();
        serviceInstances2.getServiceInstance().add(si2);

        when(bbInputSetupUtilsMock.getAAIServiceInstanceByName("id123", "subServiceType123", "siName123"))
                .thenReturn(siOp);
        when(bbInputSetupUtilsMock.getAAIServiceInstanceByName("id123", "subServiceType123", "siName222"))
                .thenReturn(siOp2);
        String id = testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.SERVICE, "siName123",
                reqDetails, new WorkflowResourceIds());
        assertEquals("siId123", id);
        String id2 = testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.SERVICE, "111111", reqDetails,
                new WorkflowResourceIds());
        assertEquals("generatedId123", id2);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "serviceInstance with name (siName222) and different version id (1234567) already exists. The name must be unique."));
        testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.SERVICE, "siName222", reqDetails,
                new WorkflowResourceIds());
    }

    @Test
    public void validateResourceIdInAAIMultipleSITest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        reqDetails.getModelInfo().setModelVersionId("1234567");
        ServiceInstance si = new ServiceInstance();
        ServiceInstances serviceInstances = new ServiceInstances();
        serviceInstances.getServiceInstance().add(si);
        ServiceInstance si2 = new ServiceInstance();
        serviceInstances.getServiceInstance().add(si2);
        when(bbInputSetupUtilsMock.getAAIServiceInstancesGloballyByName("siName123")).thenReturn(serviceInstances);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "serviceInstance with name (siName123) and multiple combination of model-version-id + service-type + global-customer-id already exists. The name must be unique."));
        testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.SERVICE, "siName123", reqDetails,
                new WorkflowResourceIds());
    }

    @Test
    public void validateResourceIdInAAISIExistsTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        reqDetails.getModelInfo().setModelVersionId("1234567");

        ServiceInstance si = new ServiceInstance();
        si.setServiceInstanceId("siId123");
        si.setModelVersionId("1234567");
        ServiceInstances serviceInstances = new ServiceInstances();
        serviceInstances.getServiceInstance().add(si);

        Map<String, String> uriKeys = new HashMap<>();
        uriKeys.put(AAIFluentTypeBuilder.Types.CUSTOMER.getUriParams().globalCustomerId, "globalCustomerId");
        uriKeys.put(AAIFluentTypeBuilder.Types.SERVICE_SUBSCRIPTION.getUriParams().serviceType, "serviceType");

        when(bbInputSetupUtilsMock.getAAIServiceInstancesGloballyByName("siName123")).thenReturn(serviceInstances);
        when(bbInputSetupUtilsMock.getURIKeysFromServiceInstance("siId123")).thenReturn(uriKeys);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "serviceInstance with name (siName123) and global-customer-id (globalCustomerId), service-type (serviceType), model-version-id (1234567) already exists. The name must be unique."));
        testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.SERVICE, "siName123", reqDetails,
                new WorkflowResourceIds());
    }

    @Test
    public void validateServiceResourceIdInAAINoDupTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        String id = testedObject.validateServiceResourceIdInAAI("generatedId123", "siName123", reqDetails);
        assertEquals("generatedId123", id);
    }

    @Test
    public void validateServiceResourceIdInAAISameModelVersionId() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        reqDetails.getModelInfo().setModelVersionId("1234567");

        ServiceInstance si = new ServiceInstance();
        si.setServiceInstanceId("siId123");
        si.setModelVersionId("1234567");
        Optional<ServiceInstance> siOp = Optional.of(si);

        when(bbInputSetupUtilsMock.getAAIServiceInstanceByName("id123", "subServiceType123", "siName123"))
                .thenReturn(siOp);
        String id = testedObject.validateServiceResourceIdInAAI("generatedId123", "siName123", reqDetails);
        assertEquals("siId123", id);
    }

    @Test
    public void validateServiceResourceIdInAAIDifferentModelVersionId() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        reqDetails.getModelInfo().setModelVersionId("1234567");

        ServiceInstance si = new ServiceInstance();
        si.setModelVersionId("9999999");
        ServiceInstances serviceInstances = new ServiceInstances();
        serviceInstances.getServiceInstance().add(si);
        Optional<ServiceInstance> siOp = Optional.of(si);

        when(bbInputSetupUtilsMock.getAAIServiceInstanceByName("id123", "subServiceType123", "siName123"))
                .thenReturn(siOp);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "serviceInstance with name (siName123) and different version id (1234567) already exists. The name must be unique."));

        String id = testedObject.validateServiceResourceIdInAAI("generatedId123", "siName123", reqDetails);
        assertEquals("siId123", id);
    }

    @Test
    public void validateServiceResourceIdInAAIDuplicateNameTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        ServiceInstance si = new ServiceInstance();
        si.setModelVersionId("1234567");
        ServiceInstances serviceInstances = new ServiceInstances();
        serviceInstances.getServiceInstance().add(si);

        when(bbInputSetupUtilsMock.getAAIServiceInstancesGloballyByName("siName")).thenReturn(serviceInstances);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "serviceInstance with name (siName) and global-customer-id (null), service-type (null), model-version-id (1234567) already exists. The name must be unique."));

        testedObject.validateServiceResourceIdInAAI("generatedId123", "siName", reqDetails);
    }

    @Test
    public void validateServiceResourceIdInAAIDuplicateNameMultipleTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        ServiceInstances serviceInstances = new ServiceInstances();
        serviceInstances.getServiceInstance().add(new ServiceInstance());
        serviceInstances.getServiceInstance().add(new ServiceInstance());

        when(bbInputSetupUtilsMock.getAAIServiceInstancesGloballyByName("siName")).thenReturn(serviceInstances);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "serviceInstance with name (siName) and multiple combination of model-version-id + service-type + global-customer-id already exists. The name must be unique."));

        testedObject.validateServiceResourceIdInAAI("generatedId123", "siName", reqDetails);
    }

    @Test
    public void validateNetworkResourceIdInAAITest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        String id = testedObject.validateNetworkResourceIdInAAI("generatedId123", "name123", reqDetails,
                new WorkflowResourceIds());
        assertEquals("generatedId123", id);
    }

    @Test
    public void validateNetworkResourceIdInAAISameModelCustIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");
        L3Network network = new L3Network();
        network.setNetworkId("id123");
        network.setModelCustomizationId("1234567");
        Optional<L3Network> opNetwork = Optional.of(network);

        when(bbInputSetupUtilsMock.getRelatedNetworkByNameFromServiceInstance("siId123", "name123"))
                .thenReturn(opNetwork);

        String id = testedObject.validateNetworkResourceIdInAAI("generatedId123", "name123", reqDetails,
                workflowResourceIds);
        assertEquals("id123", id);
    }

    @Test
    public void validateNetworkResourceIdInAAIDuplicateNameTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");
        L3Network network = new L3Network();
        network.setModelCustomizationId("9999999");
        Optional<L3Network> opNetwork = Optional.of(network);

        when(bbInputSetupUtilsMock.getRelatedNetworkByNameFromServiceInstance("siId123", "name123"))
                .thenReturn(opNetwork);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "l3Network with name (name123), same parent and different customization id (9999999) already exists. The name must be unique."));

        testedObject.validateNetworkResourceIdInAAI("generatedId123", "name123", reqDetails, workflowResourceIds);
    }

    @Test
    public void validateNetworkResourceIdInAAINotGloballyUniqueTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        when(bbInputSetupUtilsMock.existsAAINetworksGloballyByName("name123")).thenReturn(true);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "l3Network with name (name123) id (siId123) and different parent relationship already exists. The name must be unique."));

        testedObject.validateNetworkResourceIdInAAI("generatedId123", "name123", reqDetails, workflowResourceIds);
    }

    @Test
    public void validateVnfResourceIdInAAITest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        String id = testedObject.validateVnfResourceIdInAAI("generatedId123", "vnfName123", reqDetails,
                new WorkflowResourceIds());
        assertEquals("generatedId123", id);
    }

    @Test
    public void validateVnfResourceIdInAAISameModelCustomizationIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("id123");
        vnf.setModelCustomizationId("1234567");
        Optional<GenericVnf> opVnf = Optional.of(vnf);

        when(bbInputSetupUtilsMock.getRelatedVnfByNameFromServiceInstance("siId123", "vnfName123")).thenReturn(opVnf);
        String id = testedObject.validateVnfResourceIdInAAI("generatedId123", "vnfName123", reqDetails,
                workflowResourceIds);
        assertEquals("id123", id);
    }

    @Test
    public void validateVnfResourceIdInAAIDiffModelCustomizationIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");
        GenericVnf vnf = new GenericVnf();
        vnf.setModelCustomizationId("9999999");
        Optional<GenericVnf> opVnf = Optional.of(vnf);

        when(bbInputSetupUtilsMock.getRelatedVnfByNameFromServiceInstance("siId123", "vnfName123")).thenReturn(opVnf);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "generic-vnf with name (vnfName123), same parent and different customization id (9999999) already exists. The name must be unique."));

        testedObject.validateVnfResourceIdInAAI("generatedId123", "vnfName123", reqDetails, workflowResourceIds);
    }

    @Test
    public void validateVnfResourceIdInAAINotGloballyUniqueTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("id123");
        GenericVnfs genericVnfs = new GenericVnfs();
        genericVnfs.getGenericVnf().add(vnf);

        when(bbInputSetupUtilsMock.getAAIVnfsGloballyByName("vnfName123")).thenReturn(genericVnfs);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "generic-vnf with name (vnfName123) id (id123) and different parent relationship already exists. The name must be unique."));

        testedObject.validateVnfResourceIdInAAI("generatedId123", "vnfName123", reqDetails, new WorkflowResourceIds());
    }

    @Test
    public void validateVfModuleResourceIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        String id = testedObject.validateVfModuleResourceIdInAAI("generatedId123", "name123", reqDetails,
                new WorkflowResourceIds());
        assertEquals("generatedId123", id);
    }

    @Test
    public void validateVfModuleResourceIdSameModelCustIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setVnfId("vnfId123");
        VfModules vfModules = new VfModules();
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("id123");
        vfModule.setVfModuleName("name123");
        vfModule.setModelCustomizationId("1234567");
        vfModules.getVfModule().add(vfModule);
        GenericVnf vnf = new GenericVnf();
        vnf.setVfModules(vfModules);

        when(bbInputSetupUtilsMock.getAAIGenericVnf("vnfId123")).thenReturn(vnf);

        String id = testedObject.validateVfModuleResourceIdInAAI("generatedId123", "name123", reqDetails,
                workflowResourceIds);
        assertEquals("id123", id);
    }

    @Test
    public void validateVfModuleResourceIdDifferentModelCustIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setVnfId("vnfId123");
        VfModules vfModules = new VfModules();
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleName("name123");
        vfModule.setModelCustomizationId("9999999");
        vfModules.getVfModule().add(vfModule);
        GenericVnf vnf = new GenericVnf();
        vnf.setVfModules(vfModules);

        when(bbInputSetupUtilsMock.getAAIGenericVnf("vnfId123")).thenReturn(vnf);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "vfModule with name (name123), same parent and different customization id (1234567) already exists. The name must be unique."));

        testedObject.validateVfModuleResourceIdInAAI("generatedId123", "name123", reqDetails, workflowResourceIds);
    }

    @Test
    public void validateVfModuleResourceIdNotGloballyUniqueTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        when(bbInputSetupUtilsMock.existsAAIVfModuleGloballyByName("name123")).thenReturn(true);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException
                .expectMessage(containsString("vfModule with name name123 already exists. The name must be unique."));

        testedObject.validateVfModuleResourceIdInAAI("generatedId123", "name123", reqDetails, workflowResourceIds);
    }

    @Test
    public void validateVolumeGroupResourceIdInAAITest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        String id = testedObject.validateVolumeGroupResourceIdInAAI("generatedId123", "name123", reqDetails,
                new WorkflowResourceIds());
        assertEquals("generatedId123", id);
    }

    @Test
    public void validateVolumeGroupResourceIdInAAISameModelCustIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setVnfId("vnfId123");
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("id123");
        volumeGroup.setVfModuleModelCustomizationId("1234567");
        Optional<VolumeGroup> opVolumeGroup = Optional.of(volumeGroup);

        when(bbInputSetupUtilsMock.getRelatedVolumeGroupByNameFromVnf("vnfId123", "name123")).thenReturn(opVolumeGroup);
        String id = testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.VOLUMEGROUP, "name123",
                reqDetails, workflowResourceIds);

        assertEquals("id123", id);
    }

    @Test
    public void validateVolumeGroupResourceIdInAAIDifferentModelCustIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setVnfId("vnfId123");
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupName("name123");
        volumeGroup.setVfModuleModelCustomizationId("9999999");
        Optional<VolumeGroup> opVolumeGroup = Optional.of(volumeGroup);

        when(bbInputSetupUtilsMock.getRelatedVolumeGroupByNameFromVnf("vnfId123", "name123")).thenReturn(opVolumeGroup);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(
                containsString("volumeGroup with name name123 already exists. The name must be unique."));

        testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.VOLUMEGROUP, "name123", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateVolumeGroupResourceIdInAAINotGloballyUniqueTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        when(bbInputSetupUtilsMock.existsAAIVolumeGroupGloballyByName("name123")).thenReturn(true);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(
                containsString("volumeGroup with name name123 already exists. The name must be unique."));

        testedObject.validateResourceIdInAAI("generatedId123", WorkflowType.VOLUMEGROUP, "name123", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateConfigurationResourceIdInAAITest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        String id = testedObject.validateConfigurationResourceIdInAAI("generatedId123", "name123", reqDetails,
                new WorkflowResourceIds());
        assertEquals("generatedId123", id);
    }

    @Test
    public void validateConfigurationResourceIdInAAISameModelCustIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");
        org.onap.aai.domain.yang.Configuration configuration = new org.onap.aai.domain.yang.Configuration();
        configuration.setConfigurationId("id123");
        configuration.setModelCustomizationId("1234567");
        Optional<org.onap.aai.domain.yang.Configuration> opConfiguration = Optional.of(configuration);

        when(bbInputSetupUtilsMock.getRelatedConfigurationByNameFromServiceInstance("siId123", "name123"))
                .thenReturn(opConfiguration);

        String id = testedObject.validateConfigurationResourceIdInAAI("generatedId123", "name123", reqDetails,
                workflowResourceIds);
        assertEquals("id123", id);
    }

    @Test
    public void validateConfigurationResourceIdInAAIDifferentModelCustIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");
        org.onap.aai.domain.yang.Configuration configuration = new org.onap.aai.domain.yang.Configuration();
        configuration.setConfigurationId("id123");
        configuration.setModelCustomizationId("9999999");
        Optional<org.onap.aai.domain.yang.Configuration> opConfiguration = Optional.of(configuration);

        when(bbInputSetupUtilsMock.getRelatedConfigurationByNameFromServiceInstance("siId123", "name123"))
                .thenReturn(opConfiguration);
        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "configuration with name (name123), same parent and different customization id (id123) already exists. The name must be unique."));

        testedObject.validateConfigurationResourceIdInAAI("generatedId123", "name123", reqDetails, workflowResourceIds);
    }

    @Test
    public void validateConfigurationResourceIdInAAINotGloballyUniqueTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();

        when(bbInputSetupUtilsMock.existsAAIConfigurationGloballyByName("name123")).thenReturn(true);
        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(
                containsString("configuration with name name123 already exists. The name must be unique."));

        testedObject.validateConfigurationResourceIdInAAI("generatedId123", "name123", reqDetails, workflowResourceIds);
    }

    private RequestDetails setupRequestDetails() {
        RequestDetails reqDetails = new RequestDetails();
        SubscriberInfo subInfo = new SubscriberInfo();
        subInfo.setGlobalSubscriberId("id123");
        reqDetails.setSubscriberInfo(subInfo);
        RequestParameters reqParams = new RequestParameters();
        reqParams.setSubscriptionServiceType("subServiceType123");
        reqDetails.setRequestParameters(reqParams);
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationId("1234567");
        reqDetails.setModelInfo(modelInfo);
        return reqDetails;
    }
}
