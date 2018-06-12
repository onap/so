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

package org.openecomp.mso.bpmn.servicedecomposition.tasks;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Collection;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.L3Network;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.LineOfBusiness;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.OwningEntity;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Platform;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Project;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.RouteTableReference;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.openecomp.mso.bpmn.servicedecomposition.entities.BuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ResourceKey;
import org.openecomp.mso.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoCollection;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoNetwork;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.OrchestrationContext;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.client.aai.AAICommonObjectMapperProvider;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;
import org.openecomp.mso.client.aai.entities.uri.Depth;
import org.openecomp.mso.db.catalog.beans.CollectionResource;
import org.openecomp.mso.db.catalog.beans.CollectionResourceCustomization;
import org.openecomp.mso.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.openecomp.mso.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;
import org.openecomp.mso.serviceinstancebeans.ModelInfo;
import org.openecomp.mso.serviceinstancebeans.ModelType;
import org.openecomp.mso.serviceinstancebeans.RelatedInstance;
import org.openecomp.mso.serviceinstancebeans.RelatedInstanceList;
import org.openecomp.mso.serviceinstancebeans.RequestDetails;
import org.openecomp.mso.serviceinstancebeans.RequestInfo;
import org.openecomp.mso.serviceinstancebeans.RequestParameters;
import org.openecomp.mso.serviceinstancebeans.SubscriberInfo;
import org.openecomp.mso.serviceinstancebeans.VfModules;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
@RunWith(MockitoJUnitRunner.class)
public class BBInputSetupTest {
	private static final String RESOURCE_PATH = "src/test/resources/__files/ExecuteBuildingBlock/";

	protected ObjectMapper mapper = new ObjectMapper();

	@Spy
	private BBInputSetup bbInputSetup = new BBInputSetup();
	
	@Mock
	private BBInputSetupUtils bbInputSetupUtils;
	
	@Mock
	private BBInputSetupMapperLayer bbInputSetupMapperLayer; 
	
	@Before
	public void setup(){
		bbInputSetup.setBbInputSetupUtils(bbInputSetupUtils);
		bbInputSetup.setMapperLayer(bbInputSetupMapperLayer);
	}

	@Test
	public void testGetCustomerAndServiceSubscription() throws JsonParseException, JsonMappingException, IOException {
		RequestDetails requestDetails = mapper.readValue(
				new File(RESOURCE_PATH + "RequestDetailsInput_withRelatedInstanceList.json"), RequestDetails.class);
		SubscriberInfo subscriberInfo = new SubscriberInfo();
		subscriberInfo.setGlobalSubscriberId("globalSubscriberId");
		RequestParameters requestParams = new RequestParameters();
		requestParams.setSubscriptionServiceType("subscriptionServiceType");
		requestDetails.setRequestParameters(requestParams);
		requestDetails.setSubscriberInfo(subscriberInfo);
		String resourceId = "resourceId";
		Customer expected = new Customer();
		expected.setGlobalCustomerId("globalCustomerId");
		ServiceSubscription serviceSubscription = new ServiceSubscription();
		serviceSubscription.setServiceType("subscriptionServiceType");

		doReturn(expected).when(this.bbInputSetup).getCustomerFromRequest(requestDetails);
		doReturn(serviceSubscription).when(this.bbInputSetup).getServiceSubscription(requestDetails, expected);

		Customer actual = this.bbInputSetup.getCustomerAndServiceSubscription(requestDetails, resourceId);

		assertThat(actual, sameBeanAs(expected));

		requestDetails.setSubscriberInfo(null);

		doReturn(null).when(this.bbInputSetup).getServiceSubscription(requestDetails, expected);
		doReturn(expected).when(this.bbInputSetup).getCustomerFromURI(resourceId);
		doReturn(serviceSubscription).when(this.bbInputSetup).getServiceSubscriptionFromURI(resourceId, expected);

		assertThat(actual, sameBeanAs(expected));

	}

	@Test
	public void testSetHomingFlag() throws JsonParseException, JsonMappingException, IOException {
		GeneralBuildingBlock expected = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
				GeneralBuildingBlock.class);
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
		lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, "vnfId");
		GenericVnf genericVnfExpected = new GenericVnf();
		genericVnfExpected.setVnfId("vnfId");
		genericVnfExpected.setCallHoming(true);
		expected.getCustomer().getServiceSubscription().getServiceInstances().get(0).getVnfs().add(genericVnfExpected);
		boolean homing = true;
		GenericVnf genericVnfActual = new GenericVnf();
		genericVnfActual.setVnfId("vnfId");
		genericVnfActual.setCallHoming(false);
		GeneralBuildingBlock actual = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
				GeneralBuildingBlock.class);
		actual.getCustomer().getServiceSubscription().getServiceInstances().get(0).getVnfs().add(genericVnfActual);

		bbInputSetup.setHomingFlag(actual, homing, lookupKeyMap);
		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testGetExecuteBBFromExecution() throws IOException {
		ExecuteBuildingBlock expected = new ExecuteBuildingBlock();
		BuildingBlock bb = new BuildingBlock();
		bb.setBpmnFlowName("AssignServiceInstanceBB");
		expected.setBuildingBlock(bb);
		expected.setRequestId("00032ab7-3fb3-42e5-965d-8ea592502017");
		System.out.println(mapper.writeValueAsString(expected));
		DelegateExecution execution = Mockito.mock(DelegateExecution.class);
		doReturn(expected).when(execution).getVariable(any(String.class));
		ExecuteBuildingBlock actual = bbInputSetup.getExecuteBBFromExecution(execution);
		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testGetGBB() throws Exception {
		GeneralBuildingBlock expected = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
				GeneralBuildingBlock.class);

		ExecuteBuildingBlock executeBB = new ExecuteBuildingBlock();
		executeBB.setRequestId("requestId");
		RequestDetails requestDetails = new RequestDetails();
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelType(ModelType.service);
		requestDetails.setModelInfo(modelInfo);
		RequestParameters requestParams = new RequestParameters();
		requestParams.setaLaCarte(true);
		requestDetails.setRequestParameters(requestParams);
		doReturn(requestDetails).when(bbInputSetupUtils).getRequestDetails(executeBB.getRequestId());
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
		String resourceId = "123";
		String requestAction = "createInstance";
		doReturn(expected).when(bbInputSetup).getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
				requestAction, resourceId);
		doNothing().when(bbInputSetup).populateLookupKeyMapWithIds(any(WorkflowResourceIds.class), any());
		boolean aLaCarte = true;
		GeneralBuildingBlock actual = bbInputSetup.getGBB(executeBB, lookupKeyMap, requestAction, aLaCarte,
				resourceId, null);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testGetGBBALaCarteNonService() throws Exception {
		GeneralBuildingBlock expected = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
				GeneralBuildingBlock.class);
		ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
				ExecuteBuildingBlock.class);
		RequestDetails requestDetails = mapper.readValue(
				new File(RESOURCE_PATH + "RequestDetailsInput_withRelatedInstanceList.json"), RequestDetails.class);
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
		String requestAction = "createInstance";
		Service service = Mockito.mock(Service.class);
		ServiceInstance serviceInstance = Mockito.mock(ServiceInstance.class);
		String resourceId = "123";
		String vnfType = "vnfType";
		org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
		aaiServiceInstance.setModelVersionId("modelVersionId");
		org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = Mockito.mock(org.onap.aai.domain.yang.CloudRegion.class);
		lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, "instanceId");
		doReturn(service).when(bbInputSetupUtils).getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());
		doReturn(aaiServiceInstance).when(bbInputSetupUtils).getAAIServiceInstanceById("instanceId");
		doReturn(aaiCloudRegion).when(bbInputSetupUtils).getCloudRegion(requestDetails, "att-aic");

		doNothing().when(bbInputSetup).populateObjectsOnAssignAndCreateFlows(requestDetails, service, "bbName",
				serviceInstance, lookupKeyMap, resourceId, vnfType);
		doReturn(serviceInstance).when(bbInputSetup).getExistingServiceInstance(aaiServiceInstance);
		doReturn(expected).when(bbInputSetup).populateGBBWithSIAndAdditionalInfo(requestDetails, serviceInstance,
				executeBB, requestAction, null);

		GeneralBuildingBlock actual = bbInputSetup.getGBBALaCarteNonService(executeBB, requestDetails, lookupKeyMap,
				requestAction, resourceId, vnfType);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test(expected = Exception.class)
	public void testGetGBBALaCarteNonServiceWithoutServiceModelInfo() throws Exception {
		ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
				ExecuteBuildingBlock.class);
		RequestDetails requestDetails = mapper.readValue(
				new File(RESOURCE_PATH + "RequestDetailsInput_withRelatedInstanceList.json"), RequestDetails.class);
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
		String requestAction = "createInstance";
		org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
		aaiServiceInstance.setModelVersionId("modelVersionId");
		String resourceId = "123";
		String vnfType = "vnfType";
		doReturn(null).when(bbInputSetupUtils).getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());
		doReturn(aaiServiceInstance).when(bbInputSetupUtils).getAAIServiceInstanceById("instanceId");

		bbInputSetup.getGBBALaCarteNonService(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId,
				vnfType);
	}

	@Test
	public void testGetGBBALaCarteNonServiceWithoutRelatedInstances() throws Exception {
		GeneralBuildingBlock expected = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
				GeneralBuildingBlock.class);
		ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
				ExecuteBuildingBlock.class);
		RequestDetails requestDetails = mapper.readValue(
				new File(RESOURCE_PATH + "RequestDetailsInput_withoutRelatedInstanceList.json"), RequestDetails.class);
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
		String requestAction = "createInstance";
		Service service = Mockito.mock(Service.class);
		String resourceId = "123";
		ServiceInstance serviceInstance = Mockito.mock(ServiceInstance.class);
		org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
		aaiServiceInstance.setModelVersionId("modelVersionId");
		org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = Mockito.mock(org.onap.aai.domain.yang.CloudRegion.class);
		String vnfType = "vnfType";
		lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, "instanceId");
		doReturn(service).when(bbInputSetupUtils).getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());
		doReturn(aaiServiceInstance).when(bbInputSetupUtils).getAAIServiceInstanceById("instanceId");
		doReturn(aaiCloudRegion).when(bbInputSetupUtils).getCloudRegion(requestDetails, "att-aic");

		doNothing().when(bbInputSetup).populateObjectsOnAssignAndCreateFlows(requestDetails, service, "bbName",
				serviceInstance, lookupKeyMap, resourceId, vnfType);

		doReturn(serviceInstance).when(bbInputSetup).getExistingServiceInstance(aaiServiceInstance);
		doReturn(expected).when(bbInputSetup).populateGBBWithSIAndAdditionalInfo(requestDetails, serviceInstance,
				executeBB, requestAction,null);

		GeneralBuildingBlock actual = bbInputSetup.getGBBALaCarteNonService(executeBB, requestDetails, lookupKeyMap,
				requestAction, resourceId, vnfType);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testGetGBBALaCarteService() throws Exception {
		GeneralBuildingBlock expected = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
				GeneralBuildingBlock.class);
		ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
				ExecuteBuildingBlock.class);
		RequestDetails requestDetails = mapper.readValue(
				new File(RESOURCE_PATH + "RequestDetailsInput_withRelatedInstanceList.json"), RequestDetails.class);
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();

		org.openecomp.mso.serviceinstancebeans.Project requestProject = new org.openecomp.mso.serviceinstancebeans.Project();
		org.openecomp.mso.serviceinstancebeans.OwningEntity requestOwningEntity = new org.openecomp.mso.serviceinstancebeans.OwningEntity();
		requestDetails.setProject(requestProject);
		requestDetails.setOwningEntity(requestOwningEntity);

		Service service = Mockito.mock(Service.class);
		Customer customer = Mockito.mock(Customer.class);
		ServiceSubscription serviceSubscription = Mockito.mock(ServiceSubscription.class);
		Project project = Mockito.mock(Project.class);
		OwningEntity owningEntity = Mockito.mock(OwningEntity.class);
		ServiceInstance serviceInstance = Mockito.mock(ServiceInstance.class);
		String resourceId = "123";
		String requestAction = "createInstance";
		executeBB.setaLaCarte(true);
		executeBB.getBuildingBlock().setBpmnFlowName(AssignFlows.SERVICE_INSTANCE.toString());
		org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = Mockito.mock(org.onap.aai.domain.yang.CloudRegion.class);

		doReturn(service).when(bbInputSetupUtils)
				.getCatalogServiceByModelUUID(requestDetails.getModelInfo().getModelVersionId());
		doReturn(aaiCloudRegion).when(bbInputSetupUtils).getCloudRegion(requestDetails, "att-aic");
		doReturn(project).when(bbInputSetupMapperLayer).mapRequestProject(requestDetails.getProject());
		doReturn(owningEntity).when(bbInputSetupMapperLayer)
				.mapRequestOwningEntity(requestDetails.getOwningEntity());

		doReturn(customer).when(bbInputSetup).getCustomerAndServiceSubscription(requestDetails, resourceId);
		doReturn(serviceInstance).when(bbInputSetup).getALaCarteServiceInstance(service, requestDetails, customer,
				project, owningEntity, lookupKeyMap, resourceId, executeBB.isaLaCarte(),
				executeBB.getBuildingBlock().getBpmnFlowName());
		doReturn(expected).when(bbInputSetup).populateGBBWithSIAndAdditionalInfo(requestDetails, serviceInstance,
				executeBB, requestAction, customer);

		GeneralBuildingBlock actual = bbInputSetup.getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
				requestAction, resourceId);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testGetGBBALaCarteServiceFindServiceByModelVersionId() throws Exception {
		GeneralBuildingBlock expected = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
				GeneralBuildingBlock.class);
		ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
				ExecuteBuildingBlock.class);
		RequestDetails requestDetails = mapper.readValue(
				new File(RESOURCE_PATH + "RequestDetailsInput_withRelatedInstanceList.json"), RequestDetails.class);
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();

		org.openecomp.mso.serviceinstancebeans.Project requestProject = new org.openecomp.mso.serviceinstancebeans.Project();
		org.openecomp.mso.serviceinstancebeans.OwningEntity requestOwningEntity = new org.openecomp.mso.serviceinstancebeans.OwningEntity();
		requestDetails.setProject(requestProject);
		requestDetails.setOwningEntity(requestOwningEntity);

		Service service = Mockito.mock(Service.class);
		Customer customer = Mockito.mock(Customer.class);
		ServiceSubscription serviceSubscription = Mockito.mock(ServiceSubscription.class);
		Project project = Mockito.mock(Project.class);
		OwningEntity owningEntity = Mockito.mock(OwningEntity.class);
		ServiceInstance serviceInstance = Mockito.mock(ServiceInstance.class);
		String resourceId = "123";
		String requestAction = "createInstance";
		executeBB.setaLaCarte(true);
		executeBB.getBuildingBlock().setBpmnFlowName(AssignFlows.SERVICE_INSTANCE.toString());
		org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = Mockito.mock(org.onap.aai.domain.yang.CloudRegion.class);

		doReturn(null).when(bbInputSetupUtils)
				.getCatalogServiceByModelUUID(requestDetails.getModelInfo().getModelVersionId());
		doReturn(service).when(bbInputSetupUtils).getCatalogServiceByModelVersionAndModelInvariantUUID(
				requestDetails.getModelInfo().getModelVersion(), requestDetails.getModelInfo().getModelInvariantId());
		doReturn(aaiCloudRegion).when(bbInputSetupUtils).getCloudRegion(requestDetails, "att-aic");
		doReturn(project).when(bbInputSetupMapperLayer).mapRequestProject(requestDetails.getProject());
		doReturn(owningEntity).when(bbInputSetupMapperLayer)
				.mapRequestOwningEntity(requestDetails.getOwningEntity());

		doReturn(customer).when(bbInputSetup).getCustomerAndServiceSubscription(requestDetails, resourceId);
		doReturn(serviceSubscription).when(bbInputSetup).getServiceSubscription(requestDetails, customer);
		doReturn(serviceInstance).when(bbInputSetup).getALaCarteServiceInstance(service, requestDetails, customer,
				project, owningEntity, lookupKeyMap, resourceId, executeBB.isaLaCarte(),
				executeBB.getBuildingBlock().getBpmnFlowName());
		doReturn(expected).when(bbInputSetup).populateGBBWithSIAndAdditionalInfo(requestDetails, serviceInstance,
				executeBB, requestAction,customer);

		GeneralBuildingBlock actual = bbInputSetup.getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
				requestAction, resourceId);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testGetGBBALaCarteServiceNoProjectNoOE() throws Exception {
		GeneralBuildingBlock expected = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
				GeneralBuildingBlock.class);
		ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
				ExecuteBuildingBlock.class);
		RequestDetails requestDetails = mapper.readValue(
				new File(RESOURCE_PATH + "RequestDetailsInput_withRelatedInstanceList.json"), RequestDetails.class);
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();

		Service service = Mockito.mock(Service.class);
		Customer customer = Mockito.mock(Customer.class);
		ServiceSubscription serviceSubscription = Mockito.mock(ServiceSubscription.class);
		ServiceInstance serviceInstance = Mockito.mock(ServiceInstance.class);
		String resourceId = "123";
		String requestAction = "createInstance";
		executeBB.setaLaCarte(true);
		executeBB.getBuildingBlock().setBpmnFlowName(AssignFlows.SERVICE_INSTANCE.toString());
		org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = Mockito.mock(org.onap.aai.domain.yang.CloudRegion.class);

		Map<String, String> uriKeys = new HashMap<>();
		uriKeys.put("global-customer-id", "globalCustomerId");
		uriKeys.put("service-type", "serviceType");
		doReturn(uriKeys).when(bbInputSetupUtils)
				.getURIKeysFromServiceInstance(resourceId);
		doReturn(service).when(bbInputSetupUtils)
				.getCatalogServiceByModelUUID(requestDetails.getModelInfo().getModelVersionId());
		doReturn(aaiCloudRegion).when(bbInputSetupUtils).getCloudRegion(requestDetails, "att-aic");

		doReturn(customer).when(bbInputSetup).getCustomerAndServiceSubscription(requestDetails, resourceId);
		doReturn(serviceSubscription).when(bbInputSetup).getServiceSubscription(requestDetails, customer);
		doReturn(serviceInstance).when(bbInputSetup).getALaCarteServiceInstance(service, requestDetails, customer,
				null, null, lookupKeyMap, resourceId, executeBB.isaLaCarte(),
				executeBB.getBuildingBlock().getBpmnFlowName());
		doReturn(expected).when(bbInputSetup).populateGBBWithSIAndAdditionalInfo(requestDetails, serviceInstance,
				executeBB, requestAction,customer);

		GeneralBuildingBlock actual = bbInputSetup.getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
				requestAction, resourceId);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testGetServiceInstanceHelperCreateScenario() throws Exception {
		RequestDetails requestDetails = new RequestDetails();
		RequestInfo requestInfo = new RequestInfo();
		requestInfo.setInstanceName("SharansInstanceName");
		requestDetails.setRequestInfo(requestInfo);
		Customer customer = new Customer();
		String serviceInstanceId = "SharansInstanceId";
		boolean aLaCarte = true;
		ServiceInstance expected = new ServiceInstance();
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
		Service service = new Service();
		service.setModelUUID("modelUUID");
		String bbName = AssignFlows.SERVICE_INSTANCE.toString();

		doReturn(null).when(bbInputSetupUtils).getAAIServiceInstanceByName(requestInfo.getInstanceName(), customer);
		doReturn(null).when(bbInputSetupUtils).getAAIServiceInstanceById(serviceInstanceId);


		doReturn(expected).when(bbInputSetup).createServiceInstance(requestDetails, null, null,
				lookupKeyMap, serviceInstanceId);

		ServiceInstance actual = bbInputSetup.getServiceInstanceHelper(requestDetails, customer, null, null,
				lookupKeyMap, serviceInstanceId, aLaCarte, service, bbName);
		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testGetServiceInstanceHelperCreateScenarioExisting() throws Exception {
		RequestDetails requestDetails = new RequestDetails();
		RequestInfo requestInfo = new RequestInfo();
		requestInfo.setInstanceName("SharansInstanceName");
		requestDetails.setRequestInfo(requestInfo);
		Customer customer = new Customer();
		String serviceInstanceId = "SharansInstanceId";
		boolean aLaCarte = true;
		Service service = new Service();
		service.setModelUUID("modelUUID");
		ServiceInstance expected = new ServiceInstance();
		org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = new org.onap.aai.domain.yang.ServiceInstance();
		serviceInstanceAAI.setModelVersionId("modelUUID");
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
		String bbName = AssignFlows.SERVICE_INSTANCE.toString();

		doReturn(serviceInstanceAAI).when(bbInputSetupUtils)
				.getAAIServiceInstanceByName(requestInfo.getInstanceName(), customer);
		doReturn(expected).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);

		ServiceInstance actual = bbInputSetup.getServiceInstanceHelper(requestDetails, customer, null, null,
				lookupKeyMap, serviceInstanceId, aLaCarte, service, bbName);
		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testGetServiceInstanceHelperCreateScenarioExistingNoNameButWithId() throws Exception {
		RequestDetails requestDetails = new RequestDetails();
		RequestInfo requestInfo = new RequestInfo();
		requestInfo.setInstanceName("SharansInstanceName");
		requestDetails.setRequestInfo(requestInfo);
		Customer customer = new Customer();
		String serviceInstanceId = "SharansInstanceId";
		boolean aLaCarte = true;
		Service service = new Service();
		service.setModelUUID("modelUUID");
		ServiceInstance expected = new ServiceInstance();
		org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = new org.onap.aai.domain.yang.ServiceInstance();
		serviceInstanceAAI.setModelVersionId("modelUUID");
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
		String bbName = "ActivateServiceInstanceBB";

		doReturn(serviceInstanceAAI).when(bbInputSetupUtils).getAAIServiceInstanceById(serviceInstanceId);
		doReturn(expected).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);

		ServiceInstance actual = bbInputSetup.getServiceInstanceHelper(requestDetails, customer, null, null,
				lookupKeyMap, serviceInstanceId, aLaCarte, service, bbName);
		assertThat(actual, sameBeanAs(expected));
	}

	@Test(expected = Exception.class)
	public void testGetServiceInstanceHelperCreateScenarioExistingNoNameButWithIdExceptionThrown() throws Exception {
		RequestDetails requestDetails = new RequestDetails();
		RequestInfo requestInfo = new RequestInfo();
		requestDetails.setRequestInfo(requestInfo);
		Customer customer = new Customer();
		String serviceInstanceId = "SharansInstanceId";
		boolean aLaCarte = true;
		Service service = new Service();
		service.setModelUUID("modelUUID");
		ServiceInstance expected = new ServiceInstance();
		org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = new org.onap.aai.domain.yang.ServiceInstance();
		serviceInstanceAAI.setModelVersionId("modelUUIDDifferent");
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
		String bbName = AssignFlows.SERVICE_INSTANCE.toString();

		ServiceInstance actual = bbInputSetup.getServiceInstanceHelper(requestDetails, customer, null, null,
				lookupKeyMap, serviceInstanceId, aLaCarte, service, bbName);
		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testPopulateObjectsOnAssignAndCreateFlows() throws Exception {
		String bbName = AssignFlows.SERVICE_INSTANCE.toString();
		String instanceName = "instanceName";
		String resourceId = "123";
		String vnfType = "vnfType";
		Service service = Mockito.mock(Service.class);
		ServiceInstance serviceInstance = Mockito.mock(ServiceInstance.class);
		RequestDetails requestDetails = Mockito.mock(RequestDetails.class);
		ModelInfo modelInfo = Mockito.mock(ModelInfo.class);
		RequestInfo requestInfo = Mockito.mock(RequestInfo.class);
		org.openecomp.mso.serviceinstancebeans.Platform platform = Mockito
				.mock(org.openecomp.mso.serviceinstancebeans.Platform.class);
		org.openecomp.mso.serviceinstancebeans.LineOfBusiness lineOfBusiness = Mockito
				.mock(org.openecomp.mso.serviceinstancebeans.LineOfBusiness.class);
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();

		doNothing().when(bbInputSetup).populateL3Network(instanceName, modelInfo, service, bbName, serviceInstance,
				lookupKeyMap, resourceId);
		doNothing().when(bbInputSetup).populateGenericVnf(modelInfo, instanceName, platform, lineOfBusiness,
				service, bbName, serviceInstance, lookupKeyMap, requestDetails, resourceId, vnfType);
		doNothing().when(bbInputSetup).populateVolumeGroup(requestDetails, service, bbName, serviceInstance,
				lookupKeyMap, resourceId);
		doNothing().when(bbInputSetup).populateVfModule(requestDetails, service, bbName, serviceInstance,
				lookupKeyMap, resourceId);
		doReturn(modelInfo).when(requestDetails).getModelInfo();
		doReturn(requestInfo).when(requestDetails).getRequestInfo();
		doReturn(instanceName).when(requestInfo).getInstanceName();
		doReturn(platform).when(requestDetails).getPlatform();
		doReturn(lineOfBusiness).when(requestDetails).getLineOfBusiness();

		doReturn(ModelType.network).when(modelInfo).getModelType();

		bbInputSetup.populateObjectsOnAssignAndCreateFlows(requestDetails, service, bbName, serviceInstance,
				lookupKeyMap, resourceId, vnfType);

		verify(bbInputSetup, times(1)).populateL3Network(instanceName, modelInfo, service, bbName, serviceInstance,
				lookupKeyMap, resourceId);

		doReturn(ModelType.vnf).when(modelInfo).getModelType();

		bbInputSetup.populateObjectsOnAssignAndCreateFlows(requestDetails, service, bbName, serviceInstance,
				lookupKeyMap, resourceId, vnfType);

		verify(bbInputSetup, times(1)).populateGenericVnf(modelInfo, instanceName, platform, lineOfBusiness,
				service, bbName, serviceInstance, lookupKeyMap, requestDetails, resourceId, vnfType);

		doReturn(ModelType.volumeGroup).when(modelInfo).getModelType();

		bbInputSetup.populateObjectsOnAssignAndCreateFlows(requestDetails, service, bbName, serviceInstance,
				lookupKeyMap, resourceId, vnfType);

		verify(bbInputSetup, times(1)).populateVolumeGroup(requestDetails, service, bbName, serviceInstance,
				lookupKeyMap, resourceId);

		doReturn(ModelType.vfModule).when(modelInfo).getModelType();

		bbInputSetup.populateObjectsOnAssignAndCreateFlows(requestDetails, service, bbName, serviceInstance,
				lookupKeyMap, resourceId, vnfType);

		verify(bbInputSetup, times(1)).populateVfModule(requestDetails, service, bbName, serviceInstance,
				lookupKeyMap, resourceId);
	}

	@Test
	public void testPopulateGBBWithSIAndAdditionalInfo() throws Exception {
		GeneralBuildingBlock expected = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
				GeneralBuildingBlock.class);
		ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
				ExecuteBuildingBlock.class);
		RequestDetails requestDetails = mapper.readValue(
				new File(RESOURCE_PATH + "RequestDetailsInput_withRelatedInstanceList.json"), RequestDetails.class);
		RequestContext requestContext = mapper.readValue(new File(RESOURCE_PATH + "RequestContextExpected.json"),
				RequestContext.class);
		ServiceInstance serviceInstance = mapper.readValue(
				new File(RESOURCE_PATH + "ServiceInstance_getServiceInstanceNOAAIExpected.json"),
				ServiceInstance.class);

		OrchestrationContext orchestrationContext = new OrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(false);

		CloudRegion cloudRegion = new CloudRegion();
		cloudRegion.setCloudOwner("att-aic");
		cloudRegion.setLcpCloudRegionId("lcpCloudRegionId");
		cloudRegion.setComplex("complexName");
		cloudRegion.setTenantId("tenantId");

		Map<String, String> uriKeys = new HashMap<>();
		uriKeys.put("global-customer-id","global-customer-id");
		uriKeys.put("service-type","service-type");

		Customer customer = new Customer();
		ServiceSubscription serviceSubscription = new ServiceSubscription();
		serviceSubscription.setServiceType("subscriptionServiceType");
		customer.setGlobalCustomerId("globalCustomerId");
		customer.setSubscriberName("subscriberName");
		customer.setSubscriberType("subscriberType");
		customer.setServiceSubscription(serviceSubscription);

		org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = Mockito.mock(org.onap.aai.domain.yang.CloudRegion.class);

		String requestAction = "createInstance";

		doReturn(uriKeys).when(bbInputSetupUtils).getURIKeysFromServiceInstance(serviceInstance.getServiceInstanceId());
		doReturn(customer).when(bbInputSetup).mapCustomer(uriKeys.get("global-customer-id"),uriKeys.get("service-type"));
		doReturn(aaiCloudRegion).when(bbInputSetupUtils).getCloudRegion(requestDetails, "att-aic");
		doReturn(orchestrationContext).when(bbInputSetupMapperLayer).mapOrchestrationContext(requestDetails);
		doReturn(requestContext).when(bbInputSetupMapperLayer).mapRequestContext(requestDetails);
		doReturn(cloudRegion).when(bbInputSetupMapperLayer).mapCloudRegion(requestDetails, aaiCloudRegion,
				"att-aic");

		GeneralBuildingBlock actual = bbInputSetup.populateGBBWithSIAndAdditionalInfo(requestDetails,
				serviceInstance, executeBB, requestAction, null);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testGetServiceInstanceNOAAI() throws Exception {
		ServiceInstance expected = mapper.readValue(
				new File(RESOURCE_PATH + "ServiceInstance_getServiceInstanceNOAAIExpected.json"),
				ServiceInstance.class);
		Service service = mapper.readValue(
				new File(RESOURCE_PATH + "CatalogDBService_getServiceInstanceNOAAIInput.json"), Service.class);
		Customer customer = mapper.readValue(new File(RESOURCE_PATH + "Customer.json"), Customer.class);
		Project project = mapper.readValue(new File(RESOURCE_PATH + "Project.json"), Project.class);
		OwningEntity owningEntity = mapper.readValue(new File(RESOURCE_PATH + "OwningEntity.json"), OwningEntity.class);
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();

		ExecuteBuildingBlock executeBB = new ExecuteBuildingBlock();
		executeBB.setaLaCarte(true);
		BuildingBlock buildingBlock = new BuildingBlock();
		buildingBlock.setBpmnFlowName(AssignFlows.SERVICE_INSTANCE.toString());
		executeBB.setBuildingBlock(buildingBlock);
		RequestDetails requestDetails = new RequestDetails();
		RequestInfo reqInfo = new RequestInfo();
		reqInfo.setInstanceName("serviceInstanceName");
		requestDetails.setRequestInfo(reqInfo);
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelType(ModelType.service);
		requestDetails.setModelInfo(modelInfo);
		doReturn(null).when(bbInputSetupUtils)
				.getAAIServiceInstanceByName(requestDetails.getRequestInfo().getInstanceName(), customer);
		doReturn(expected.getModelInfoServiceInstance()).when(bbInputSetupMapperLayer)
				.mapCatalogServiceIntoServiceInstance(service);
		doReturn(null).when(bbInputSetupUtils).getAAIServiceInstanceById(any(String.class));
		String serviceInstanceId = "3655a595-05d1-433c-93c0-3afd6b572545";
		boolean aLaCarte = true;

		ServiceInstance actual = bbInputSetup.getALaCarteServiceInstance(service, requestDetails, customer, project,
				owningEntity, lookupKeyMap, serviceInstanceId, aLaCarte, executeBB.getBuildingBlock().getBpmnFlowName());

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testGetServiceSubscription() throws IOException {
		ServiceSubscription expected = new ServiceSubscription();
		RequestDetails requestDetails = new RequestDetails();
		RequestParameters params = new RequestParameters();
		params.setSubscriptionServiceType("subscriptionServiceType");
		requestDetails.setRequestParameters(params);
		org.onap.aai.domain.yang.ServiceSubscription aaiServiceSubscription = new org.onap.aai.domain.yang.ServiceSubscription();
		Customer customer = new Customer();
		customer.setGlobalCustomerId("globalCustomerId");
		doReturn(aaiServiceSubscription).when(bbInputSetupUtils).getAAIServiceSubscription(
				customer.getGlobalCustomerId(), requestDetails.getRequestParameters().getSubscriptionServiceType());
		doReturn(expected).when(bbInputSetupMapperLayer).mapAAIServiceSubscription(aaiServiceSubscription);

		ServiceSubscription actual = bbInputSetup.getServiceSubscription(requestDetails, customer);
		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testGetCustomer() throws IOException {
		Customer expected = new Customer();
		RequestDetails requestDetails = new RequestDetails();
		SubscriberInfo subscriberInfo = new SubscriberInfo();
		subscriberInfo.setGlobalSubscriberId("globalSubscriberId");
		requestDetails.setSubscriberInfo(subscriberInfo);
		org.onap.aai.domain.yang.Customer aaiCustomer = new org.onap.aai.domain.yang.Customer();
		doReturn(aaiCustomer).when(bbInputSetupUtils)
				.getAAICustomer(requestDetails.getSubscriberInfo().getGlobalSubscriberId());
		doReturn(expected).when(bbInputSetupMapperLayer).mapAAICustomer(aaiCustomer);

		Customer actual = bbInputSetup.getCustomerFromRequest(requestDetails);
		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testGetExistingServiceInstance() throws Exception {
		org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = new org.onap.aai.domain.yang.ServiceInstance();
		ServiceInstance expected = new ServiceInstance();

		doReturn(expected).when(bbInputSetupMapperLayer)
				.mapAAIServiceInstanceIntoServiceInstance(serviceInstanceAAI);

		doNothing().when(bbInputSetup).addRelationshipsToSI(serviceInstanceAAI, expected);

		ServiceInstance actual = bbInputSetup.getExistingServiceInstance(serviceInstanceAAI);
		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testPopulateNetworkCollectionAndInstanceGroupAssign() throws Exception {
		Service service = mapper.readValue(
				new File(RESOURCE_PATH + "CatalogDBService_getServiceInstanceNOAAIInput.json"), Service.class);
		ServiceInstance serviceInstance = mapper.readValue(
				new File(RESOURCE_PATH + "ServiceInstance_getServiceInstanceNOAAIExpected.json"),
				ServiceInstance.class);
		String resourceId = "123";
		Collection collection = bbInputSetup.createCollection(resourceId);
		InstanceGroup instanceGroup = bbInputSetup.createInstanceGroup();
		assertNull(serviceInstance.getCollection());
		doReturn(collection).when(bbInputSetup).createCollection(resourceId);
		doReturn(instanceGroup).when(bbInputSetup).createInstanceGroup();
		doNothing().when(bbInputSetup).mapCatalogCollection(service, collection);
		doNothing().when(bbInputSetup).mapCatalogNetworkCollectionInstanceGroup(service,
				collection.getInstanceGroup());

		bbInputSetup.populateNetworkCollectionAndInstanceGroupAssign(service,
				AssignFlows.NETWORK_COLLECTION.toString(), serviceInstance, resourceId);

		assertNotNull(serviceInstance.getCollection());
		assertNotNull(serviceInstance.getCollection().getInstanceGroup());

		verify(bbInputSetup, times(1)).mapCatalogCollection(service, serviceInstance.getCollection());
		verify(bbInputSetup, times(1)).mapCatalogNetworkCollectionInstanceGroup(service,
				serviceInstance.getCollection().getInstanceGroup());
	}

	@Test
	public void testPopulateVolumeGroup() throws Exception {
		RequestDetails requestDetails = new RequestDetails();
		RelatedInstanceList ril = new RelatedInstanceList();
		RelatedInstance ri = new RelatedInstance();
		ModelInfo mi = new ModelInfo();
		mi.setModelType(ModelType.vnf);
		mi.setModelCustomizationUuid("vnfModelCustomizationUUID");
		ri.setModelInfo(mi);
		ril.setRelatedInstance(ri);
		requestDetails.setRelatedInstanceList(new RelatedInstanceList[] { ril });

		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelType(ModelType.volumeGroup);

		RequestInfo reqInfo = new RequestInfo();
		reqInfo.setInstanceName("volumeGroupName");
		requestDetails.setModelInfo(modelInfo);
		requestDetails.setRequestInfo(reqInfo);

		ServiceInstance serviceInstance = new ServiceInstance();
		GenericVnf vnf = new GenericVnf();
		vnf.setVnfId("genericVnfId");

		VolumeGroup vg = new VolumeGroup();
		vg.setVolumeGroupName("volumeGroupName");
		vg.setVolumeGroupId("volumeGroupId");
		vnf.getVolumeGroups().add(vg);
		vnf.getVolumeGroups().add(vg);
		serviceInstance.getVnfs().add(vnf);

		Service service = mapper.readValue(
				new File(RESOURCE_PATH + "CatalogDBService_getServiceInstanceNOAAIInput.json"), Service.class);
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
		lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, "genericVnfId");

		String bbName = AssignFlows.VOLUME_GROUP.toString();
		String resourceId = "123";
		doNothing().when(bbInputSetup).mapCatalogVolumeGroup(isA(VolumeGroup.class), eq(requestDetails),
				eq(service), eq("vnfModelCustomizationUUID"));
		org.onap.aai.domain.yang.GenericVnf aaiGenericVnf = new org.onap.aai.domain.yang.GenericVnf();
		aaiGenericVnf.setModelCustomizationId("vnfModelCustomizationUUID");
		doReturn(aaiGenericVnf).when(bbInputSetupUtils).getAAIGenericVnf(vnf.getVnfId());

		bbInputSetup.populateVolumeGroup(requestDetails, service, bbName, serviceInstance, lookupKeyMap,
				resourceId);
		verify(bbInputSetup, times(2)).mapCatalogVolumeGroup(vg, requestDetails, service,
				"vnfModelCustomizationUUID");
		vnf.getVolumeGroups().clear();
		bbInputSetup.populateVolumeGroup(requestDetails, service, bbName, serviceInstance, lookupKeyMap,
				resourceId);
		verify(bbInputSetup, times(1)).mapCatalogVolumeGroup(vnf.getVolumeGroups().get(0), requestDetails, service,
				"vnfModelCustomizationUUID");
	}

	@Test
	public void testMapCatalogVolumeGroup() {
		VolumeGroup volumeGroup = new VolumeGroup();
		RequestDetails requestDetails = new RequestDetails();
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelCustomizationUuid("modelCustomizationUUID");
		requestDetails.setModelInfo(modelInfo);
		Service service = new Service();
		VnfResourceCustomization resourceCust = new VnfResourceCustomization();
		resourceCust.setModelCustomizationUUID("vnfModelCustomizationUUID");
		service.getVnfCustomizations().add(resourceCust);
		VfModuleCustomization vfResourceCust = new VfModuleCustomization();
		vfResourceCust.setModelCustomizationUUID("modelCustomizationUUID");
		ModelInfoVfModule modelInfoVfModule = new ModelInfoVfModule();
		resourceCust.getVfModuleCustomizations().add(vfResourceCust);

		doReturn(modelInfoVfModule).when(bbInputSetupMapperLayer).mapCatalogVfModuleToVfModule(vfResourceCust);

		bbInputSetup.mapCatalogVolumeGroup(volumeGroup, requestDetails, service, "vnfModelCustomizationUUID");

		assertEquals(modelInfoVfModule, volumeGroup.getModelInfoVfModule());
	}

	@Test
	public void testPopulateL3Network() throws JsonParseException, JsonMappingException, IOException {
		String instanceName = "networkName";
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelType(ModelType.network);

		ServiceInstance serviceInstance = new ServiceInstance();
		L3Network network = new L3Network();
		network.setNetworkId("networkId");
		network.setNetworkName("networkName");
		serviceInstance.getNetworks().add(network);
		String resourceId = "123";
		// Mock service
		Service service = mapper.readValue(
				new File(RESOURCE_PATH + "CatalogDBService_getServiceInstanceNOAAIInput.json"), Service.class);
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
		lookupKeyMap.put(ResourceKey.NETWORK_ID, "networkId");
		String bbName = AssignFlows.NETWORK.toString();

		doNothing().when(bbInputSetup).mapCatalogNetwork(network, modelInfo, service);

		bbInputSetup.populateL3Network(instanceName, modelInfo, service, bbName, serviceInstance, lookupKeyMap,
				resourceId);

		lookupKeyMap.put(ResourceKey.NETWORK_ID, null);

		bbInputSetup.populateL3Network(instanceName, modelInfo, service, bbName, serviceInstance, lookupKeyMap,
				resourceId);
		verify(bbInputSetup, times(2)).mapCatalogNetwork(network, modelInfo, service);

		instanceName = "networkName2";
		L3Network network2 = bbInputSetup.createNetwork(lookupKeyMap, service, instanceName, resourceId);
		doReturn(network2).when(bbInputSetup).createNetwork(lookupKeyMap, service, instanceName, resourceId);
		bbInputSetup.populateL3Network(instanceName, modelInfo, service, bbName, serviceInstance, lookupKeyMap,
				resourceId);
		verify(bbInputSetup, times(1)).mapCatalogNetwork(network2, modelInfo, service);
	}

	@Test
	public void testMapCatalogNetwork() {
		ModelInfoNetwork modelInfoNetwork = new ModelInfoNetwork();
		L3Network network = new L3Network();

		RequestDetails requestDetails = new RequestDetails();
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelCustomizationUuid("modelCustomizationUUID");
		requestDetails.setModelInfo(modelInfo);
		Service service = new Service();
		NetworkResourceCustomization resourceCust = new NetworkResourceCustomization();
		resourceCust.setModelCustomizationUUID("modelCustomizationUUID");
		service.setNetworkCustomizations(Arrays.asList(new NetworkResourceCustomization[] { resourceCust }));

		doReturn(modelInfoNetwork).when(bbInputSetupMapperLayer).mapCatalogNetworkToNetwork(resourceCust);

		bbInputSetup.mapCatalogNetwork(network, modelInfo, service);

		assertEquals(modelInfoNetwork, network.getModelInfoNetwork());
	}

	@Test
	public void testPopulateGenericVnf() throws JsonParseException, JsonMappingException, IOException {
		org.openecomp.mso.serviceinstancebeans.Platform platform = new org.openecomp.mso.serviceinstancebeans.Platform();
		org.openecomp.mso.serviceinstancebeans.LineOfBusiness lineOfBusiness = new org.openecomp.mso.serviceinstancebeans.LineOfBusiness();
		String instanceName = "vnfName";
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelType(ModelType.vnf);

		ServiceInstance serviceInstance = new ServiceInstance();
		GenericVnf vnf = new GenericVnf();
		vnf.setVnfId("genericVnfId");
		vnf.setVnfName("vnfName");
		serviceInstance.getVnfs().add(vnf);
		String vnfType = "vnfType";
		RequestDetails requestDetails = mapper.readValue(new File(RESOURCE_PATH + "RequestDetails_CreateVnf.json"),
				RequestDetails.class);

		Service service = mapper.readValue(
				new File(RESOURCE_PATH + "CatalogDBService_getServiceInstanceNOAAIInput.json"), Service.class);
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
		lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, "genericVnfId");
		String bbName = AssignFlows.VNF.toString();

		Platform expectedPlatform = new Platform();
		LineOfBusiness expectedLineOfBusiness = new LineOfBusiness();
		String resourceId = "123";
		doReturn(expectedPlatform).when(bbInputSetupMapperLayer).mapRequestPlatform(platform);
		doReturn(expectedLineOfBusiness).when(bbInputSetupMapperLayer).mapRequestLineOfBusiness(lineOfBusiness);
		doNothing().when(bbInputSetup).mapCatalogVnf(vnf, modelInfo, service);
		doReturn(null).when(bbInputSetupUtils).getAAIGenericVnf(any(String.class));
		bbInputSetup.populateGenericVnf(modelInfo, instanceName, platform, lineOfBusiness, service, bbName,
				serviceInstance, lookupKeyMap, requestDetails, resourceId, vnfType);

		lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, null);

		bbInputSetup.populateGenericVnf(modelInfo, instanceName, platform, lineOfBusiness, service, bbName,
				serviceInstance, lookupKeyMap, requestDetails, resourceId, vnfType);
		verify(bbInputSetup, times(2)).mapCatalogVnf(vnf, modelInfo, service);

		instanceName = "vnfName2";
		GenericVnf vnf2 = bbInputSetup.createGenericVnf(lookupKeyMap, instanceName, platform, lineOfBusiness, resourceId, vnfType);
		doReturn(vnf2).when(bbInputSetup).createGenericVnf(lookupKeyMap,instanceName, platform, lineOfBusiness, resourceId, vnfType);
		doNothing().when(bbInputSetup).mapNetworkCollectionInstanceGroup(vnf2,"{instanceGroupId}");
		doNothing().when(bbInputSetup).mapVnfcCollectionInstanceGroup(vnf2, modelInfo, service);
		bbInputSetup.populateGenericVnf(modelInfo, instanceName, platform, lineOfBusiness, service, bbName,
				serviceInstance, lookupKeyMap, requestDetails, resourceId, vnfType);
		verify(bbInputSetup, times(1)).mapCatalogVnf(vnf2, modelInfo, service);
		verify(bbInputSetup, times(1)).mapNetworkCollectionInstanceGroup(vnf2, "{instanceGroupId}");
		verify(bbInputSetup, times(1)).mapVnfcCollectionInstanceGroup(vnf2, modelInfo, service);
	}
	
	@Test
	public void testPopulateGenericVnfWhereVnfTypeIsNull() throws JsonParseException, JsonMappingException, IOException {
		org.openecomp.mso.serviceinstancebeans.Platform platform = new org.openecomp.mso.serviceinstancebeans.Platform();
		org.openecomp.mso.serviceinstancebeans.LineOfBusiness lineOfBusiness = new org.openecomp.mso.serviceinstancebeans.LineOfBusiness();
		String instanceName = "vnfName";
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelType(ModelType.vnf);

		ServiceInstance serviceInstance = new ServiceInstance();
		GenericVnf vnf = new GenericVnf();
		vnf.setVnfId("genericVnfId");
		vnf.setVnfName("vnfName");
		serviceInstance.getVnfs().add(vnf);
		String vnfType = null;
		RequestDetails requestDetails = mapper.readValue(new File(RESOURCE_PATH + "RequestDetails_CreateVnf.json"),
				RequestDetails.class);

		Service service = mapper.readValue(
				new File(RESOURCE_PATH + "CatalogDBService_getServiceInstanceNOAAIInput.json"), Service.class);
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
		lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, "genericVnfId");
		String bbName = AssignFlows.VNF.toString();

		Platform expectedPlatform = new Platform();
		LineOfBusiness expectedLineOfBusiness = new LineOfBusiness();
		String resourceId = "123";
		doReturn(expectedPlatform).when(bbInputSetupMapperLayer).mapRequestPlatform(platform);
		doReturn(expectedLineOfBusiness).when(bbInputSetupMapperLayer).mapRequestLineOfBusiness(lineOfBusiness);
		doNothing().when(bbInputSetup).mapCatalogVnf(vnf, modelInfo, service);

		bbInputSetup.populateGenericVnf(modelInfo, instanceName, platform, lineOfBusiness, service, bbName,
				serviceInstance, lookupKeyMap, requestDetails, resourceId, vnfType);

		lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, null);

		bbInputSetup.populateGenericVnf(modelInfo, instanceName, platform, lineOfBusiness, service, bbName,
				serviceInstance, lookupKeyMap, requestDetails, resourceId, vnfType);
		verify(bbInputSetup, times(2)).mapCatalogVnf(vnf, modelInfo, service);

		instanceName = "vnfName2";
		GenericVnf vnf2 = bbInputSetup.createGenericVnf(lookupKeyMap, instanceName, platform, lineOfBusiness,
				resourceId, vnfType);
		doReturn(vnf2).when(bbInputSetup).createGenericVnf(lookupKeyMap, instanceName, platform, lineOfBusiness,
				resourceId, vnfType);
		doNothing().when(bbInputSetup).mapNetworkCollectionInstanceGroup(vnf2, "{instanceGroupId}");
		doNothing().when(bbInputSetup).mapVnfcCollectionInstanceGroup(vnf2, modelInfo, service);
		bbInputSetup.populateGenericVnf(modelInfo, instanceName, platform, lineOfBusiness, service, bbName,
				serviceInstance, lookupKeyMap, requestDetails, resourceId, vnfType);
		verify(bbInputSetup, times(1)).mapCatalogVnf(vnf2, modelInfo, service);
		verify(bbInputSetup, times(1)).mapNetworkCollectionInstanceGroup(vnf2, "{instanceGroupId}");
		verify(bbInputSetup, times(1)).mapVnfcCollectionInstanceGroup(vnf2, modelInfo, service);
	}

	@Test
	public void testMapCatalogVnf() {
		ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
		GenericVnf genericVnf = new GenericVnf();
		RequestDetails requestDetails = new RequestDetails();
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelCustomizationUuid("modelCustomizationUUID");
		requestDetails.setModelInfo(modelInfo);
		Service service = new Service();
		VnfResourceCustomization resourceCust = new VnfResourceCustomization();
		resourceCust.setModelCustomizationUUID("vnfModelCustomizationUUID");
		service.getVnfCustomizations().add(resourceCust);
		resourceCust.setModelCustomizationUUID("modelCustomizationUUID");

		doReturn(modelInfoGenericVnf).when(bbInputSetupMapperLayer).mapCatalogVnfToVnf(resourceCust);

		bbInputSetup.mapCatalogVnf(genericVnf, modelInfo, service);

		assertEquals(modelInfoGenericVnf, genericVnf.getModelInfoGenericVnf());
	}

	@Test
	public void testMapCatalogCollectionAndInstanceGroup() {
		ModelInfoCollection modelInfoCollection = new ModelInfoCollection();
		modelInfoCollection.setCollectionFunction("collectionFunction");
		modelInfoCollection.setCollectionRole("collectionRole");
		modelInfoCollection.setCollectionType("collectionType");
		modelInfoCollection.setDescription("description");
		modelInfoCollection.setModelInvariantUUID("modelInvariantUUID");
		modelInfoCollection.setQuantity(0);

		ModelInfoInstanceGroup modelInfoInstanceGroup = new ModelInfoInstanceGroup();
		modelInfoInstanceGroup.setFunction("function");
		modelInfoInstanceGroup.setInstanceGroupRole("instanceGroupRole");
		modelInfoInstanceGroup.setModelInvariantUUID("modelInvariantUUID");
		modelInfoInstanceGroup.setModelUUID("modelUUID");
		modelInfoInstanceGroup.setType("VNFC");

		InstanceGroup instanceGroup = new InstanceGroup();
		Collection collection = new Collection();
		collection.setInstanceGroup(instanceGroup);

		CollectionResource collectionResource = new CollectionResource();
		org.openecomp.mso.db.catalog.beans.InstanceGroup catalogInstanceGroup = new org.openecomp.mso.db.catalog.beans.InstanceGroup();
		collectionResource.setToscaNodeType("NetworkCollection");
		collectionResource.setInstanceGroup(catalogInstanceGroup);

		CollectionResourceCustomization collectionCust = new NetworkCollectionResourceCustomization();
		collectionCust.setModelCustomizationUUID("modelCustomizationUUID");
		collectionCust.setCollectionResource(collectionResource);

		Service service = new Service();
		service.setCollectionResourceCustomization(collectionCust);
		ServiceInstance serviceInstance = new ServiceInstance();
		serviceInstance.setCollection(collection);

		List<CollectionResourceInstanceGroupCustomization> instanceGroupCustList = new ArrayList<>();
		CollectionResourceInstanceGroupCustomization instanceGroupCust = new CollectionResourceInstanceGroupCustomization();
		instanceGroupCust.setFunction("function");
		instanceGroupCust.setDescription("description");

		doReturn(modelInfoCollection).when(bbInputSetupMapperLayer).mapCatalogCollectionToCollection(collectionCust,
				collectionResource);
		doReturn(modelInfoInstanceGroup).when(bbInputSetupMapperLayer)
				.mapCatalogInstanceGroupToInstanceGroup(catalogInstanceGroup);
		doReturn(instanceGroupCustList).when(bbInputSetupUtils)
				.getCollectionResourceInstanceGroupCustomization(collectionCust.getModelCustomizationUUID());

		bbInputSetup.mapCatalogCollection(service, serviceInstance.getCollection());
		//bbInputSetup.mapCatalogNetworkCollectionInstanceGroup(service,
		// serviceInstance.getCollection().getInstanceGroup());

		assertThat(collection.getModelInfoCollection(), sameBeanAs(modelInfoCollection));
		// assertThat(instanceGroup.getModelInfoInstanceGroup(),
		// sameBeanAs(modelInfoInstanceGroup));
	}

	@Test
	public void testAddRelationshipsToSI() throws Exception {
		ServiceInstance serviceInstance = new ServiceInstance();
		org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = new org.onap.aai.domain.yang.ServiceInstance();
		serviceInstanceAAI.setServiceInstanceId("serviceInstanceId");

		org.onap.aai.domain.yang.RelationshipList relationshipList = new org.onap.aai.domain.yang.RelationshipList();
		org.onap.aai.domain.yang.Relationship relationship = new org.onap.aai.domain.yang.Relationship();
		relationshipList.getRelationship().add(relationship);
		serviceInstanceAAI.setRelationshipList(relationshipList);

		Map<String, String> uriKeys = new HashMap<>();
		uriKeys.put("global-customer-id", "globalCustomerId");
		uriKeys.put("service-type", "serviceType");

		doReturn(uriKeys).when(bbInputSetupUtils)
				.getURIKeysFromServiceInstance(serviceInstanceAAI.getServiceInstanceId());
		doNothing().when(bbInputSetup).mapProject(any(), eq(serviceInstance));
		doNothing().when(bbInputSetup).mapOwningEntity(any(), eq(serviceInstance));
		doNothing().when(bbInputSetup).mapL3Networks(any(), eq(serviceInstance.getNetworks()));
		doNothing().when(bbInputSetup).mapGenericVnfs(any(), eq(serviceInstance.getVnfs()));
		doNothing().when(bbInputSetup).mapCollection(any(), any(), eq(serviceInstance));

		bbInputSetup.addRelationshipsToSI(serviceInstanceAAI, serviceInstance);

		verify(bbInputSetup, times(1)).mapProject(any(), eq(serviceInstance));
		verify(bbInputSetup, times(1)).mapOwningEntity(any(), eq(serviceInstance));
		verify(bbInputSetup, times(1)).mapL3Networks(any(), eq(serviceInstance.getNetworks()));
		verify(bbInputSetup, times(1)).mapGenericVnfs(any(), eq(serviceInstance.getVnfs()));
		verify(bbInputSetup, times(1)).mapCollection(any(), any(), eq(serviceInstance));
	}

	@Test
	public void testMapGenericVnfs() throws JsonProcessingException {
		org.onap.aai.domain.yang.GenericVnf expectedAAI = new org.onap.aai.domain.yang.GenericVnf();
		org.onap.aai.domain.yang.RelationshipList relationshipList = new org.onap.aai.domain.yang.RelationshipList();
		org.onap.aai.domain.yang.Relationship relationship = new org.onap.aai.domain.yang.Relationship();
		relationshipList.getRelationship().add(relationship);
		expectedAAI.setRelationshipList(relationshipList);

		GenericVnf expected = new GenericVnf();
		AAIResourceUri aaiResourceUri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "vnfId");
		AAIResultWrapper vnfWrapper = new AAIResultWrapper(
				new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));

		doReturn(vnfWrapper).when(bbInputSetupUtils).getAAIGenericVnf(aaiResourceUri);
		doReturn(expected).when(bbInputSetupMapperLayer)
				.mapAAIGenericVnfIntoGenericVnf(isA(org.onap.aai.domain.yang.GenericVnf.class));
		doNothing().when(bbInputSetup).mapPlatform(any(), eq(expected));
		doNothing().when(bbInputSetup).mapLineOfBusiness(any(), eq(expected));
		doNothing().when(bbInputSetup).mapVolumeGroups(any(), eq(expected.getVolumeGroups()));

		List<GenericVnf> genericVnfs = new ArrayList<>();

		bbInputSetup.mapGenericVnfs(Arrays.asList(new AAIResourceUri[] { aaiResourceUri }), genericVnfs);

		assertEquals(expected, genericVnfs.get(0));
		verify(bbInputSetup, times(1)).mapPlatform(any(), eq(expected));
		verify(bbInputSetup, times(1)).mapLineOfBusiness(any(), eq(expected));
		verify(bbInputSetup, times(1)).mapVolumeGroups(any(), eq(expected.getVolumeGroups()));
	}

	@Test
	public void testMapVolumeGroups() throws JsonProcessingException {
		org.onap.aai.domain.yang.VolumeGroup expectedAAI = new org.onap.aai.domain.yang.VolumeGroup();

		VolumeGroup expected = new VolumeGroup();
		AAIResultWrapper vnfWrapper = new AAIResultWrapper(
				new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));

		doReturn(expected).when(bbInputSetupMapperLayer)
				.mapAAIVolumeGroup(isA(org.onap.aai.domain.yang.VolumeGroup.class));

		List<VolumeGroup> volumeGroupsList = new ArrayList<>();

		bbInputSetup.mapVolumeGroups(Arrays.asList(new AAIResultWrapper[] { vnfWrapper }), volumeGroupsList);

		assertEquals(expected, volumeGroupsList.get(0));
	}

	@Test
	public void testMapLineOfBusiness() throws JsonProcessingException {
		org.onap.aai.domain.yang.LineOfBusiness expectedAAI = new org.onap.aai.domain.yang.LineOfBusiness();

		LineOfBusiness expected = new LineOfBusiness();
		AAIResultWrapper vnfWrapper = new AAIResultWrapper(
				new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));

		doReturn(expected).when(bbInputSetupMapperLayer)
				.mapAAILineOfBusiness(isA(org.onap.aai.domain.yang.LineOfBusiness.class));

		GenericVnf vnf = new GenericVnf();

		bbInputSetup.mapLineOfBusiness(Arrays.asList(new AAIResultWrapper[] { vnfWrapper }), vnf);

		assertEquals(expected, vnf.getLineOfBusiness());
	}

	@Test
	public void testMapPlatform() throws JsonProcessingException {
		org.onap.aai.domain.yang.Platform expectedAAI = new org.onap.aai.domain.yang.Platform();

		Platform expected = new Platform();
		AAIResultWrapper vnfWrapper = new AAIResultWrapper(
				new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));

		doReturn(expected).when(bbInputSetupMapperLayer)
				.mapAAIPlatform(isA(org.onap.aai.domain.yang.Platform.class));

		GenericVnf vnf = new GenericVnf();

		bbInputSetup.mapPlatform(Arrays.asList(new AAIResultWrapper[] { vnfWrapper }), vnf);

		assertEquals(expected, vnf.getPlatform());
	}

	@Test
	public void testMapCollection() throws JsonProcessingException {
		List<AAIResultWrapper> collections = new ArrayList<>();
		List<AAIResultWrapper> instanceGroups = new ArrayList<>();
		ServiceInstance serviceInstance = new ServiceInstance();

		org.onap.aai.domain.yang.Collection aaiCollection = new org.onap.aai.domain.yang.Collection();
		org.onap.aai.domain.yang.InstanceGroup aaiInstanceGroup = new org.onap.aai.domain.yang.InstanceGroup();
		org.onap.aai.domain.yang.RelationshipList relationshipList = new org.onap.aai.domain.yang.RelationshipList();
		org.onap.aai.domain.yang.Relationship relationship = new org.onap.aai.domain.yang.Relationship();
		relationshipList.getRelationship().add(relationship);
		aaiInstanceGroup.setRelationshipList(relationshipList);

		collections.add(new AAIResultWrapper(
				new AAICommonObjectMapperProvider().getMapper().writeValueAsString(aaiCollection)));
		instanceGroups.add(new AAIResultWrapper(
				new AAICommonObjectMapperProvider().getMapper().writeValueAsString(aaiInstanceGroup)));

		Collection collection = new Collection();
		InstanceGroup instanceGroup = new InstanceGroup();

		doReturn(collection).when(bbInputSetupMapperLayer)
				.mapAAICollectionIntoCollection(isA(org.onap.aai.domain.yang.Collection.class));
		doReturn(instanceGroup).when(bbInputSetupMapperLayer)
				.mapAAIInstanceGroupIntoInstanceGroup(isA(org.onap.aai.domain.yang.InstanceGroup.class));
		doNothing().when(bbInputSetup).mapL3Networks(any(), eq(instanceGroup.getL3Networks()));

		bbInputSetup.mapCollection(collections, instanceGroups, serviceInstance);

		verify(bbInputSetup, times(1)).mapL3Networks(any(), eq(instanceGroup.getL3Networks()));
		assertEquals(collection, serviceInstance.getCollection());
		assertEquals(instanceGroup, collection.getInstanceGroup());
	}

	@Test
	public void testMapL3Networks() throws JsonProcessingException {
		org.onap.aai.domain.yang.L3Network expectedAAI = new org.onap.aai.domain.yang.L3Network();
		org.onap.aai.domain.yang.RelationshipList relationshipList = new org.onap.aai.domain.yang.RelationshipList();
		org.onap.aai.domain.yang.Relationship relationship = new org.onap.aai.domain.yang.Relationship();
		relationshipList.getRelationship().add(relationship);
		expectedAAI.setRelationshipList(relationshipList);

		L3Network expected = new L3Network();
		List<L3Network> l3Networks = new ArrayList<>();
		AAIResultWrapper l3NetworksWrapper = new AAIResultWrapper(
				new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));
		AAIResourceUri aaiResourceUri = AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, "networkId");

		doReturn(l3NetworksWrapper).when(bbInputSetupUtils).getAAIL3Network(aaiResourceUri);
		doReturn(expected).when(bbInputSetupMapperLayer)
				.mapAAIL3Network(isA(org.onap.aai.domain.yang.L3Network.class));
		doNothing().when(bbInputSetup).mapNetworkPolicies(any(), eq(expected.getNetworkPolicies()));
		doNothing().when(bbInputSetup).mapRouteTableReferences(any(),
				eq(expected.getContrailNetworkRouteTableReferences()));

		bbInputSetup.mapL3Networks(Arrays.asList(new AAIResourceUri[] { aaiResourceUri }), l3Networks);

		assertEquals(expected, l3Networks.get(0));
		verify(bbInputSetup, times(1)).mapNetworkPolicies(any(), eq(expected.getNetworkPolicies()));
		verify(bbInputSetup, times(1)).mapRouteTableReferences(any(),
				eq(expected.getContrailNetworkRouteTableReferences()));
	}

	@Test
	public void testMapRouteTableReferences() throws JsonProcessingException {
		org.onap.aai.domain.yang.RouteTableReference expectedAAI = new org.onap.aai.domain.yang.RouteTableReference();

		RouteTableReference expected = new RouteTableReference();
		List<RouteTableReference> contrailNetworkRouteTableReferences = new ArrayList<>();
		AAIResultWrapper vnfWrapper = new AAIResultWrapper(
				new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));

		doReturn(expected).when(bbInputSetupMapperLayer).mapAAIRouteTableReferenceIntoRouteTableReference(
				isA(org.onap.aai.domain.yang.RouteTableReference.class));

		bbInputSetup.mapRouteTableReferences(Arrays.asList(new AAIResultWrapper[] { vnfWrapper }),
				contrailNetworkRouteTableReferences);

		assertEquals(expected, contrailNetworkRouteTableReferences.get(0));
	}

	@Test
	public void testMapOwningEntity() throws JsonProcessingException {
		org.onap.aai.domain.yang.OwningEntity expectedAAI = new org.onap.aai.domain.yang.OwningEntity();

		OwningEntity expected = new OwningEntity();
		AAIResultWrapper vnfWrapper = new AAIResultWrapper(
				new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));

		doReturn(expected).when(bbInputSetupMapperLayer)
				.mapAAIOwningEntity(isA(org.onap.aai.domain.yang.OwningEntity.class));

		ServiceInstance serviceInstance = new ServiceInstance();

		bbInputSetup.mapOwningEntity(Arrays.asList(new AAIResultWrapper[] { vnfWrapper }), serviceInstance);

		assertEquals(expected, serviceInstance.getOwningEntity());
	}

	@Test
	public void testMapProject() throws JsonProcessingException {
		org.onap.aai.domain.yang.Project expectedAAI = new org.onap.aai.domain.yang.Project();

		Project expected = new Project();
		AAIResultWrapper vnfWrapper = new AAIResultWrapper(
				new AAICommonObjectMapperProvider().getMapper().writeValueAsString(expectedAAI));

		doReturn(expected).when(bbInputSetupMapperLayer).mapAAIProject(isA(org.onap.aai.domain.yang.Project.class));

		ServiceInstance serviceInstance = new ServiceInstance();

		bbInputSetup.mapProject(Arrays.asList(new AAIResultWrapper[] { vnfWrapper }), serviceInstance);

		assertEquals(expected, serviceInstance.getProject());
	}

	@Test
	public void testMapCustomer() throws Exception {
		org.onap.aai.domain.yang.Customer customerAAI = new org.onap.aai.domain.yang.Customer();
		org.onap.aai.domain.yang.ServiceSubscription serviceSubscriptionAAI = new org.onap.aai.domain.yang.ServiceSubscription();

		Customer expected = new Customer();
		ServiceSubscription serviceSubscription = new ServiceSubscription();

		String globalCustomerId = "globalCustomerId";
		String subscriptionServiceType = "subscriptionServiceType";

		doReturn(customerAAI).when(bbInputSetupUtils).getAAICustomer(globalCustomerId);
		doReturn(serviceSubscriptionAAI).when(bbInputSetupUtils).getAAIServiceSubscription(globalCustomerId,
				subscriptionServiceType);
		doReturn(expected).when(bbInputSetupMapperLayer)
				.mapAAICustomer(isA(org.onap.aai.domain.yang.Customer.class));
		doReturn(serviceSubscription).when(bbInputSetupMapperLayer)
				.mapAAIServiceSubscription(isA(org.onap.aai.domain.yang.ServiceSubscription.class));

		Customer actual = bbInputSetup.mapCustomer(globalCustomerId, subscriptionServiceType);

		assertEquals(expected, actual);
		assertEquals(serviceSubscription, expected.getServiceSubscription());
	}

	@Test
	public void testPopulateLookupKeyMapWithIds() {
		Map<ResourceKey, String> expected = new HashMap<>();
		Map<ResourceKey, String> actual = new HashMap<>();
		String serviceInstanceId = "serviceInstanceId";
		String networkId = "networkId";
		String vnfId = "vnfId";
		String vfModuleId = "vfModuleId";
		String volumeGroupId = "volumeGroupId";

		expected.put(ResourceKey.SERVICE_INSTANCE_ID, serviceInstanceId);
		expected.put(ResourceKey.NETWORK_ID, networkId);
		expected.put(ResourceKey.GENERIC_VNF_ID, vnfId);
		expected.put(ResourceKey.VF_MODULE_ID, vfModuleId);
		expected.put(ResourceKey.VOLUME_GROUP_ID, volumeGroupId);

		WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
		workflowResourceIds.setServiceInstanceId("serviceInstanceId");
		workflowResourceIds.setNetworkId("networkId");
		workflowResourceIds.setVnfId("vnfId");
		workflowResourceIds.setVfModuleId("vfModuleId");
		workflowResourceIds.setVolumeGroupId("volumeGroupId");

		bbInputSetup.populateLookupKeyMapWithIds(workflowResourceIds, actual);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testgetGBBMacro() throws Exception {
		GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
				GeneralBuildingBlock.class);
		ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
				ExecuteBuildingBlock.class);
		RequestDetails requestDetails = mapper
				.readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
		String resourceId = "123";
		String vnfType = "vnfType";
		Service service = Mockito.mock(Service.class);
		String requestAction = "createInstance";
		doReturn(gBB).when(bbInputSetup).getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
				requestAction, resourceId);
		doReturn(service).when(bbInputSetupUtils)
				.getCatalogServiceByModelUUID(requestDetails.getModelInfo().getModelVersionId());

		executeBB.getBuildingBlock().setBpmnFlowName("Network");
		executeBB.getBuildingBlock().setSequenceNumber(0);
		bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
		verify(bbInputSetup, times(1)).populateL3Network(any(String.class), isA(ModelInfo.class),
				isA(Service.class), any(String.class), isA(ServiceInstance.class), any(), any(String.class));

		executeBB.getBuildingBlock().setBpmnFlowName("Vnf");
		bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
		verify(bbInputSetup, times(1)).populateGenericVnf(isA(ModelInfo.class), any(String.class),
				isA(org.openecomp.mso.serviceinstancebeans.Platform.class),
				isA(org.openecomp.mso.serviceinstancebeans.LineOfBusiness.class), isA(Service.class), any(String.class),
				isA(ServiceInstance.class), any(), isA(RequestDetails.class), any(String.class), any(String.class));

		executeBB.getBuildingBlock().setBpmnFlowName("VfModule");
		bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
		verify(bbInputSetup, times(1)).populateMacroVfModule(isA(VfModules.class), any(String.class),
				any(String.class), isA(Service.class), any(String.class), isA(ServiceInstance.class), any(), any(),
				any(String.class));
	}
	
	@Test
	public void testgetGBBMacroWithVnfTypeNull() throws Exception {
		GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
				GeneralBuildingBlock.class);
		ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
				ExecuteBuildingBlock.class);
		RequestDetails requestDetails = mapper
				.readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
		String resourceId = "123";
		String vnfType = null;
		Service service = Mockito.mock(Service.class);
		String requestAction = "createInstance";
		doReturn(gBB).when(bbInputSetup).getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
				requestAction, resourceId);
		doReturn(service).when(bbInputSetupUtils)
				.getCatalogServiceByModelUUID(requestDetails.getModelInfo().getModelVersionId());

		executeBB.getBuildingBlock().setBpmnFlowName("Network");
		executeBB.getBuildingBlock().setSequenceNumber(0);
		bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
		verify(bbInputSetup, times(1)).populateL3Network(any(String.class), isA(ModelInfo.class),
				isA(Service.class), any(String.class), isA(ServiceInstance.class), any(), any(String.class));

		executeBB.getBuildingBlock().setBpmnFlowName("Vnf");
		bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
		verify(bbInputSetup, times(1)).populateGenericVnf(isA(ModelInfo.class), any(String.class),
				isA(org.openecomp.mso.serviceinstancebeans.Platform.class),
				isA(org.openecomp.mso.serviceinstancebeans.LineOfBusiness.class), isA(Service.class), any(String.class),
				isA(ServiceInstance.class), any(), isA(RequestDetails.class), any(String.class), any(String.class));

		executeBB.getBuildingBlock().setBpmnFlowName("VfModule");
		bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
		verify(bbInputSetup, times(1)).populateMacroVfModule(isA(VfModules.class), any(String.class),
				any(String.class), isA(Service.class), any(String.class), isA(ServiceInstance.class), any(), any(),
				any(String.class));
	}

	@Test
	public void testgetGBBMacroGetServiceWithInvariantId() throws Exception {
		GeneralBuildingBlock gBB = mapper.readValue(new File(RESOURCE_PATH + "GeneralBuildingBlockExpected.json"),
				GeneralBuildingBlock.class);
		ExecuteBuildingBlock executeBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockSimple.json"),
				ExecuteBuildingBlock.class);
		RequestDetails requestDetails = mapper
				.readValue(new File(RESOURCE_PATH + "RequestDetailsInput_serviceMacro.json"), RequestDetails.class);
		Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
		String resourceId = "123";
		String vnfType = "vnfType";
		Service service = Mockito.mock(Service.class);
		String requestAction = "createInstance";
		doReturn(gBB).when(bbInputSetup).getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap,
				requestAction, resourceId);
		doReturn(null).when(bbInputSetupUtils)
				.getCatalogServiceByModelUUID(requestDetails.getModelInfo().getModelVersionId());
		doReturn(service).when(bbInputSetupUtils).getCatalogServiceByModelVersionAndModelInvariantUUID(
				requestDetails.getModelInfo().getModelVersion(), requestDetails.getModelInfo().getModelInvariantId());

		executeBB.getBuildingBlock().setBpmnFlowName("Network");
		executeBB.getBuildingBlock().setSequenceNumber(0);
		bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
		verify(bbInputSetup, times(1)).populateL3Network(any(String.class), isA(ModelInfo.class),
				isA(Service.class), any(String.class), isA(ServiceInstance.class), any(), any(String.class));

		executeBB.getBuildingBlock().setBpmnFlowName("Vnf");
		bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
		verify(bbInputSetup, times(1)).populateGenericVnf(isA(ModelInfo.class), any(String.class),
				isA(org.openecomp.mso.serviceinstancebeans.Platform.class),
				isA(org.openecomp.mso.serviceinstancebeans.LineOfBusiness.class), isA(Service.class), any(String.class),
				isA(ServiceInstance.class), any(), isA(RequestDetails.class), any(String.class), any(String.class));

		executeBB.getBuildingBlock().setBpmnFlowName("VfModule");
		bbInputSetup.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
		verify(bbInputSetup, times(1)).populateMacroVfModule(isA(VfModules.class), any(String.class),
				any(String.class), isA(Service.class), any(String.class), isA(ServiceInstance.class), any(), any(),
				any(String.class));
	}
}
