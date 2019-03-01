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
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVmNetworkData;
import org.onap.so.adapters.vnfrest.CreateVfModuleRequest;
import org.onap.so.adapters.vnfrest.DeleteVfModuleRequest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;

import com.fasterxml.jackson.databind.ObjectMapper;

public class VnfAdapterVfModuleObjectMapperPayloadTest {

	private final static String JSON_FILE_LOCATION = "src/test/resources/__files/BuildingBlocks/VnfAndVfModuleMapper/";

	private VnfAdapterVfModuleObjectMapper vfModuleObjectMapper = new VnfAdapterVfModuleObjectMapper();
	private ObjectMapper omapper = new ObjectMapper();
	@Before
	public void setUp() {
		vfModuleObjectMapper.vnfAdapterObjectMapperUtils = new VnfAdapterObjectMapperUtils();
		vfModuleObjectMapper.init();
		
	}
	@Test
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

		RequestContext requestContext = new RequestContext();
		Map<String, Object> userParams = new HashMap<>();
		userParams.put("key1", "value2");
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
		HashMap<String, String> cloudParams = new HashMap<String, String>();
		cloudParams.put("key3", "value3");
		vfModule.setCloudParams(cloudParams);

		CloudRegion cloudRegion = new CloudRegion();
		cloudRegion.setLcpCloudRegionId("cloudRegionId");
		cloudRegion.setTenantId("tenantId");

		OrchestrationContext orchestrationContext = new OrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(false);

		String sdncVnfQueryResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVnfTopology.json")));
		String sdncVfModuleQueryResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVfModuleTopology.json")));

		CreateVfModuleRequest vfModuleVNFAdapterRequest = vfModuleObjectMapper.createVfModuleRequestMapper(
				requestContext, cloudRegion, orchestrationContext, serviceInstance,
				vnf, vfModule, null, sdncVnfQueryResponse, sdncVfModuleQueryResponse);


		String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "vnfAdapterCreateVfModuleRequest.json")));

				CreateVfModuleRequest reqMapper1 = omapper.readValue(
				jsonToCompare,
				CreateVfModuleRequest.class);

		assertThat(vfModuleVNFAdapterRequest, sameBeanAs(reqMapper1).ignoring("messageId").ignoring("notificationUrl"));
	}
	
	@Test
	public void createVfModuleWithFalseRollbackRequestMapperTest() throws Exception {

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

		RequestContext requestContext = new RequestContext();
		Map<String, Object> userParams = new HashMap<>();
		userParams.put("key1", "value2");
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
		HashMap<String, String> cloudParams = new HashMap<String, String>();
		cloudParams.put("key3", "value3");
		vfModule.setCloudParams(cloudParams);

		CloudRegion cloudRegion = new CloudRegion();
		cloudRegion.setLcpCloudRegionId("cloudRegionId");
		cloudRegion.setTenantId("tenantId");

		OrchestrationContext orchestrationContext = new OrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(true);

		String sdncVnfQueryResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVnfTopology.json")));
		String sdncVfModuleQueryResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVfModuleTopology.json")));

		CreateVfModuleRequest vfModuleVNFAdapterRequest = vfModuleObjectMapper.createVfModuleRequestMapper(
				requestContext, cloudRegion, orchestrationContext, serviceInstance,
				vnf, vfModule, null, sdncVnfQueryResponse, sdncVfModuleQueryResponse);


		String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "vnfAdapterCreateVfModuleRequestTrueBackout.json")));

				CreateVfModuleRequest reqMapper1 = omapper.readValue(
				jsonToCompare,
				CreateVfModuleRequest.class);

		assertThat(vfModuleVNFAdapterRequest, sameBeanAs(reqMapper1).ignoring("messageId").ignoring("notificationUrl"));
	}
	
	@Test
	public void createVfModuleRequestWithNoEnvironmentAndWorkloadContextMapperTest() throws Exception {

		// prepare and set service instance
		ServiceInstance serviceInstance = new ServiceInstance();
		serviceInstance.setServiceInstanceId("serviceInstanceId");
		ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
		modelInfoServiceInstance.setModelInvariantUuid("serviceModelInvariantUuid");
		modelInfoServiceInstance.setModelName("serviceModelName");
		modelInfoServiceInstance.setModelUuid("serviceModelUuid");
		modelInfoServiceInstance.setModelVersion("serviceModelVersion");
		modelInfoServiceInstance.setEnvironmentContext(null);		
		serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);
		
		RequestContext requestContext = new RequestContext();
		Map<String, Object> userParams = new HashMap<>();
		userParams.put("key1", "value2");
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

		CreateVfModuleRequest vfModuleVNFAdapterRequest = vfModuleObjectMapper.createVfModuleRequestMapper(
				requestContext, cloudRegion, orchestrationContext, serviceInstance,
				vnf, vfModule, null, sdncVnfQueryResponse, sdncVfModuleQueryResponse);


		String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "vnfAdapterCreateVfModuleWithNoEnvironmentAndWorkloadContextRequest.json")));

				CreateVfModuleRequest reqMapper1 = omapper.readValue(
				jsonToCompare,
				CreateVfModuleRequest.class);

		assertThat(vfModuleVNFAdapterRequest, sameBeanAs(reqMapper1).ignoring("messageId").ignoring("notificationUrl"));
	}
	
	@Test
	public void createVfModuleAddonRequestMapperTest() throws Exception {

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

		//
		RequestContext requestContext = new RequestContext();
		Map<String, Object> userParams = new HashMap<>();
		userParams.put("key1", "value2");
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
		
		VfModule baseVfModule = new VfModule();
		baseVfModule.setVfModuleId("baseVfModuleId");
		baseVfModule.setHeatStackId("baseVfModuleStackId");
		ModelInfoVfModule baseModelInfoVfModule = new ModelInfoVfModule();
		baseModelInfoVfModule.setIsBaseBoolean(true);
		baseVfModule.setModelInfoVfModule(baseModelInfoVfModule);	
		vnf.getVfModules().add(baseVfModule);		

		CloudRegion cloudRegion = new CloudRegion();
		cloudRegion.setLcpCloudRegionId("cloudRegionId");
		cloudRegion.setTenantId("tenantId");

		OrchestrationContext orchestrationContext = new OrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(false);

		String sdncVnfQueryResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVnfTopology.json")));
		String sdncVfModuleQueryResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVfModuleTopology.json")));

		CreateVfModuleRequest vfModuleVNFAdapterRequest = vfModuleObjectMapper.createVfModuleRequestMapper(
				requestContext, cloudRegion, orchestrationContext, serviceInstance,
				vnf, vfModule, null, sdncVnfQueryResponse, sdncVfModuleQueryResponse);


		String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "vnfAdapterCreateVfModuleAddonRequest.json")));

				CreateVfModuleRequest reqMapper1 = omapper.readValue(
				jsonToCompare,
				CreateVfModuleRequest.class);

		assertThat(vfModuleVNFAdapterRequest, sameBeanAs(reqMapper1).ignoring("messageId").ignoring("notificationUrl"));
	}
	
	@Test
	public void createVfModuleWithVolumeGroupRequestMapperTest() throws Exception {

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

		//
		RequestContext requestContext = new RequestContext();
		Map<String, Object> userParams = new HashMap<>();
		userParams.put("key1", "value2");
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
		
		VolumeGroup volumeGroup = new VolumeGroup();
		volumeGroup.setVolumeGroupId("volumeGroupId");
		volumeGroup.setHeatStackId("volumeGroupStackId");		

		CloudRegion cloudRegion = new CloudRegion();
		cloudRegion.setLcpCloudRegionId("cloudRegionId");
		cloudRegion.setTenantId("tenantId");

		OrchestrationContext orchestrationContext = new OrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(false);

		String sdncVnfQueryResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVnfTopology.json")));
		String sdncVfModuleQueryResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVfModuleTopology.json")));

		CreateVfModuleRequest vfModuleVNFAdapterRequest = vfModuleObjectMapper.createVfModuleRequestMapper(
				requestContext, cloudRegion, orchestrationContext, serviceInstance,
				vnf, vfModule, volumeGroup, sdncVnfQueryResponse, sdncVfModuleQueryResponse);


		String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "vnfAdapterCreateVfModuleWithVolumeGroupRequest.json")));

				CreateVfModuleRequest reqMapper1 = omapper.readValue(
				jsonToCompare,
				CreateVfModuleRequest.class);

		assertThat(vfModuleVNFAdapterRequest, sameBeanAs(reqMapper1).ignoring("messageId").ignoring("notificationUrl"));
	}
	
	@Test
	public void createVfModuleWithSingleAvailabilityZoneRequestMapperTest() throws Exception {

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

		RequestContext requestContext = new RequestContext();
		Map<String, Object> userParams = new HashMap<>();
		userParams.put("key1", "value2");
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

		String sdncVnfQueryResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVnfTopologyWithSingletonArray.json")));
		String sdncVfModuleQueryResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "genericResourceApiVfModuleSdncVfModuleTopology.json")));

		CreateVfModuleRequest vfModuleVNFAdapterRequest = vfModuleObjectMapper.createVfModuleRequestMapper(
				requestContext, cloudRegion, orchestrationContext, serviceInstance,
				vnf, vfModule, null, sdncVnfQueryResponse, sdncVfModuleQueryResponse);


		String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "vnfAdapterCreateVfModuleRequestWithSingleAvailabilityZone.json")));

				CreateVfModuleRequest reqMapper1 = omapper.readValue(
				jsonToCompare,
				CreateVfModuleRequest.class);

		assertThat(vfModuleVNFAdapterRequest, sameBeanAs(reqMapper1).ignoring("messageId").ignoring("notificationUrl"));
	}

	@Test
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

		RequestContext requestContext = new RequestContext();
		Map<String, Object> userParams = new HashMap<>();
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

		CreateVfModuleRequest vfModuleVNFAdapterRequest = vfModuleObjectMapper.createVfModuleRequestMapper(
				requestContext, cloudRegion, orchestrationContext, serviceInstance,
				vnf, vfModule, null, sdncVnfQueryResponse, sdncVfModuleQueryResponse);


		String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "vnfAdapterCreateVfModuleRequestWithCloudResources.json")));

				CreateVfModuleRequest reqMapper1 = omapper.readValue(
				jsonToCompare,
				CreateVfModuleRequest.class);

		assertThat(vfModuleVNFAdapterRequest, sameBeanAs(reqMapper1).ignoring("messageId").ignoring("notificationUrl"));
	}

	@Test
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

		RequestContext requestContext = new RequestContext();
		Map<String, Object> userParams = new HashMap<>();
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

		CreateVfModuleRequest vfModuleVNFAdapterRequest = vfModuleObjectMapper.createVfModuleRequestMapper(
				requestContext, cloudRegion, orchestrationContext, serviceInstance,
				vnf, vfModule, null, sdncVnfQueryResponse, sdncVfModuleQueryResponse);


		String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "vnfAdapterCreateVfModuleRequestDhcpDisabled.json")));

				CreateVfModuleRequest reqMapper1 = omapper.readValue(
				jsonToCompare,
				CreateVfModuleRequest.class);

		assertThat(vfModuleVNFAdapterRequest, sameBeanAs(reqMapper1).ignoring("messageId").ignoring("notificationUrl"));
	}

	@Test
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

		RequestContext requestContext = new RequestContext();
		Map<String, Object> userParams = new HashMap<>();
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

		CreateVfModuleRequest vfModuleVNFAdapterRequest = vfModuleObjectMapper.createVfModuleRequestMapper(
				requestContext, cloudRegion, orchestrationContext, serviceInstance,
				vnf, vfModule, null, sdncVnfQueryResponse, sdncVfModuleQueryResponse);


		String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "vnfAdapterCreateVfModuleRequestMultipleDhcp.json")));

				CreateVfModuleRequest reqMapper1 = omapper.readValue(
				jsonToCompare,
				CreateVfModuleRequest.class);

		assertThat(vfModuleVNFAdapterRequest, sameBeanAs(reqMapper1).ignoring("messageId").ignoring("notificationUrl"));
	}
	
	@Test
	public void createVfModuleRequestMapperWithNullUserParamsTest() throws Exception {

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

		RequestContext requestContext = new RequestContext();		
		requestContext.setMsoRequestId("requestId");		
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

		CreateVfModuleRequest vfModuleVNFAdapterRequest = vfModuleObjectMapper.createVfModuleRequestMapper(
				requestContext, cloudRegion, orchestrationContext, serviceInstance,
				vnf, vfModule, null, sdncVnfQueryResponse, sdncVfModuleQueryResponse);


		String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "vnfAdapterCreateVfModuleRequestNoUserParams.json")));

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
		
		RequestContext requestContext = new RequestContext();
		Map<String, Object> userParams = new HashMap<>();
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

		DeleteVfModuleRequest vfModuleVNFAdapterRequest = vfModuleObjectMapper.deleteVfModuleRequestMapper(
				requestContext, cloudRegion, serviceInstance,
				vnf, vfModule);


		String jsonToCompare = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "vnfAdapterDeleteVfModuleRequest.json")));

				DeleteVfModuleRequest reqMapper1 = omapper.readValue(
				jsonToCompare,
				DeleteVfModuleRequest.class);

		assertThat(vfModuleVNFAdapterRequest, sameBeanAs(reqMapper1).ignoring("messageId").ignoring("notificationUrl").ignoring("vfModuleStackId"));
	}
	
	@Test
	public void networkCloudParamsTest() throws IOException {
		
		String json = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "grApiVmNetworkSubSectionWith5GParams.json")));
		GenericResourceApiVmNetworkData network = omapper.readValue(json, GenericResourceApiVmNetworkData.class);
		Map<String, Object> paramsMap = new HashMap<>();
		vfModuleObjectMapper.buildVlanInformation(paramsMap, network, "testKey", "testType");
		
		assertEquals("1,3", paramsMap.get("testKey_testType_private_vlans"));
		assertEquals("2,3", paramsMap.get("testKey_testType_public_vlans"));
		assertEquals("1,2,3", paramsMap.get("testKey_testType_guest_vlans"));
		assertEquals("my-segemntation-id", paramsMap.get("testKey_testType_vlan_filter"));
	}
}
