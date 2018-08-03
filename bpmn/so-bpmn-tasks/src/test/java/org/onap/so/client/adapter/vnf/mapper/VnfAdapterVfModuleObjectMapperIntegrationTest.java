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

package org.onap.so.client.adapter.vnf.mapper;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.so.adapters.vnfrest.CreateVfModuleRequest;
import org.onap.so.adapters.vnfrest.DeleteVfModuleRequest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;

import com.fasterxml.jackson.databind.ObjectMapper;

public class VnfAdapterVfModuleObjectMapperIntegrationTest {

	private final static String JSON_FILE_LOCATION = "src/test/resources/__files/BuildingBlocks/";

	@Test
	@Ignore
	public void createVfModuleRequestMapperTest() throws Exception {

		// prepare and set service instance
		ServiceInstance serviceInstance = new ServiceInstance();
		serviceInstance.setServiceInstanceId("serviceInstanceId");
		ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
		modelInfoServiceInstance.setModelInvariantUuid("serviceModelInvariantUuid");
		modelInfoServiceInstance.setModelName("serviceModelName");
		modelInfoServiceInstance.setModelUuid("serviceModelUuid");
		modelInfoServiceInstance.setModelVersion("serviceModelVersion");
		modelInfoServiceInstance.setEnvironmentContext("environmentContext");
		modelInfoServiceInstance.setWorkloadContext("workloadContext");
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
		requestContext.setMsoRequestId("requestId");
		requestContext.setUserParams(userParams);
		requestContext.setProductFamilyId("productFamilyId");

		GenericVnf vnf = new GenericVnf();
		vnf.setVnfId("vnfId");
		vnf.setVnfType("vnfType");
		vnf.setVnfName("vnfName");
		ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
		modelInfoGenericVnf.setModelInvariantUuid("vnfModelInvariantUuid");
		modelInfoGenericVnf.setModelName("vnfModelName");
		modelInfoGenericVnf.setModelVersion("vnfModelVersion");
		modelInfoGenericVnf.setModelUuid("vnfModelUuid");
		modelInfoGenericVnf.setModelCustomizationUuid("vnfModelCustomizationUuid");
		vnf.setModelInfoGenericVnf(modelInfoGenericVnf);

		Integer vfModuleIndex = 1;
		VfModule vfModule = new VfModule();
		vfModule.setVfModuleId("vfModuleId");
		vfModule.setVfModuleName("vfModuleName");
		vfModule.setModuleIndex(vfModuleIndex);
		ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
		modelInfoVfModule.setModelInvariantUUID("vfModuleModelInvariantUuid");
		modelInfoVfModule.setModelName("vfModuleModelName");
		modelInfoVfModule.setModelVersion("vfModuleModelVersion");
		modelInfoVfModule.setModelUUID("vfModuleModelUuid");
		modelInfoVfModule.setModelCustomizationUUID("vfModuleModelCustomizationUuid");
		vfModule.setModelInfoVfModule(modelInfoVfModule);

		CloudRegion cloudRegion = new CloudRegion();
		cloudRegion.setLcpCloudRegionId("cloudRegionId");
		cloudRegion.setTenantId("tenantId");

		OrchestrationContext orchestrationContext = new OrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(false);

		String sdncVnfQueryResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVnfTopology.json")));
		String sdncVfModuleQueryResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVfModuleTopology.json")));

		VnfAdapterVfModuleObjectMapper mapper = new VnfAdapterVfModuleObjectMapper();
		mapper.vnfAdapterObjectMapperUtils = new VnfAdapterObjectMapperUtils();

		CreateVfModuleRequest vfModuleVNFAdapterRequest = mapper.createVfModuleRequestMapper(
				requestContext, cloudRegion, orchestrationContext, serviceInstance,
				vnf, vfModule, sdncVnfQueryResponse, sdncVfModuleQueryResponse);


		String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "vnfAdapterCreateVfModuleRequest.json")));

		ObjectMapper omapper = new ObjectMapper();
		CreateVfModuleRequest reqMapper1 = omapper.readValue(
				jsonToCompare,
				CreateVfModuleRequest.class);

		assertThat(vfModuleVNFAdapterRequest, sameBeanAs(reqMapper1).ignoring("messageId").ignoring("notificationUrl"));
	}

	@Test
	@Ignore
	public void createVfModuleRequestMapperWithCloudResourcesTest() throws Exception {

		// prepare and set service instance
		ServiceInstance serviceInstance = new ServiceInstance();
		serviceInstance.setServiceInstanceId("serviceInstanceId");
		ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
		modelInfoServiceInstance.setModelInvariantUuid("serviceModelInvariantUuid");
		modelInfoServiceInstance.setModelName("serviceModelName");
		modelInfoServiceInstance.setModelUuid("serviceModelUuid");
		modelInfoServiceInstance.setModelVersion("serviceModelVersion");
		modelInfoServiceInstance.setEnvironmentContext("environmentContext");
		modelInfoServiceInstance.setWorkloadContext("workloadContext");
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
		requestContext.setMsoRequestId("requestId");
		requestContext.setUserParams(userParams);
		requestContext.setProductFamilyId("productFamilyId");

		GenericVnf vnf = new GenericVnf();
		vnf.setVnfId("vnfId");
		vnf.setVnfType("vnfType");
		vnf.setVnfName("vnfName");
		ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
		modelInfoGenericVnf.setModelInvariantUuid("vnfModelInvariantUuid");
		modelInfoGenericVnf.setModelName("vnfModelName");
		modelInfoGenericVnf.setModelVersion("vnfModelVersion");
		modelInfoGenericVnf.setModelUuid("vnfModelUuid");
		modelInfoGenericVnf.setModelCustomizationUuid("vnfModelCustomizationUuid");
		vnf.setModelInfoGenericVnf(modelInfoGenericVnf);

		Integer vfModuleIndex = 1;
		VfModule vfModule = new VfModule();
		vfModule.setVfModuleId("vfModuleId");
		vfModule.setVfModuleName("vfModuleName");
		vfModule.setModuleIndex(vfModuleIndex);
		ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
		modelInfoVfModule.setModelInvariantUUID("vfModuleModelInvariantUuid");
		modelInfoVfModule.setModelName("vfModuleModelName");
		modelInfoVfModule.setModelVersion("vfModuleModelVersion");
		modelInfoVfModule.setModelUUID("vfModuleModelUuid");
		modelInfoVfModule.setModelCustomizationUUID("vfModuleModelCustomizationUuid");
		vfModule.setModelInfoVfModule(modelInfoVfModule);

		CloudRegion cloudRegion = new CloudRegion();
		cloudRegion.setLcpCloudRegionId("cloudRegionId");
		cloudRegion.setTenantId("tenantId");

		OrchestrationContext orchestrationContext = new OrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(false);

		String sdncVnfQueryResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVnfTopologyWithCloudResources.json")));
		String sdncVfModuleQueryResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVfModuleTopologyWithCloudResources.json")));

		VnfAdapterVfModuleObjectMapper mapper = new VnfAdapterVfModuleObjectMapper();
		mapper.vnfAdapterObjectMapperUtils = new VnfAdapterObjectMapperUtils();

		CreateVfModuleRequest vfModuleVNFAdapterRequest = mapper.createVfModuleRequestMapper(
				requestContext, cloudRegion, orchestrationContext, serviceInstance,
				vnf, vfModule, sdncVnfQueryResponse, sdncVfModuleQueryResponse);


		String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "vnfAdapterCreateVfModuleRequestWithCloudResources.json")));

		ObjectMapper omapper = new ObjectMapper();
		CreateVfModuleRequest reqMapper1 = omapper.readValue(
				jsonToCompare,
				CreateVfModuleRequest.class);

		assertThat(vfModuleVNFAdapterRequest, sameBeanAs(reqMapper1).ignoring("messageId").ignoring("notificationUrl"));
	}

	@Test
	@Ignore
	public void createVfModuleRequestMapperDhcpDisabledTest() throws Exception {
		// prepare and set service instance
		ServiceInstance serviceInstance = new ServiceInstance();
		serviceInstance.setServiceInstanceId("serviceInstanceId");
		ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
		modelInfoServiceInstance.setModelInvariantUuid("serviceModelInvariantUuid");
		modelInfoServiceInstance.setModelName("serviceModelName");
		modelInfoServiceInstance.setModelUuid("serviceModelUuid");
		modelInfoServiceInstance.setModelVersion("serviceModelVersion");
		modelInfoServiceInstance.setEnvironmentContext("environmentContext");
		modelInfoServiceInstance.setWorkloadContext("workloadContext");
		serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);
		// prepare Customer object
		Customer customer = new Customer();
		customer.setGlobalCustomerId("globalCustomerId");
		customer.setServiceSubscription(new ServiceSubscription());
		// set Customer on service instance

		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		

		RequestContext requestContext = new RequestContext();
		HashMap<String, String> userParams = new HashMap<String, String>();
		userParams.put("key1", "value1");
		requestContext.setMsoRequestId("requestId");
		requestContext.setUserParams(userParams);
		requestContext.setProductFamilyId("productFamilyId");

		GenericVnf vnf = new GenericVnf();
		vnf.setVnfId("vnfId");
		vnf.setVnfType("vnfType");
		vnf.setVnfName("vnfName");
		ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
		modelInfoGenericVnf.setModelInvariantUuid("vnfModelInvariantUuid");
		modelInfoGenericVnf.setModelName("vnfModelName");
		modelInfoGenericVnf.setModelVersion("vnfModelVersion");
		modelInfoGenericVnf.setModelUuid("vnfModelUuid");
		modelInfoGenericVnf.setModelCustomizationUuid("vnfModelCustomizationUuid");
		vnf.setModelInfoGenericVnf(modelInfoGenericVnf);

		Integer vfModuleIndex = 1;
		VfModule vfModule = new VfModule();
		vfModule.setVfModuleId("vfModuleId");
		vfModule.setVfModuleName("vfModuleName");
		vfModule.setModuleIndex(vfModuleIndex);
		ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
		modelInfoVfModule.setModelInvariantUUID("vfModuleModelInvariantUuid");
		modelInfoVfModule.setModelName("vfModuleModelName");
		modelInfoVfModule.setModelVersion("vfModuleModelVersion");
		modelInfoVfModule.setModelUUID("vfModuleModelUuid");
		modelInfoVfModule.setModelCustomizationUUID("vfModuleModelCustomizationUuid");
		vfModule.setModelInfoVfModule(modelInfoVfModule);

		CloudRegion cloudRegion = new CloudRegion();
		cloudRegion.setLcpCloudRegionId("cloudRegionId");
		cloudRegion.setTenantId("tenantId");

		OrchestrationContext orchestrationContext = new OrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(false);

		String sdncVnfQueryResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVnfTopologySubnetDhcpDisabled.json")));
		String sdncVfModuleQueryResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVfModuleTopology.json")));

		VnfAdapterVfModuleObjectMapper mapper = new VnfAdapterVfModuleObjectMapper();
		mapper.vnfAdapterObjectMapperUtils = new VnfAdapterObjectMapperUtils();

		CreateVfModuleRequest vfModuleVNFAdapterRequest = mapper.createVfModuleRequestMapper(
				requestContext, cloudRegion, orchestrationContext, serviceInstance,
				vnf, vfModule, sdncVnfQueryResponse, sdncVfModuleQueryResponse);


		String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "vnfAdapterCreateVfModuleRequestDhcpDisabled.json")));

		ObjectMapper omapper = new ObjectMapper();
		CreateVfModuleRequest reqMapper1 = omapper.readValue(
				jsonToCompare,
				CreateVfModuleRequest.class);

		assertThat(vfModuleVNFAdapterRequest, sameBeanAs(reqMapper1).ignoring("messageId").ignoring("notificationUrl"));
	}

	@Test
	@Ignore
	public void createVfModuleRequestMapperMultipleDhcpTest() throws Exception {
		// prepare and set service instance
		ServiceInstance serviceInstance = new ServiceInstance();
		serviceInstance.setServiceInstanceId("serviceInstanceId");
		ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
		modelInfoServiceInstance.setModelInvariantUuid("serviceModelInvariantUuid");
		modelInfoServiceInstance.setModelName("serviceModelName");
		modelInfoServiceInstance.setModelUuid("serviceModelUuid");
		modelInfoServiceInstance.setModelVersion("serviceModelVersion");
		modelInfoServiceInstance.setEnvironmentContext("environmentContext");
		modelInfoServiceInstance.setWorkloadContext("workloadContext");
		serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);
		// prepare Customer object
		Customer customer = new Customer();
		customer.setGlobalCustomerId("globalCustomerId");
		customer.setServiceSubscription(new ServiceSubscription());
		// set Customer on service instance

		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);

		RequestContext requestContext = new RequestContext();
		HashMap<String, String> userParams = new HashMap<String, String>();
		userParams.put("key1", "value1");
		requestContext.setMsoRequestId("requestId");
		requestContext.setUserParams(userParams);
		requestContext.setProductFamilyId("productFamilyId");

		GenericVnf vnf = new GenericVnf();
		vnf.setVnfId("vnfId");
		vnf.setVnfType("vnfType");
		vnf.setVnfName("vnfName");
		ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
		modelInfoGenericVnf.setModelInvariantUuid("vnfModelInvariantUuid");
		modelInfoGenericVnf.setModelName("vnfModelName");
		modelInfoGenericVnf.setModelVersion("vnfModelVersion");
		modelInfoGenericVnf.setModelUuid("vnfModelUuid");
		modelInfoGenericVnf.setModelCustomizationUuid("vnfModelCustomizationUuid");
		vnf.setModelInfoGenericVnf(modelInfoGenericVnf);

		Integer vfModuleIndex = 1;
		VfModule vfModule = new VfModule();
		vfModule.setVfModuleId("vfModuleId");
		vfModule.setVfModuleName("vfModuleName");
		vfModule.setModuleIndex(vfModuleIndex);
		ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
		modelInfoVfModule.setModelInvariantUUID("vfModuleModelInvariantUuid");
		modelInfoVfModule.setModelName("vfModuleModelName");
		modelInfoVfModule.setModelVersion("vfModuleModelVersion");
		modelInfoVfModule.setModelUUID("vfModuleModelUuid");
		modelInfoVfModule.setModelCustomizationUUID("vfModuleModelCustomizationUuid");
		vfModule.setModelInfoVfModule(modelInfoVfModule);

		CloudRegion cloudRegion = new CloudRegion();
		cloudRegion.setLcpCloudRegionId("cloudRegionId");
		cloudRegion.setTenantId("tenantId");

		OrchestrationContext orchestrationContext = new OrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(false);

		String sdncVnfQueryResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVnfTopologySubnetMultipleDhcp.json")));
		String sdncVfModuleQueryResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVfModuleTopology.json")));

		VnfAdapterVfModuleObjectMapper mapper = new VnfAdapterVfModuleObjectMapper();
		mapper.vnfAdapterObjectMapperUtils = new VnfAdapterObjectMapperUtils();

		CreateVfModuleRequest vfModuleVNFAdapterRequest = mapper.createVfModuleRequestMapper(
				requestContext, cloudRegion, orchestrationContext, serviceInstance,
				vnf, vfModule, sdncVnfQueryResponse, sdncVfModuleQueryResponse);


		String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "vnfAdapterCreateVfModuleRequestMultipleDhcp.json")));

		ObjectMapper omapper = new ObjectMapper();
		CreateVfModuleRequest reqMapper1 = omapper.readValue(
				jsonToCompare,
				CreateVfModuleRequest.class);

		assertThat(vfModuleVNFAdapterRequest, sameBeanAs(reqMapper1).ignoring("messageId").ignoring("notificationUrl"));
	}

	@Test
	public void DeleteVfModuleRequestMapperTest() throws Exception {
		ServiceInstance serviceInstance = new ServiceInstance();
		serviceInstance.setServiceInstanceId("serviceInstanceId");
		ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
		modelInfoServiceInstance.setModelInvariantUuid("serviceModelInvariantUuid");
		modelInfoServiceInstance.setModelName("serviceModelName");
		modelInfoServiceInstance.setModelUuid("serviceModelUuid");
		modelInfoServiceInstance.setModelVersion("serviceModelVersion");
		modelInfoServiceInstance.setEnvironmentContext("environmentContext");
		modelInfoServiceInstance.setWorkloadContext("workloadContext");
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
		requestContext.setMsoRequestId("requestId");
		requestContext.setUserParams(userParams);
		requestContext.setProductFamilyId("productFamilyId");

		GenericVnf vnf = new GenericVnf();
		vnf.setVnfId("vnfId");
		vnf.setVnfType("vnfType");
		vnf.setVnfName("vnfName");
		ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
		modelInfoGenericVnf.setModelInvariantUuid("vnfModelInvariantUuid");
		modelInfoGenericVnf.setModelName("vnfModelName");
		modelInfoGenericVnf.setModelVersion("vnfModelVersion");
		modelInfoGenericVnf.setModelUuid("vnfModelUuid");
		modelInfoGenericVnf.setModelCustomizationUuid("vnfModelCustomizationUuid");
		vnf.setModelInfoGenericVnf(modelInfoGenericVnf);

		VfModule vfModule = new VfModule();
		vfModule.setVfModuleId("vfModuleId");
		vfModule.setVfModuleName("vfModuleName");
		ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
		modelInfoVfModule.setModelInvariantUUID("vfModuleModelInvariantUuid");
		modelInfoVfModule.setModelName("vfModuleModelName");
		modelInfoVfModule.setModelVersion("vfModuleModelVersion");
		modelInfoVfModule.setModelUUID("vfModuleModelUuid");
		modelInfoVfModule.setModelCustomizationUUID("vfModuleModelCustomizationUuid");
		vfModule.setModelInfoVfModule(modelInfoVfModule);

		CloudRegion cloudRegion = new CloudRegion();
		cloudRegion.setLcpCloudRegionId("cloudRegionId");
		cloudRegion.setTenantId("tenantId");

		OrchestrationContext orchestrationContext = new OrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(false);

		VnfAdapterVfModuleObjectMapper mapper = new VnfAdapterVfModuleObjectMapper();
		mapper.vnfAdapterObjectMapperUtils = new VnfAdapterObjectMapperUtils();

		DeleteVfModuleRequest vfModuleVNFAdapterRequest = mapper.deleteVfModuleRequestMapper(
				requestContext, cloudRegion, serviceInstance,
				vnf, vfModule);


		String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "vnfAdapterDeleteVfModuleRequest.json")));

		ObjectMapper omapper = new ObjectMapper();
		DeleteVfModuleRequest reqMapper1 = omapper.readValue(
				jsonToCompare,
				DeleteVfModuleRequest.class);

		assertThat(vfModuleVNFAdapterRequest, sameBeanAs(reqMapper1).ignoring("messageId").ignoring("notificationUrl"));
	}
}
