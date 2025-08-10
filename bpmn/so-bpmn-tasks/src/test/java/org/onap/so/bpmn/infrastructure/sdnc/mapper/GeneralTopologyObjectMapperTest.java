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

package org.onap.so.bpmn.infrastructure.sdnc.mapper;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.onap.sdnc.northbound.client.model.GenericResourceApiConfigurationinformationConfigurationInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiGcrequestinputGcRequestInput;
import org.onap.sdnc.northbound.client.model.GenericResourceApiNetworkinformationNetworkInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiOnapmodelinformationOnapModelInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiParam;
import org.onap.sdnc.northbound.client.model.GenericResourceApiParamParam;
import org.onap.sdnc.northbound.client.model.GenericResourceApiSdncrequestheaderSdncRequestHeader;
import org.onap.sdnc.northbound.client.model.GenericResourceApiServiceinformationServiceInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiSvcActionEnumeration;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVfmoduleinformationVfModuleInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfinformationVnfInformation;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoConfiguration;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoNetwork;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;


public class GeneralTopologyObjectMapperTest extends TestDataSetup {
    @InjectMocks
    private GeneralTopologyObjectMapper genObjMapper = new GeneralTopologyObjectMapper();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() {

    }

    @After
    public void after() {

    }

    @Test
    public void testBuildServiceInformation() {
        // prepare and set service instance
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("serviceInstanceId");
        ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
        modelInfoServiceInstance.setModelInvariantUuid("serviceModelInvariantUuid");
        modelInfoServiceInstance.setModelName("serviceModelName");
        modelInfoServiceInstance.setModelUuid("serviceModelUuid");
        modelInfoServiceInstance.setModelVersion("serviceModelVersion");

        serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);
        // prepare Customer object
        Customer customer = new Customer();
        customer.setGlobalCustomerId("globalCustomerId");
        ServiceSubscription serviceSubscription = new ServiceSubscription();
        serviceSubscription.setServiceType("productFamilyId");
        customer.setServiceSubscription(serviceSubscription);
        // set Customer on service instance
        customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
        //
        RequestContext requestContext = new RequestContext();
        Map<String, Object> userParams = new HashMap<>();
        userParams.put("key1", "value1");
        requestContext.setUserParams(userParams);
        requestContext.setProductFamilyId("productFamilyId");

        GenericResourceApiServiceinformationServiceInformation serviceInfo =
                genObjMapper.buildServiceInformation(serviceInstance, requestContext, customer, true);

        assertEquals("serviceModelInvariantUuid", serviceInfo.getOnapModelInformation().getModelInvariantUuid());
        assertEquals("serviceModelName", serviceInfo.getOnapModelInformation().getModelName());
        assertEquals("serviceModelUuid", serviceInfo.getOnapModelInformation().getModelUuid());
        assertEquals("serviceModelVersion", serviceInfo.getOnapModelInformation().getModelVersion());
        assertNull(serviceInfo.getOnapModelInformation().getModelCustomizationUuid());
        assertEquals("serviceInstanceId", serviceInfo.getServiceInstanceId());
        assertEquals("serviceInstanceId", serviceInfo.getServiceId());
        assertEquals("globalCustomerId", serviceInfo.getGlobalCustomerId());
        assertEquals("productFamilyId", serviceInfo.getSubscriptionServiceType());
    }

    @Test
    public void buildSdncRequestHeaderActivateTest() {
        GenericResourceApiSdncrequestheaderSdncRequestHeader requestHeader =
                genObjMapper.buildSdncRequestHeader(SDNCSvcAction.ACTIVATE, "sdncReqId");

        assertEquals(GenericResourceApiSvcActionEnumeration.ACTIVATE, requestHeader.getSvcAction());
        assertEquals("sdncReqId", requestHeader.getSvcRequestId());
    }

    @Test
    public void buildSdncRequestHeaderAssignTest() {
        GenericResourceApiSdncrequestheaderSdncRequestHeader requestHeader =
                genObjMapper.buildSdncRequestHeader(SDNCSvcAction.ASSIGN, "sdncReqId");

        assertEquals(GenericResourceApiSvcActionEnumeration.ASSIGN, requestHeader.getSvcAction());
        assertEquals("sdncReqId", requestHeader.getSvcRequestId());
    }

    @Test
    public void buildSdncRequestHeaderDeactivateTest() {
        GenericResourceApiSdncrequestheaderSdncRequestHeader requestHeader =
                genObjMapper.buildSdncRequestHeader(SDNCSvcAction.DEACTIVATE, "sdncReqId");

        assertEquals(GenericResourceApiSvcActionEnumeration.DEACTIVATE, requestHeader.getSvcAction());
        assertEquals("sdncReqId", requestHeader.getSvcRequestId());
    }

    @Test
    public void buildSdncRequestHeaderDeleteTest() {
        GenericResourceApiSdncrequestheaderSdncRequestHeader requestHeader =
                genObjMapper.buildSdncRequestHeader(SDNCSvcAction.DELETE, "sdncReqId");

        assertEquals(GenericResourceApiSvcActionEnumeration.DELETE, requestHeader.getSvcAction());
        assertEquals("sdncReqId", requestHeader.getSvcRequestId());
    }

    @Test
    public void buildSdncRequestHeaderChangeAssignTest() {
        GenericResourceApiSdncrequestheaderSdncRequestHeader requestHeader =
                genObjMapper.buildSdncRequestHeader(SDNCSvcAction.CHANGE_ASSIGN, "sdncReqId");

        assertEquals(GenericResourceApiSvcActionEnumeration.CHANGEASSIGN, requestHeader.getSvcAction());
        assertEquals("sdncReqId", requestHeader.getSvcRequestId());
    }

    @Test
    public void buildConfigurationInformationTest_excludesOnapModelInfo() {
        Configuration configuration = new Configuration();
        configuration.setConfigurationId("testConfigurationId");
        configuration.setConfigurationType("VNR");
        configuration.setConfigurationName("VNRCONF");
        GenericResourceApiConfigurationinformationConfigurationInformation configurationInformation =
                genObjMapper.buildConfigurationInformation(configuration, false);
        assertEquals(configuration.getConfigurationId(), configurationInformation.getConfigurationId());
        assertEquals(configuration.getConfigurationType(), configurationInformation.getConfigurationType());
        assertEquals(configuration.getConfigurationName(), configurationInformation.getConfigurationName());
        assertNull(configurationInformation.getOnapModelInformation());
    }

    @Test
    public void buildConfigurationInformationTest_includesOnapModelInfo() {
        Configuration configuration = new Configuration();
        configuration.setConfigurationId("testConfigurationId");
        configuration.setConfigurationType("VNR");
        configuration.setConfigurationName("VNRCONF");
        ModelInfoConfiguration modelInfoConfiguration = new ModelInfoConfiguration();
        modelInfoConfiguration.setModelVersionId("modelVersionId");
        modelInfoConfiguration.setModelInvariantId("modelInvariantId");
        modelInfoConfiguration.setModelCustomizationId("modelCustomizationId");
        configuration.setModelInfoConfiguration(modelInfoConfiguration);

        GenericResourceApiConfigurationinformationConfigurationInformation configurationInformation =
                genObjMapper.buildConfigurationInformation(configuration, true);

        assertEquals(configuration.getConfigurationId(), configurationInformation.getConfigurationId());
        assertEquals(configuration.getConfigurationType(), configurationInformation.getConfigurationType());
        assertEquals(configuration.getConfigurationName(), configurationInformation.getConfigurationName());
        assertNotNull(configurationInformation.getOnapModelInformation());
        assertEquals(configuration.getModelInfoConfiguration().getModelVersionId(),
                configurationInformation.getOnapModelInformation().getModelUuid());
        assertEquals(configuration.getModelInfoConfiguration().getModelInvariantId(),
                configurationInformation.getOnapModelInformation().getModelInvariantUuid());
        assertEquals(configuration.getModelInfoConfiguration().getModelCustomizationId(),
                configurationInformation.getOnapModelInformation().getModelCustomizationUuid());

    }

    @Test
    public void buildGcRequestInformationTest() {
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("TestVnfId");
        GenericResourceApiGcrequestinputGcRequestInput gcRequestInput =
                genObjMapper.buildGcRequestInformation(vnf, null);
        assertNotNull(gcRequestInput);
        assertEquals(vnf.getVnfId(), gcRequestInput.getVnfId());
        assertNull(gcRequestInput.getInputParameters());
    }

    @Test
    public void buildGcRequestInformationTest_withInputParams() {
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("TestVnfId");
        GenericResourceApiParam genericResourceApiParam = new GenericResourceApiParam();
        genericResourceApiParam.addParamItem(new GenericResourceApiParamParam());
        GenericResourceApiGcrequestinputGcRequestInput gcRequestInput =
                genObjMapper.buildGcRequestInformation(vnf, genericResourceApiParam);
        assertNotNull(gcRequestInput);
        assertEquals(vnf.getVnfId(), gcRequestInput.getVnfId());
        assertNotNull(gcRequestInput.getInputParameters());
    }

    @Test
    public void buildVnfInformationTest_withNullData() {
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("TestVnfId");
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("serviceInstanceId");
        GenericResourceApiVnfinformationVnfInformation gcRequestInput =
                genObjMapper.buildVnfInformation(vnf, serviceInstance, true);
        assertNotNull(gcRequestInput);
        assertNull(vnf.getModelInfoGenericVnf());
        assertNull(gcRequestInput.getOnapModelInformation());
        assertEquals(vnf.getVnfId(), gcRequestInput.getVnfId());
        assertNotNull(gcRequestInput.getVnfId());
    }

    @Test
    public void buildNetworkInformationTest() {

        L3Network network = new L3Network();
        ModelInfoNetwork modelInfoNetwork = new ModelInfoNetwork();
        modelInfoNetwork.setModelInvariantUUID("my-uuid");
        modelInfoNetwork.setModelName("my-model-name");
        modelInfoNetwork.setModelVersion("my-model-version");
        modelInfoNetwork.setModelUUID("my-model-uuid");
        modelInfoNetwork.setModelCustomizationUUID("my-customization-uuid");
        network.setModelInfoNetwork(modelInfoNetwork);
        network.setNetworkId("my-network-id");
        network.setNetworkType("my-network-type");
        network.setNetworkTechnology("my-network-technology");

        GenericResourceApiNetworkinformationNetworkInformation networkInformation =
                new GenericResourceApiNetworkinformationNetworkInformation();
        GenericResourceApiOnapmodelinformationOnapModelInformation onapModelInformation =
                new GenericResourceApiOnapmodelinformationOnapModelInformation();
        networkInformation.setNetworkId("my-network-id");
        networkInformation.setNetworkType("my-network-type");
        networkInformation.networkTechnology("my-network-technology");
        networkInformation.setFromPreload(null);
        onapModelInformation.setModelInvariantUuid("my-uuid");
        onapModelInformation.setModelName("my-model-name");
        onapModelInformation.setModelVersion("my-model-version");
        onapModelInformation.setModelUuid("my-model-uuid");
        onapModelInformation.setModelCustomizationUuid("my-customization-uuid");
        networkInformation.setOnapModelInformation(onapModelInformation);

        assertThat(networkInformation, sameBeanAs(genObjMapper.buildNetworkInformation(network)));

    }

    @Test
    public void buildNetworkInformationNoModelTest() {

        L3Network network = new L3Network();
        network.setNetworkId("my-network-id");
        network.setNetworkType("my-network-type");
        network.setNetworkTechnology("my-network-technology");

        GenericResourceApiNetworkinformationNetworkInformation networkInformation =
                new GenericResourceApiNetworkinformationNetworkInformation();
        networkInformation.setNetworkId("my-network-id");
        networkInformation.setNetworkType("my-network-type");
        networkInformation.networkTechnology("my-network-technology");
        networkInformation.setFromPreload(null);


        assertThat(networkInformation, sameBeanAs(genObjMapper.buildNetworkInformation(network)));

    }


    @Test
    public void buildVfModuleInformationTest_withNoModelIsFromPreload() {
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("TestVfModuleId");
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("serviceInstanceId");
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("TestVnfId");
        RequestContext requestContext = new RequestContext();
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setUsePreload(true);
        requestContext.setRequestParameters(requestParameters);
        GenericResourceApiVfmoduleinformationVfModuleInformation gcRequestInput = null;
        try {
            gcRequestInput =
                    genObjMapper.buildVfModuleInformation(vfModule, genericVnf, serviceInstance, requestContext, false);
        } catch (MapperException ex) {

        }
        assertNotNull(gcRequestInput);
        assertNull(vfModule.getModelInfoVfModule());
        assertNull(gcRequestInput.getOnapModelInformation());
        assertEquals(vfModule.getVfModuleId(), gcRequestInput.getVfModuleId());
        assertNotNull(gcRequestInput.getVfModuleId());
        assertTrue(gcRequestInput.getFromPreload());
    }

    @Test
    public void buildVfModuleInformationTest_withNoModelIsNotFromPreload() {
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("TestVfModuleId");
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("serviceInstanceId");
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("TestVnfId");
        RequestContext requestContext = new RequestContext();
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setUsePreload(false);
        requestContext.setRequestParameters(requestParameters);
        GenericResourceApiVfmoduleinformationVfModuleInformation gcRequestInput = null;
        try {
            gcRequestInput =
                    genObjMapper.buildVfModuleInformation(vfModule, genericVnf, serviceInstance, requestContext, false);
        } catch (MapperException ex) {

        }
        assertNotNull(gcRequestInput);
        assertNull(vfModule.getModelInfoVfModule());
        assertNull(gcRequestInput.getOnapModelInformation());
        assertEquals(vfModule.getVfModuleId(), gcRequestInput.getVfModuleId());
        assertNotNull(gcRequestInput.getVfModuleId());
        assertFalse(gcRequestInput.getFromPreload());
    }

    @Test
    public void buildVfModuleInformationTest_withNoModelNoRequestContext() {
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("TestVfModuleId");
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("serviceInstanceId");
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("TestVnfId");
        GenericResourceApiVfmoduleinformationVfModuleInformation gcRequestInput = null;
        try {
            gcRequestInput = genObjMapper.buildVfModuleInformation(vfModule, genericVnf, serviceInstance, null, false);
        } catch (MapperException ex) {

        }
        assertNotNull(gcRequestInput);
        assertNull(vfModule.getModelInfoVfModule());
        assertNull(gcRequestInput.getOnapModelInformation());
        assertEquals(vfModule.getVfModuleId(), gcRequestInput.getVfModuleId());
        assertNotNull(gcRequestInput.getVfModuleId());
        assertTrue(gcRequestInput.getFromPreload());
    }

    @Test
    public void buildVfModuleInformationTest_withNoModelNoRequestParameters() {
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("TestVfModuleId");
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("serviceInstanceId");
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("TestVnfId");
        RequestContext requestContext = new RequestContext();
        GenericResourceApiVfmoduleinformationVfModuleInformation gcRequestInput = null;
        try {
            gcRequestInput =
                    genObjMapper.buildVfModuleInformation(vfModule, genericVnf, serviceInstance, requestContext, false);
        } catch (MapperException ex) {

        }
        assertNotNull(gcRequestInput);
        assertNull(vfModule.getModelInfoVfModule());
        assertNull(gcRequestInput.getOnapModelInformation());
        assertEquals(vfModule.getVfModuleId(), gcRequestInput.getVfModuleId());
        assertNotNull(gcRequestInput.getVfModuleId());
        assertTrue(gcRequestInput.getFromPreload());
    }

    @Test
    public void buildVfModuleInformationTest_withModel() {
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("TestVfModuleId");
        ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
        modelInfoVfModule.setModelInvariantUUID("testModelInvariantUUID");
        modelInfoVfModule.setModelName("testModelName");
        modelInfoVfModule.setModelVersion("testModelVersion");
        modelInfoVfModule.setModelUUID("testModelUUID");
        modelInfoVfModule.setModelCustomizationUUID("testModelCustomizationUUID");
        vfModule.setModelInfoVfModule(modelInfoVfModule);
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("serviceInstanceId");
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("TestVnfId");
        RequestContext requestContext = new RequestContext();
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setUsePreload(true);
        requestContext.setRequestParameters(requestParameters);
        GenericResourceApiVfmoduleinformationVfModuleInformation gcRequestInput = null;
        try {
            gcRequestInput =
                    genObjMapper.buildVfModuleInformation(vfModule, genericVnf, serviceInstance, requestContext, true);
        } catch (MapperException ex) {

        }
        assertNotNull(gcRequestInput);
        assertNotNull(vfModule.getModelInfoVfModule());
        assertNotNull(gcRequestInput.getOnapModelInformation());
        assertEquals(modelInfoVfModule.getModelInvariantUUID(),
                gcRequestInput.getOnapModelInformation().getModelInvariantUuid());
        assertEquals(modelInfoVfModule.getModelName(), gcRequestInput.getOnapModelInformation().getModelName());
        assertEquals(modelInfoVfModule.getModelVersion(), gcRequestInput.getOnapModelInformation().getModelVersion());
        assertEquals(modelInfoVfModule.getModelUUID(), gcRequestInput.getOnapModelInformation().getModelUuid());
        assertEquals(modelInfoVfModule.getModelCustomizationUUID(),
                gcRequestInput.getOnapModelInformation().getModelCustomizationUuid());
        assertEquals(vfModule.getVfModuleId(), gcRequestInput.getVfModuleId());
        assertNotNull(gcRequestInput.getVfModuleId());
        assertTrue(gcRequestInput.getFromPreload());
    }
}
