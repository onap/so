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

package org.onap.so.client.sdnc.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoConfiguration;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;

import org.onap.sdnc.apps.client.model.GenericResourceApiConfigurationinformationConfigurationInformation;
import org.onap.sdnc.apps.client.model.GenericResourceApiGcrequestinputGcRequestInput;
import org.onap.sdnc.apps.client.model.GenericResourceApiParam;
import org.onap.sdnc.apps.client.model.GenericResourceApiParamParam;
import org.onap.sdnc.apps.client.model.GenericResourceApiSdncrequestheaderSdncRequestHeader;
import org.onap.sdnc.apps.client.model.GenericResourceApiServiceinformationServiceInformation;
import org.onap.sdnc.apps.client.model.GenericResourceApiSvcActionEnumeration;
import org.onap.sdnc.apps.client.model.GenericResourceApiVnfinformationVnfInformation;


public class GeneralTopologyObjectMapperTest  extends TestDataSetup{
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
		HashMap<String, String> userParams = new HashMap<String, String>();
		userParams.put("key1", "value1");
		requestContext.setUserParams(userParams);
		requestContext.setProductFamilyId("productFamilyId");

		GenericResourceApiServiceinformationServiceInformation serviceInfo = genObjMapper.buildServiceInformation(serviceInstance, requestContext, customer, true);

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
		GenericResourceApiSdncrequestheaderSdncRequestHeader requestHeader = genObjMapper.buildSdncRequestHeader(SDNCSvcAction.ACTIVATE, "sdncReqId");

		assertEquals(GenericResourceApiSvcActionEnumeration.ACTIVATE, requestHeader.getSvcAction());
		assertEquals("sdncReqId", requestHeader.getSvcRequestId());
	}

	@Test
	public void buildSdncRequestHeaderAssignTest() {
		GenericResourceApiSdncrequestheaderSdncRequestHeader requestHeader = genObjMapper.buildSdncRequestHeader(SDNCSvcAction.ASSIGN, "sdncReqId");

		assertEquals(GenericResourceApiSvcActionEnumeration.ASSIGN, requestHeader.getSvcAction());
		assertEquals("sdncReqId", requestHeader.getSvcRequestId());
	}

	@Test
	public void buildSdncRequestHeaderDeactivateTest() {
		GenericResourceApiSdncrequestheaderSdncRequestHeader requestHeader = genObjMapper.buildSdncRequestHeader(SDNCSvcAction.DEACTIVATE, "sdncReqId");

		assertEquals(GenericResourceApiSvcActionEnumeration.DEACTIVATE, requestHeader.getSvcAction());
		assertEquals("sdncReqId", requestHeader.getSvcRequestId());
	}

	@Test
	public void buildSdncRequestHeaderDeleteTest() {
		GenericResourceApiSdncrequestheaderSdncRequestHeader requestHeader = genObjMapper.buildSdncRequestHeader(SDNCSvcAction.DELETE, "sdncReqId");

		assertEquals(GenericResourceApiSvcActionEnumeration.DELETE, requestHeader.getSvcAction());
		assertEquals("sdncReqId", requestHeader.getSvcRequestId());
	}

	@Test
	public void buildSdncRequestHeaderChangeAssignTest() {
		GenericResourceApiSdncrequestheaderSdncRequestHeader requestHeader = genObjMapper.buildSdncRequestHeader(SDNCSvcAction.CHANGE_ASSIGN, "sdncReqId");

		assertEquals(GenericResourceApiSvcActionEnumeration.CHANGEASSIGN, requestHeader.getSvcAction());
		assertEquals("sdncReqId", requestHeader.getSvcRequestId());
	}

	@Test
	public void buildConfigurationInformationTest_excludesOnapModelInfo() {
		Configuration configuration = new Configuration();
		configuration.setConfigurationId("testConfigurationId");
		configuration.setConfigurationType("VNR");
		configuration.setConfigurationName("VNRCONF");
		GenericResourceApiConfigurationinformationConfigurationInformation configurationInformation =genObjMapper.buildConfigurationInformation(configuration,false);
		assertEquals(configuration.getConfigurationId(),configurationInformation.getConfigurationId());
		assertEquals(configuration.getConfigurationType(),configurationInformation.getConfigurationType());
		assertEquals(configuration.getConfigurationName(),configurationInformation.getConfigurationName());
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

		GenericResourceApiConfigurationinformationConfigurationInformation configurationInformation = genObjMapper.buildConfigurationInformation(configuration,true);

		assertEquals(configuration.getConfigurationId(),configurationInformation.getConfigurationId());
		assertEquals(configuration.getConfigurationType(),configurationInformation.getConfigurationType());
		assertEquals(configuration.getConfigurationName(),configurationInformation.getConfigurationName());
		assertNotNull(configurationInformation.getOnapModelInformation());
		assertEquals(configuration.getModelInfoConfiguration().getModelVersionId(),configurationInformation.getOnapModelInformation().getModelUuid());
		assertEquals(configuration.getModelInfoConfiguration().getModelInvariantId(),configurationInformation.getOnapModelInformation().getModelInvariantUuid());
		assertEquals(configuration.getModelInfoConfiguration().getModelCustomizationId(),configurationInformation.getOnapModelInformation().getModelCustomizationUuid());

	}

	@Test
	public void buildGcRequestInformationTest() {
		GenericVnf vnf = new GenericVnf();
		vnf.setVnfId("TestVnfId");
		GenericResourceApiGcrequestinputGcRequestInput gcRequestInput = genObjMapper.buildGcRequestInformation(vnf,null);
		assertNotNull(gcRequestInput);
		assertEquals(vnf.getVnfId(),gcRequestInput.getVnfId());
		assertNull(gcRequestInput.getInputParameters());
	}

	@Test
	public void buildGcRequestInformationTest_withInputParams() {
		GenericVnf vnf = new GenericVnf();
		vnf.setVnfId("TestVnfId");
		GenericResourceApiParam  genericResourceApiParam =new GenericResourceApiParam();
		genericResourceApiParam.addParamItem(new GenericResourceApiParamParam());
		GenericResourceApiGcrequestinputGcRequestInput gcRequestInput = genObjMapper.buildGcRequestInformation(vnf,genericResourceApiParam);
		assertNotNull(gcRequestInput);
		assertEquals(vnf.getVnfId(),gcRequestInput.getVnfId());
		assertNotNull(gcRequestInput.getInputParameters());
	}
	
	@Test
	public void buildVnfInformationTest_withNullData() {
		GenericVnf vnf = new GenericVnf();
		vnf.setVnfId("TestVnfId");
		ServiceInstance serviceInstance = new ServiceInstance();
		serviceInstance.setServiceInstanceId("serviceInstanceId");
		GenericResourceApiVnfinformationVnfInformation gcRequestInput = genObjMapper.buildVnfInformation(vnf,serviceInstance,true);
		assertNotNull(gcRequestInput);
		assertNull(vnf.getModelInfoGenericVnf());
		assertNull(gcRequestInput.getOnapModelInformation());
		assertEquals(vnf.getVnfId(),gcRequestInput.getVnfId());
		assertNotNull(gcRequestInput.getVnfId());
	}
}
