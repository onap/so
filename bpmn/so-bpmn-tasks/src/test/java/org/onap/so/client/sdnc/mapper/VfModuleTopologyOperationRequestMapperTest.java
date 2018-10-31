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

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;

import org.onap.sdnc.northbound.client.model.GenericResourceApiVfModuleOperationInformation;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VfModuleTopologyOperationRequestMapperTest {

	private final static String JSON_FILE_LOCATION = "src/test/resources/__files/BuildingBlocks/";
	private final static String ERRORMESSAGE = "VF Module model info is null for testVfModuleId";
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void assignGenericResourceApiVfModuleInformationTest() throws Exception {

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

		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		//
		RequestContext requestContext = new RequestContext();
		HashMap<String,String> userParams = new HashMap<String,String>();
		userParams.put("key1", "value1");		
		requestContext.setUserParams(userParams);
		requestContext.setProductFamilyId("productFamilyId");

		GenericVnf vnf = new GenericVnf();
		vnf.setVnfId("testVnfId");
		vnf.setVnfType("testVnfType");
		vnf.setVnfName("testVnfName");
		ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
		modelInfoGenericVnf.setModelInvariantUuid("vnfModelInvariantUuid");
		modelInfoGenericVnf.setModelName("vnfModelName");
		modelInfoGenericVnf.setModelVersion("vnfModelVersion");
		modelInfoGenericVnf.setModelUuid("vnfModelUuid");
		modelInfoGenericVnf.setModelCustomizationUuid("vnfModelCustomizationUuid");
		vnf.setModelInfoGenericVnf(modelInfoGenericVnf);

		VfModule vfModule = new VfModule();
		vfModule.setVfModuleId("testVfModuleId");
		vfModule.setVfModuleName("testVfModuleName");
		ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
		modelInfoVfModule.setModelInvariantUUID("vfModuleModelInvariantUuid");
		modelInfoVfModule.setModelName("vfModuleModelName");
		modelInfoVfModule.setModelVersion("vfModuleModelVersion");
		modelInfoVfModule.setModelUUID("vfModuleModelUuid");
		modelInfoVfModule.setModelCustomizationUUID("vfModuleModelCustomizationUuid");
		vfModule.setModelInfoVfModule(modelInfoVfModule);
		HashMap<String, String> cloudParams = new HashMap<String, String>();
		cloudParams.put("key2", "value2");
		vfModule.setCloudParams(cloudParams);

		VolumeGroup volumeGroup = new VolumeGroup();
		volumeGroup.setVolumeGroupId("volumeGroupId");
		volumeGroup.setVolumeGroupName("volumeGroupName");

		CloudRegion cloudRegion = new CloudRegion();

		VfModuleTopologyOperationRequestMapper mapper = new VfModuleTopologyOperationRequestMapper();
		GenericResourceApiVfModuleOperationInformation vfModuleSDNCrequest = mapper.reqMapper(
				SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION, SDNCSvcAction.ASSIGN, vfModule, volumeGroup, vnf, serviceInstance, customer,
				cloudRegion, requestContext, null);

		String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleOperationInformationAssign.json")));

		ObjectMapper omapper = new ObjectMapper();
		GenericResourceApiVfModuleOperationInformation reqMapper1 = omapper.readValue(
				jsonToCompare,
				GenericResourceApiVfModuleOperationInformation.class);

		assertThat(reqMapper1, sameBeanAs(vfModuleSDNCrequest).ignoring("sdncRequestHeader.svcRequestId")
				.ignoring("requestInformation.requestId"));
	}

	@Test
	public void unassignGenericResourceApiVfModuleInformationTest() throws Exception {

		// prepare and set service instance
		ServiceInstance serviceInstance = new ServiceInstance();
		serviceInstance.setServiceInstanceId("serviceInstanceId");

		// prepare and set vnf instance

		GenericVnf vnf = new GenericVnf();
		vnf.setVnfId("testVnfId");
		vnf.setVnfType("testVnfType");

		// prepare and set vf module instance

		VfModule vfModule = new VfModule();
		vfModule.setVfModuleId("testVfModuleId");
		vfModule.setVfModuleName("testVfModuleName");

		VfModuleTopologyOperationRequestMapper mapper = new VfModuleTopologyOperationRequestMapper();
		GenericResourceApiVfModuleOperationInformation vfModuleSDNCrequest = mapper.reqMapper(
				SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION, SDNCSvcAction.UNASSIGN, vfModule, null, vnf, serviceInstance, null,
				null, null, null);

		String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleOperationInformationUnassign.json")));

		ObjectMapper omapper = new ObjectMapper();
		GenericResourceApiVfModuleOperationInformation reqMapper1 = omapper.readValue(
				jsonToCompare,
				GenericResourceApiVfModuleOperationInformation.class);

		assertThat(reqMapper1, sameBeanAs(vfModuleSDNCrequest).ignoring("sdncRequestHeader.svcRequestId")
				.ignoring("requestInformation.requestId"));
	}

	@Test
	public void reqMapperTest() throws Exception {

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
		customer.setServiceSubscription(new ServiceSubscription());
		// set Customer on service instance
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		//
		RequestContext requestContext = new RequestContext();
		RequestParameters requestParameters = new RequestParameters();
		HashMap<String,Object> userParams1 = new HashMap<String,Object>();
		userParams1.put("key1", "value1");
		List<Map<String,Object>> userParams = new ArrayList<Map<String,Object>>();
		userParams.add(userParams1);
		
		requestParameters.setUserParams(userParams);
		requestContext.setRequestParameters(requestParameters);
		requestContext.setProductFamilyId("productFamilyId");

		GenericVnf vnf = new GenericVnf();
		vnf.setVnfId("testVnfId");
		vnf.setVnfType("testVnfType");
		ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
		modelInfoGenericVnf.setModelInvariantUuid("vnfModelInvariantUuid");
		modelInfoGenericVnf.setModelName("vnfModelName");
		modelInfoGenericVnf.setModelVersion("vnfModelVersion");
		modelInfoGenericVnf.setModelUuid("vnfModelUuid");
		modelInfoGenericVnf.setModelCustomizationUuid("vnfModelCustomizationUuid");
		vnf.setModelInfoGenericVnf(modelInfoGenericVnf);

		VfModule vfModule = new VfModule();
		vfModule.setVfModuleId("testVfModuleId");
		vfModule.setVfModuleName("testVfModuleName");
		ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
		modelInfoVfModule.setModelInvariantUUID("vfModuleModelInvariantUuid");
		modelInfoVfModule.setModelName("vfModuleModelName");
		modelInfoVfModule.setModelVersion("vfModuleModelVersion");
		modelInfoVfModule.setModelUUID("vfModuleModelUuid");
		modelInfoVfModule.setModelCustomizationUUID("vfModuleModelCustomizationUuid");
		vfModule.setModelInfoVfModule(modelInfoVfModule);

		CloudRegion cloudRegion = new CloudRegion();

		VfModuleTopologyOperationRequestMapper mapper = new VfModuleTopologyOperationRequestMapper();
		GenericResourceApiVfModuleOperationInformation vfModuleSDNCrequest = mapper.reqMapper(
				SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION, SDNCSvcAction.ASSIGN, vfModule, null, vnf, serviceInstance, customer,
				cloudRegion, requestContext, null);

		assertNull(vfModuleSDNCrequest.getServiceInformation().getOnapModelInformation().getModelCustomizationUuid());
		assertEquals("vnfModelCustomizationUuid", vfModuleSDNCrequest.getVnfInformation().getOnapModelInformation().getModelCustomizationUuid());
		assertEquals("vfModuleModelCustomizationUuid", vfModuleSDNCrequest.getVfModuleInformation().getOnapModelInformation().getModelCustomizationUuid());	
	}
	
	@Test
	public void reqMapperNoModelInfoTest() throws Exception {

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
		customer.setServiceSubscription(new ServiceSubscription());
		// set Customer on service instance
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		//
		RequestContext requestContext = new RequestContext();
		HashMap<String, String> userParams = new HashMap<String, String>();
		userParams.put("key1", "value1");
		requestContext.setUserParams(userParams);
		requestContext.setProductFamilyId("productFamilyId");

		GenericVnf vnf = new GenericVnf();
		vnf.setVnfId("testVnfId");
		vnf.setVnfType("testVnfType");
		ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
		modelInfoGenericVnf.setModelInvariantUuid("vnfModelInvariantUuid");
		modelInfoGenericVnf.setModelName("vnfModelName");
		modelInfoGenericVnf.setModelVersion("vnfModelVersion");
		modelInfoGenericVnf.setModelUuid("vnfModelUuid");
		modelInfoGenericVnf.setModelCustomizationUuid("vnfModelCustomizationUuid");
		vnf.setModelInfoGenericVnf(modelInfoGenericVnf);

		VfModule vfModule = new VfModule();
		vfModule.setVfModuleId("testVfModuleId");
		vfModule.setVfModuleName("testVfModuleName");		
		vfModule.setModelInfoVfModule(null);

		CloudRegion cloudRegion = new CloudRegion();
		
		VfModuleTopologyOperationRequestMapper mapper = new VfModuleTopologyOperationRequestMapper();
		expectedException.expect(MapperException.class);
		expectedException.expectMessage(ERRORMESSAGE);
		
		mapper.reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION, SDNCSvcAction.ASSIGN, vfModule, null, vnf, serviceInstance, customer,
			cloudRegion, requestContext, null);		
	}

}
