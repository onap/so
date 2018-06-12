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

package org.openecomp.mso.client.db.catalog;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.openecomp.mso.db.catalog.beans.BuildingBlockDetail;
import org.openecomp.mso.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.openecomp.mso.db.catalog.beans.InstanceGroup;
import org.openecomp.mso.db.catalog.beans.OrchestrationAction;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatus;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatusStateTransitionDirective;
import org.openecomp.mso.db.catalog.beans.ResourceType;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.openecomp.mso.db.catalog.beans.macro.NorthBoundRequest;
import org.openecomp.mso.db.catalog.beans.macro.OrchestrationFlow;
import org.openecomp.mso.db.catalog.beans.macro.RainyDayHandlerStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import uk.co.blackpepper.bowman.Client;
import uk.co.blackpepper.bowman.ClientFactory;
import uk.co.blackpepper.bowman.Configuration;
import uk.co.blackpepper.bowman.RestTemplateConfigurer;

@Component("CatalogDbClient")
public class CatalogDbClient {

	protected Client<Service> serviceClient;

	protected Client<VfModuleCustomization> vfModuleCustomizationClient;

	protected Client<OrchestrationFlow> orchestrationClient;

	protected Client<NorthBoundRequest> northBoundRequestClient;

	protected Client<RainyDayHandlerStatus> rainyDayHandlerStatusClient;

	protected Client<BuildingBlockDetail> buildingBlockDetailClient;

	protected Client<OrchestrationStatusStateTransitionDirective> orchestrationStatusStateTransitionDirectiveClient;

	protected Client<VnfcInstanceGroupCustomization> vnfcInstanceGroupCustomizationClient;

	protected Client<CollectionResourceInstanceGroupCustomization> collectionResourceInstanceGroupCustomizationClient;

	protected Client<InstanceGroup> instanceGroupClient;

	@Value("${mso.catalog.db.spring.endpoint}")
	protected String endpoint;

	@Value("${mso.db.auth}")
	private String msoAdaptersAuth;

	public CatalogDbClient() {
		ClientFactory clientFactory = Configuration.builder().setRestTemplateConfigurer(new RestTemplateConfigurer() {

			public void configure(RestTemplate restTemplate) {

				restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {

					public ClientHttpResponse intercept(HttpRequest request, byte[] body,
							ClientHttpRequestExecution execution) throws IOException {

						request.getHeaders().add("Authorization", msoAdaptersAuth);
						return execution.execute(request, body);
					}
				});
			}
		}).build().buildClientFactory();
		serviceClient = clientFactory.create(Service.class);
		orchestrationClient = clientFactory.create(OrchestrationFlow.class);
		vfModuleCustomizationClient = clientFactory.create(VfModuleCustomization.class);
		northBoundRequestClient = clientFactory.create(NorthBoundRequest.class);
		rainyDayHandlerStatusClient = clientFactory.create(RainyDayHandlerStatus.class);
		buildingBlockDetailClient = clientFactory.create(BuildingBlockDetail.class);
		orchestrationStatusStateTransitionDirectiveClient = clientFactory
				.create(OrchestrationStatusStateTransitionDirective.class);
		vnfcInstanceGroupCustomizationClient = clientFactory.create(VnfcInstanceGroupCustomization.class);
		collectionResourceInstanceGroupCustomizationClient = clientFactory
				.create(CollectionResourceInstanceGroupCustomization.class);
		instanceGroupClient = clientFactory.create(InstanceGroup.class);
	}

	public Service getServiceByID(String modelUUID) {
		Service service = this.getSingleService(UriBuilder.fromUri(endpoint + "/service/" + modelUUID).build());
		if (service != null) {
			service.setModelUUID(modelUUID);
		}
		return service;
	}

	public BuildingBlockDetail getBuildingBlockDetail(String buildingBlockName) {
		BuildingBlockDetail buildingBlockDetail = buildingBlockDetailClient
				.get(UriBuilder.fromUri(endpoint + "/buildingBlockDetail/search/findOneByBuildingBlockName")
						.queryParam("buildingBlockName", buildingBlockName).build());
		if (buildingBlockDetail != null) {
			buildingBlockDetail.setBuildingBlockName(buildingBlockName);
		}
		return buildingBlockDetail;
	}

	public InstanceGroup getInstanceGroupByModelUUID(String modelUUID) {
		InstanceGroup instanceGroup = this
				.getSingleInstanceGroup(UriBuilder.fromUri(endpoint + "/instanceGroup/" + modelUUID).build());
		if (instanceGroup != null) {
			instanceGroup.setModelUUID(modelUUID);
		}
		return instanceGroup;
	}

	public OrchestrationStatusStateTransitionDirective getOrchestrationStatusStateTransitionDirective(
			ResourceType resourceType, OrchestrationStatus orchestrationStatus, OrchestrationAction targetAction) {
		return orchestrationStatusStateTransitionDirectiveClient.get(UriBuilder
				.fromUri(
						endpoint + "/orchestrationStatusStateTransitionDirective/search/findOneByResourceTypeAndOrchestrationStatusAndTargetAction")
				.queryParam("resourceType", resourceType.name())
				.queryParam("orchestrationStatus", orchestrationStatus.name())
				.queryParam("targetAction", targetAction.name()).build());
	}

	public List<OrchestrationFlow> getOrchestrationFlowByAction(String action) {
		return this
				.getMultipleOrchestrationFlows(UriBuilder.fromUri(endpoint + "/orchestrationFlow/search/findByAction")
						.queryParam("COMPOSITE_ACTION", action).build());
	}

	public List<OrchestrationFlow> getAllOrchestrationFlows() {
		return this.getMultipleOrchestrationFlows(UriBuilder.fromUri(endpoint + "/orchestrationFlow/").build());
	}

	protected List<OrchestrationFlow> getMultipleOrchestrationFlows(URI uri) {
		Iterable<OrchestrationFlow> orchIterator = orchestrationClient.getAll(uri);
		List<OrchestrationFlow> orchList = new ArrayList<>();
		Iterator<OrchestrationFlow> it = orchIterator.iterator();
		it.forEachRemaining(orchList::add);
		return orchList;
	}

	public List<VnfcInstanceGroupCustomization> getVnfcInstanceGroupsByVnfResourceCust(String modelCustomizationUUID) {
		return this.getMultipleVnfcInstanceGroupCustomizations(
				UriBuilder.fromUri(endpoint + "/vnfcInstanceGroupCustomization/search/findByModelCustomizationUUID")
						.queryParam("MODEL_CUSTOMIZATION_UUID", modelCustomizationUUID).build());
	}

	public List<CollectionResourceInstanceGroupCustomization> getCollectionResourceInstanceGroupCustomizationByModelCustUUID(
			String modelCustomizationUUID) {
		return this.getMultipleCollectionResourceInstanceGroupCustomizations(UriBuilder
				.fromUri(endpoint + "/collectionResourceInstanceGroupCustomization/search/findByModelCustomizationUUID")
				.queryParam("MODEL_CUSTOMIZATION_UUID", modelCustomizationUUID).build());
	}

	private List<CollectionResourceInstanceGroupCustomization> getMultipleCollectionResourceInstanceGroupCustomizations(
			URI uri) {
		Iterable<CollectionResourceInstanceGroupCustomization> collectionInstanceGroupCustIter = collectionResourceInstanceGroupCustomizationClient
				.getAll(uri);
		List<CollectionResourceInstanceGroupCustomization> collectionInstanceGroupCustList = new ArrayList<>();
		Iterator<CollectionResourceInstanceGroupCustomization> it = collectionInstanceGroupCustIter.iterator();
		it.forEachRemaining(collectionInstanceGroupCustList::add);
		return collectionInstanceGroupCustList;
	}

	protected List<VnfcInstanceGroupCustomization> getMultipleVnfcInstanceGroupCustomizations(URI uri) {
		Iterable<VnfcInstanceGroupCustomization> vnfcIterator = vnfcInstanceGroupCustomizationClient.getAll(uri);
		List<VnfcInstanceGroupCustomization> vnfcList = new ArrayList<>();
		Iterator<VnfcInstanceGroupCustomization> it = vnfcIterator.iterator();
		it.forEachRemaining(vnfcList::add);
		return vnfcList;
	}

	public VfModuleCustomization getVfModuleCustomizationByModelCuztomizationUUID(String modelCustomizationUUID) {
		VfModuleCustomization vfModuleCust = this.getSingleVfModuleCustomization(
				UriBuilder.fromUri(endpoint + "/vfModuleCustomization/" + modelCustomizationUUID).build());
		if (vfModuleCust != null) {
			vfModuleCust.setModelCustomizationUUID(modelCustomizationUUID);
		}
		return vfModuleCust;
	}

	public NorthBoundRequest getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(String requestAction,
			String resourceName, boolean aLaCarte) {
		return this.getSingleNorthBoundRequest(UriBuilder
				.fromUri(endpoint + "/northbound_request_ref_lookup/search/findOneByActionAndRequestScopeAndIsAlacarte")
				.queryParam("ACTION", requestAction).queryParam("REQUEST_SCOPE", resourceName)
				.queryParam("IS_ALACARTE", aLaCarte).build());
	}

	public RainyDayHandlerStatus getRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep(
			String flowName, String serviceType, String vnfType, String errorCode, String workStep) {
		return this.getSingleRainyDayHandlerStatus(UriBuilder
				.fromUri(
						endpoint + "/rainy_day_handler_macro/search/findOneByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep")
				.queryParam("FLOW_NAME", flowName).queryParam("SERVICE_TYPE", serviceType)
				.queryParam("VNF_TYPE", vnfType).queryParam("ERROR_CODE", errorCode).queryParam("WORK_STEP", workStep)
				.build());
	}

	private InstanceGroup getSingleInstanceGroup(URI uri) {
		return instanceGroupClient.get(uri);
	}

	protected Service getSingleService(URI uri) {
		return serviceClient.get(uri);
	}

	protected VfModuleCustomization getSingleVfModuleCustomization(URI uri) {
		return vfModuleCustomizationClient.get(uri);
	}

	protected NorthBoundRequest getSingleNorthBoundRequest(URI uri) {
		return northBoundRequestClient.get(uri);
	}

	protected RainyDayHandlerStatus getSingleRainyDayHandlerStatus(URI uri) {
		return rainyDayHandlerStatusClient.get(uri);
	}

	public Service getServiceByModelVersionAndModelInvariantUUID(String modelVersion, String modelInvariantUUID) {
		return this.getSingleService(
				UriBuilder.fromUri(endpoint + "/service/search/findByModelVersionAndModelInvariantUUID")
						.queryParam("MODEL_VERSION", modelVersion)
						.queryParam("MODEL_INVARIANT_UUID", modelInvariantUUID).build());
	}
}