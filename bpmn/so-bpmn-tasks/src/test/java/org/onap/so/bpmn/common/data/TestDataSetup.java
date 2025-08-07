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
package org.onap.so.bpmn.common.data;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.servicedecomposition.bbobjects.AllottedResource;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.LineOfBusiness;
import org.onap.so.bpmn.servicedecomposition.bbobjects.NetworkPolicy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.OwningEntity;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Platform;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Project;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceProxy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Subnet;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VpnBinding;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VpnBondingLink;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.License;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoAllottedResource;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoConfiguration;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoNetwork;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceProxy;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.db.catalog.beans.OrchestrationStatus;

public class TestDataSetup {
    private int collectionCounter;
    private int configurationCounter;
    private int customerCounter;
    private int genericVnfCounter;
    private int instanceGroupCounter;
    private int l3NetworkCounter;
    private int owningEntityCounter;
    private int pnfCounter;
    private int projectCounter;
    private int serviceInstanceCounter;
    private int serviceProxyCounter;
    private int serviceSubscriptionCounter;
    private int vfModuleCounter;
    private int volumeGroupCounter;
    private int vpnBindingCounter;
    private int vpnBondingLinkCounter;

    protected BuildingBlockExecution execution;

    protected GeneralBuildingBlock gBBInput;

    protected HashMap<ResourceKey, String> lookupKeyMap;

    protected ExtractPojosForBB extractPojosForBB = new ExtractPojosForBB();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    protected DelegateExecution delegateExecution;

    @Before
    public void buildingBlockTestDataSetupBefore() {
        collectionCounter = 0;
        configurationCounter = 0;
        customerCounter = 0;
        genericVnfCounter = 0;
        instanceGroupCounter = 0;
        l3NetworkCounter = 0;
        owningEntityCounter = 0;
        pnfCounter = 0;
        projectCounter = 0;
        serviceInstanceCounter = 0;
        serviceProxyCounter = 0;
        serviceSubscriptionCounter = 0;
        vfModuleCounter = 0;
        volumeGroupCounter = 0;
        vpnBindingCounter = 0;
        vpnBondingLinkCounter = 0;

        execution = new DelegateExecutionImpl(new ExecutionImpl());
        execution.setVariable("testProcessKey", "testProcessKeyValue");

        gBBInput = new GeneralBuildingBlock();
        execution.setVariable("gBBInput", gBBInput);

        lookupKeyMap = new HashMap<ResourceKey, String>();
        execution.setVariable("lookupKeyMap", lookupKeyMap);

        ExecutionImpl mockExecutionImpl = mock(ExecutionImpl.class);
        doReturn("test").when(mockExecutionImpl).getProcessInstanceId();

        ExecutionImpl executionImpl = new ExecutionImpl();
        executionImpl.setProcessInstance(mockExecutionImpl);

        delegateExecution = (DelegateExecution) executionImpl;
        delegateExecution.setVariable("testProcessKey", "testProcessKeyValue");
    }

    public Map<String, String> buildUserInput() {
        Map<String, String> userInput = new HashMap<>();
        userInput.put("testUserInputKey", "testUserInputValue");

        return userInput;
    }

    public Map<String, String> setUserInput() {
        Map<String, String> userInput = buildUserInput();

        gBBInput.setUserInput(userInput);

        return userInput;
    }

    public RequestContext buildRequestContext() {
        RequestContext requestContext = new RequestContext();
        requestContext.setMsoRequestId("fb06f44c-c797-4f38-9b17-b4b975344600");
        requestContext.setProductFamilyId("testProductFamilyId");
        requestContext.setRequestorId("testRequestorId");

        requestContext.setUserParams(new HashMap<>());

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("vpnId", "testVpnId");
        dataMap.put("vpnRegion", "testVpnRegion");
        dataMap.put("vpnRt", "testVpnRt");
        dataMap.put("vpnName", "vpnName");
        dataMap.put("vpnRegion", Arrays.asList(new String[] {"USA", "EMEA", "APAC"}));

        HashMap<String, Object> userParams = new HashMap<>();
        userParams.put("vpnData", dataMap);

        List<Map<String, Object>> userParamsList = new ArrayList<>();
        userParamsList.add(userParams);

        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setUserParams(userParamsList);
        requestContext.setRequestParameters(requestParameters);

        return requestContext;
    }

    public RequestContext setRequestContext() {
        RequestContext requestContext = buildRequestContext();

        gBBInput.setRequestContext(requestContext);

        return requestContext;
    }

    public CloudRegion buildCloudRegion() {
        CloudRegion cloudRegion = new CloudRegion();
        cloudRegion.setLcpCloudRegionId("testLcpCloudRegionId");
        cloudRegion.setTenantId("testTenantId");
        cloudRegion.setCloudOwner("testCloudOwner");

        return cloudRegion;
    }

    public CloudRegion setCloudRegion() {
        CloudRegion cloudRegion = buildCloudRegion();

        gBBInput.setCloudRegion(cloudRegion);

        return cloudRegion;
    }

    public OrchestrationContext buildOrchestrationContext() {
        OrchestrationContext orchestrationContext = new OrchestrationContext();

        return orchestrationContext;
    }

    public OrchestrationContext setOrchestrationContext() {
        OrchestrationContext orchestrationContext = buildOrchestrationContext();

        gBBInput.setOrchContext(orchestrationContext);

        return orchestrationContext;
    }

    public Collection buildCollection() {
        collectionCounter++;

        Collection collection = new Collection();
        collection.setId("testId" + collectionCounter);
        collection.setInstanceGroup(buildInstanceGroup());

        return collection;
    }

    public Configuration buildConfiguration() {
        configurationCounter++;

        Configuration configuration = new Configuration();
        configuration.setConfigurationId("testConfigurationId" + configurationCounter);
        configuration.setConfigurationName("testConfigurationName" + configurationCounter);

        ModelInfoConfiguration modelInfoConfiguration = new ModelInfoConfiguration();
        modelInfoConfiguration.setModelVersionId("testModelVersionId" + configurationCounter);
        modelInfoConfiguration.setModelInvariantId("testModelInvariantId" + configurationCounter);
        modelInfoConfiguration.setModelCustomizationId("testModelCustomizationId" + configurationCounter);

        configuration.setModelInfoConfiguration(modelInfoConfiguration);

        return configuration;
    }

    public OwningEntity buildOwningEntity() {
        owningEntityCounter++;

        OwningEntity owningEntity = new OwningEntity();
        owningEntity.setOwningEntityId("testOwningEntityId" + owningEntityCounter);
        owningEntity.setOwningEntityName("testOwningEntityName" + owningEntityCounter);

        return owningEntity;
    }

    public Project buildProject() {
        projectCounter++;

        Project project = new Project();
        project.setProjectName("testProjectName1 , testProjectName2      , testProjectName3" + projectCounter);

        return project;
    }

    public ServiceSubscription buildServiceSubscription() {
        serviceSubscriptionCounter++;

        ServiceSubscription serviceSubscription = new ServiceSubscription();
        serviceSubscription.setTempUbSubAccountId("testTempUbSubAccountId" + serviceSubscriptionCounter);
        serviceSubscription.setServiceType("testServiceType" + serviceSubscriptionCounter);

        return serviceSubscription;
    }

    public Customer buildCustomer() {
        customerCounter++;

        Customer customer = new Customer();
        customer.setGlobalCustomerId("testGlobalCustomerId" + customerCounter);
        customer.setSubscriberType("testSubscriberType" + customerCounter);

        customer.setServiceSubscription(buildServiceSubscription());

        return customer;
    }

    public ServiceInstance buildServiceInstance() {
        serviceInstanceCounter++;

        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("testServiceInstanceId" + serviceInstanceCounter);
        serviceInstance.setServiceInstanceName("testServiceInstanceName" + serviceInstanceCounter);

        ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
        modelInfoServiceInstance.setModelInvariantUuid("testModelInvariantUUID" + serviceInstanceCounter);
        modelInfoServiceInstance.setModelUuid("testModelUUID" + serviceInstanceCounter);
        modelInfoServiceInstance.setModelVersion("testModelVersion" + serviceInstanceCounter);
        modelInfoServiceInstance.setModelName("testModelName" + serviceInstanceCounter);
        modelInfoServiceInstance.setServiceType("testServiceType" + serviceInstanceCounter);
        modelInfoServiceInstance.setServiceRole("testServiceRole" + serviceInstanceCounter);
        serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);

        serviceInstance.setProject(buildProject());

        serviceInstance.setOwningEntity(buildOwningEntity());

        serviceInstance.setCollection(buildCollection());

        serviceInstance.getConfigurations().add(buildConfiguration());

        return serviceInstance;
    }

    public ServiceInstance setServiceInstance() {
        ServiceInstance serviceInstance = buildServiceInstance();

        if (gBBInput.getCustomer() == null) {
            gBBInput.setCustomer(buildCustomer());
        }
        gBBInput.getCustomer().getServiceSubscription().getServiceInstances().add(serviceInstance);
        lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, serviceInstance.getServiceInstanceId());

        return serviceInstance;
    }

    public Customer setCustomer() {
        if (gBBInput.getCustomer() != null)
            return gBBInput.getCustomer();
        Customer customer = new Customer();
        customer.setGlobalCustomerId("testGlobalCustomerId");
        customer.setSubscriberType("testSubscriberType");

        customer.setServiceSubscription(buildServiceSubscription());

        gBBInput.setCustomer(customer);

        return customer;
    }

    public Collection setCollection() {
        Collection collection = new Collection();
        collection.setId("testId");

        ServiceInstance serviceInstance = null;

        try {
            serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
        } catch (BBObjectNotFoundException e) {
            serviceInstance = setServiceInstance();
        }

        serviceInstance.setCollection(collection);

        return collection;
    }

    public InstanceGroup setInstanceGroup() {
        InstanceGroup instanceGroup = new InstanceGroup();
        instanceGroup.setId("testId");
        instanceGroup.setInstanceGroupFunction("testInstanceGroupFunction");

        Collection collection = null;

        try {
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            collection = serviceInstance.getCollection();

            if (collection == null) {
                collection = setCollection();
            }
        } catch (BBObjectNotFoundException e) {
            collection = setCollection();
        }

        collection.setInstanceGroup(instanceGroup);


        return instanceGroup;
    }

    public InstanceGroup setInstanceGroupVnf() {
        InstanceGroup instanceGroup = buildInstanceGroup();

        ServiceInstance serviceInstance = null;

        try {
            serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
        } catch (BBObjectNotFoundException e) {
            serviceInstance = setServiceInstance();
        }

        serviceInstance.getInstanceGroups().add(instanceGroup);
        lookupKeyMap.put(ResourceKey.INSTANCE_GROUP_ID, instanceGroup.getId());

        return instanceGroup;
    }

    public VpnBinding buildVpnBinding() {
        vpnBindingCounter++;

        VpnBinding vpnBinding = new VpnBinding();
        vpnBinding.setVpnId("testVpnId" + vpnBindingCounter);
        vpnBinding.setVpnName("testVpnName" + vpnBindingCounter);
        vpnBinding.setCustomerVpnId("testCustomerVpnId" + vpnBindingCounter);

        return vpnBinding;
    }

    public VpnBinding setVpnBinding() {
        VpnBinding vpnBinding = buildVpnBinding();

        Customer customer = gBBInput.getCustomer();

        if (customer == null) {
            customer = buildCustomer();
        }

        customer.getVpnBindings().add(vpnBinding);
        lookupKeyMap.put(ResourceKey.VPN_ID, vpnBinding.getVpnId());

        return vpnBinding;
    }

    public InstanceGroup buildInstanceGroup() {
        instanceGroupCounter++;

        InstanceGroup instanceGroup = new InstanceGroup();
        instanceGroup.setId("testId" + instanceGroupCounter);
        instanceGroup.setInstanceGroupFunction("testInstanceGroupFunction" + instanceGroupCounter);

        return instanceGroup;
    }

    public L3Network buildL3Network() {
        l3NetworkCounter++;

        L3Network network = new L3Network();
        network.setNetworkId("testNetworkId" + l3NetworkCounter);
        network.setNetworkName("testNetworkName" + l3NetworkCounter);
        network.setNetworkType("testNetworkType" + l3NetworkCounter);

        ModelInfoNetwork modelInfoNetwork = new ModelInfoNetwork();
        modelInfoNetwork.setModelInvariantUUID("testModelInvariantUUID" + l3NetworkCounter);
        modelInfoNetwork.setModelName("testModelName" + l3NetworkCounter);
        modelInfoNetwork.setModelVersion("testModelVersion" + l3NetworkCounter);
        modelInfoNetwork.setModelUUID("testModelUUID" + l3NetworkCounter);
        network.setModelInfoNetwork(modelInfoNetwork);

        Platform platform = new Platform();
        platform.setPlatformName(" testPlatformName, testPlatformName2   ,   testPlatformName3   , testPlatformName4");
        network.setPlatform(platform);

        return network;
    }

    public L3Network setL3Network() {
        L3Network network = buildL3Network();

        ServiceInstance serviceInstance = null;

        try {
            serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
        } catch (BBObjectNotFoundException e) {
            serviceInstance = setServiceInstance();
        }

        serviceInstance.getNetworks().add(network);
        lookupKeyMap.put(ResourceKey.NETWORK_ID, network.getNetworkId());

        return network;
    }

    public GenericVnf buildGenericVnf() {
        genericVnfCounter++;

        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("testVnfId" + genericVnfCounter);
        genericVnf.setVnfName("testVnfName" + genericVnfCounter);
        genericVnf.setVnfType("testVnfType" + genericVnfCounter);
        genericVnf.setIpv4OamAddress("10.222.22.2");

        Platform platform = new Platform();
        platform.setPlatformName(" testPlatformName, testPlatformName2   ,   testPlatformName3   , testPlatformName4");
        genericVnf.setPlatform(platform);

        LineOfBusiness lob = new LineOfBusiness();
        lob.setLineOfBusinessName(
                "  testLineOfBusinessName   , testLineOfBusinessName2,    testLineOfBusinessName3,   testLineOfBusinessName4");
        genericVnf.setLineOfBusiness(lob);

        ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
        modelInfoGenericVnf.setModelName("testModelName" + genericVnfCounter);
        modelInfoGenericVnf.setModelCustomizationUuid("testModelCustomizationUUID" + genericVnfCounter);
        modelInfoGenericVnf.setModelInvariantUuid("testModelInvariantUUID" + genericVnfCounter);
        modelInfoGenericVnf.setModelVersion("testModelVersion" + genericVnfCounter);
        modelInfoGenericVnf.setModelUuid("testModelUUID" + genericVnfCounter);
        modelInfoGenericVnf.setModelInstanceName("testInstanceName" + genericVnfCounter);

        genericVnf.setModelInfoGenericVnf(modelInfoGenericVnf);

        License license = new License();
        List<String> array = new ArrayList<String>();
        array.add("testPoolUuid");
        license.setEntitlementPoolUuids(array);
        genericVnf.setLicense(license);

        return genericVnf;
    }

    public GenericVnf setGenericVnf() {
        GenericVnf genericVnf = buildGenericVnf();

        ServiceInstance serviceInstance = null;

        try {
            serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
        } catch (BBObjectNotFoundException e) {
            serviceInstance = setServiceInstance();
        }

        serviceInstance.getVnfs().add(genericVnf);
        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, genericVnf.getVnfId());

        return genericVnf;
    }

    public VfModule buildVfModule() {
        vfModuleCounter++;

        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("testVfModuleId" + vfModuleCounter);
        vfModule.setVfModuleName("testVfModuleName" + vfModuleCounter);
        vfModule.setModuleIndex(0);
        ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
        modelInfoVfModule.setModelInvariantUUID("testModelInvariantUUID" + vfModuleCounter);
        modelInfoVfModule.setModelVersion("testModelVersion" + vfModuleCounter);
        modelInfoVfModule.setModelUUID("testModelUUID" + vfModuleCounter);
        modelInfoVfModule.setModelName("testModelName" + vfModuleCounter);
        modelInfoVfModule.setModelCustomizationUUID("testModelCustomizationUUID" + vfModuleCounter);
        if (vfModuleCounter == 1) {
            modelInfoVfModule.setIsBaseBoolean(Boolean.TRUE);
        } else {
            modelInfoVfModule.setIsBaseBoolean(Boolean.FALSE);
        }
        vfModule.setModelInfoVfModule(modelInfoVfModule);

        return vfModule;
    }

    public VfModule setVfModule() {
        return setVfModule(true);
    }

    public VfModule setVfModule(boolean addToGenericVnf) {
        VfModule vfModule = buildVfModule();

        GenericVnf genericVnf = null;

        try {
            genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
        } catch (BBObjectNotFoundException e) {
            genericVnf = setGenericVnf();
        }

        if (addToGenericVnf) {
            genericVnf.getVfModules().add(vfModule);
        }
        lookupKeyMap.put(ResourceKey.VF_MODULE_ID, vfModule.getVfModuleId());

        return vfModule;
    }

    public VolumeGroup buildVolumeGroup() {
        volumeGroupCounter++;

        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("testVolumeGroupId" + volumeGroupCounter);
        volumeGroup.setVolumeGroupName("testVolumeGroupName" + volumeGroupCounter);
        volumeGroup.setHeatStackId("testHeatStackId" + volumeGroupCounter);

        return volumeGroup;
    }

    public VolumeGroup setVolumeGroup() {
        VolumeGroup volumeGroup = buildVolumeGroup();

        GenericVnf genericVnf = null;

        try {
            genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
        } catch (BBObjectNotFoundException e) {
            genericVnf = setGenericVnf();
        }

        genericVnf.getVolumeGroups().add(volumeGroup);
        lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, volumeGroup.getVolumeGroupId());

        return volumeGroup;
    }

    public Pnf buildPnf() {
        pnfCounter++;

        Pnf pnf = new Pnf();
        pnf.setPnfId("testPnfId" + pnfCounter);
        pnf.setPnfName("testPnfName" + pnfCounter);

        return pnf;
    }

    public ServiceProxy buildServiceProxy() {
        serviceProxyCounter++;

        ServiceProxy serviceProxy = new ServiceProxy();
        serviceProxy.setServiceInstance(buildServiceInstance());
        serviceProxy.getServiceInstance().getVnfs().add(buildGenericVnf());

        Pnf primaryPnf = buildPnf();
        primaryPnf.setRole("Primary");
        serviceProxy.getServiceInstance().getPnfs().add(primaryPnf);

        Pnf secondaryPnf = buildPnf();
        secondaryPnf.setRole("Secondary");
        serviceProxy.getServiceInstance().getPnfs().add(secondaryPnf);

        return serviceProxy;
    }

    public VpnBondingLink buildVpnBondingLink() {
        vpnBondingLinkCounter++;

        VpnBondingLink vpnBondingLink = new VpnBondingLink();
        vpnBondingLink.setVpnBondingLinkId("testVpnBondingLinkId" + vpnBondingLinkCounter);

        Configuration vnrConfiguration = buildConfiguration();
        vnrConfiguration.setNetwork(buildL3Network());
        vpnBondingLink.setVnrConfiguration(vnrConfiguration);

        vpnBondingLink.setVrfConfiguration(buildConfiguration());

        vpnBondingLink.setInfrastructureServiceProxy(buildServiceProxy());

        vpnBondingLink.setTransportServiceProxy(buildServiceProxy());

        return vpnBondingLink;
    }

    public VpnBondingLink setVpnBondingLink() {
        VpnBondingLink vpnBondingLink = buildVpnBondingLink();

        ServiceInstance serviceInstance = null;

        try {
            serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
        } catch (BBObjectNotFoundException e) {
            serviceInstance = setServiceInstance();
        }

        serviceInstance.getVpnBondingLinks().add(vpnBondingLink);
        lookupKeyMap.put(ResourceKey.VPN_BONDING_LINK_ID, vpnBondingLink.getVpnBondingLinkId());


        return vpnBondingLink;
    }

    public Customer setAvpnCustomer() {
        Customer customer = buildCustomer();

        gBBInput.setCustomer(customer);

        return customer;
    }

    public ServiceProxy setServiceProxy(String uniqueIdentifier, String type) {
        ServiceProxy serviceProxy = new ServiceProxy();
        serviceProxy.setId("testProxyId" + uniqueIdentifier);
        serviceProxy.setType(type);

        ModelInfoServiceProxy modelInfo = new ModelInfoServiceProxy();
        modelInfo.setModelInvariantUuid("testProxyModelInvariantUuid" + uniqueIdentifier);
        modelInfo.setModelName("testProxyModelName" + uniqueIdentifier);
        modelInfo.setModelUuid("testProxyModelUuid" + uniqueIdentifier);
        modelInfo.setModelVersion("testProxyModelVersion" + uniqueIdentifier);
        modelInfo.setModelInstanceName("testProxyInstanceName" + uniqueIdentifier);

        serviceProxy.setModelInfoServiceProxy(modelInfo);

        return serviceProxy;
    }

    public AllottedResource setAllottedResource(String uniqueIdentifier) {
        AllottedResource ar = new AllottedResource();
        ar.setId("testAllottedResourceId" + uniqueIdentifier);

        ModelInfoAllottedResource modelInfo = new ModelInfoAllottedResource();
        modelInfo.setModelInvariantUuid("testAllottedModelInvariantUuid" + uniqueIdentifier);
        modelInfo.setModelName("testAllottedModelName" + uniqueIdentifier);
        modelInfo.setModelUuid("testAllottedModelUuid" + uniqueIdentifier);
        modelInfo.setModelVersion("testAllottedModelVersion" + uniqueIdentifier);
        modelInfo.setModelInstanceName("testAllottedModelInstanceName" + uniqueIdentifier);

        ar.setModelInfoAllottedResource(modelInfo);

        return ar;
    }

    public Configuration setConfiguration() {
        Configuration config = new Configuration();
        config.setConfigurationId("testConfigurationId");
        List<Configuration> configurations = new ArrayList<>();
        configurations.add(config);
        ServiceInstance serviceInstance = new ServiceInstance();
        try {
            serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
        } catch (BBObjectNotFoundException e) {
            serviceInstance = setServiceInstance();
        }
        lookupKeyMap.put(ResourceKey.CONFIGURATION_ID, "testConfigurationId");
        serviceInstance.setConfigurations(configurations);
        return config;
    }

    public Subnet buildSubnet() {
        Subnet subnet = new Subnet();
        subnet.setSubnetId("testSubnetId");
        subnet.setOrchestrationStatus(OrchestrationStatus.PENDING);
        subnet.setNeutronSubnetId("testNeutronSubnetId");
        return subnet;
    }

    public NetworkPolicy buildNetworkPolicy() {
        NetworkPolicy networkPolicy = new NetworkPolicy();
        networkPolicy.setNetworkPolicyId("testNetworkPolicyId");
        networkPolicy.setNetworkPolicyFqdn("testNetworkPolicyFqdn");
        networkPolicy.setHeatStackId("testHeatStackId");
        return networkPolicy;
    }
}
