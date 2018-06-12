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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Collection;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Configuration;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.L3Network;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.LineOfBusiness;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.NetworkPolicy;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.OrchestrationContext;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.OwningEntity;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Platform;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Project;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.RouteTableReference;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ResourceKey;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoCollection;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoNetwork;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;
import org.openecomp.mso.db.catalog.beans.CollectionResource;
import org.openecomp.mso.db.catalog.beans.CollectionResourceCustomization;
import org.openecomp.mso.db.catalog.beans.InstanceGroup;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatus;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;
import org.openecomp.mso.serviceinstancebeans.CloudConfiguration;
import org.openecomp.mso.serviceinstancebeans.RequestDetails;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BBInputSetupMapperLayerTest {
	@InjectMocks
	BBInputSetupMapperLayer bbInputSetupMapperLayer = new BBInputSetupMapperLayer();

	ObjectMapper mapper = new ObjectMapper();

	private static final String RESOURCE_PATH = "src/test/resources/__files/ExecuteBuildingBlock/";

	@Test
	public void testMapOrchestrationStatusFromAAI() {
		OrchestrationStatus expected = OrchestrationStatus.INVENTORIED;
		String orchStatusInput = "Inventoried";
		OrchestrationStatus actual = bbInputSetupMapperLayer.mapOrchestrationStatusFromAAI(orchStatusInput);
		assertThat(actual, sameBeanAs(expected));
		
		expected = OrchestrationStatus.ASSIGNED;
		orchStatusInput = "Assigned";
		actual = bbInputSetupMapperLayer.mapOrchestrationStatusFromAAI(orchStatusInput);
		assertThat(actual, sameBeanAs(expected));
		
		expected = OrchestrationStatus.ACTIVE;
		orchStatusInput = "Active";
		actual = bbInputSetupMapperLayer.mapOrchestrationStatusFromAAI(orchStatusInput);
		assertThat(actual, sameBeanAs(expected));
		
		expected = OrchestrationStatus.CREATED;
		orchStatusInput = "Created";
		actual = bbInputSetupMapperLayer.mapOrchestrationStatusFromAAI(orchStatusInput);
		assertThat(actual, sameBeanAs(expected));
		
		expected = OrchestrationStatus.PRECREATED;
		orchStatusInput = "PreCreated";
		actual = bbInputSetupMapperLayer.mapOrchestrationStatusFromAAI(orchStatusInput);
		assertThat(actual, sameBeanAs(expected));
		
		expected = OrchestrationStatus.PENDING_CREATE;
		orchStatusInput = "PendingCreate";
		actual = bbInputSetupMapperLayer.mapOrchestrationStatusFromAAI(orchStatusInput);
		assertThat(actual, sameBeanAs(expected));
		
		expected = OrchestrationStatus.PENDING_DELETE;
		orchStatusInput = "PendingDelete";
		actual = bbInputSetupMapperLayer.mapOrchestrationStatusFromAAI(orchStatusInput);
		assertThat(actual, sameBeanAs(expected));
		
		expected = OrchestrationStatus.PENDING;
		orchStatusInput = "Pending";
		actual = bbInputSetupMapperLayer.mapOrchestrationStatusFromAAI(orchStatusInput);
		assertThat(actual, sameBeanAs(expected));
		
		expected = OrchestrationStatus.PENDING_ACTIVATION;
		orchStatusInput = "PendingActivation";
		actual = bbInputSetupMapperLayer.mapOrchestrationStatusFromAAI(orchStatusInput);
		assertThat(actual, sameBeanAs(expected));
	}
	
	@Test
	public void testMapAAICustomer() throws IOException {
		Customer expected = mapper.readValue(
				new File(RESOURCE_PATH+"Customer.json"),
				Customer.class);

		org.onap.aai.domain.yang.Customer customerAAI = mapper.readValue(
				new File(RESOURCE_PATH+"Customer_AAI.json"), org.onap.aai.domain.yang.Customer.class);
		
		Customer actual = bbInputSetupMapperLayer.mapAAICustomer(customerAAI);
		
		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testMapAAIServiceSubscription() throws IOException {
		ServiceSubscription expected = mapper.readValue(
				new File(RESOURCE_PATH + "ServiceSubscriptionExpected.json"), ServiceSubscription.class);

		org.onap.aai.domain.yang.ServiceSubscription svcSubscriptionAAI = mapper.readValue(
				new File(RESOURCE_PATH + "ServiceSubscription_AAI.json"), org.onap.aai.domain.yang.ServiceSubscription.class);

		ServiceSubscription actual = bbInputSetupMapperLayer.mapAAIServiceSubscription(svcSubscriptionAAI);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testMapAAIProject() throws IOException {
		Project expected = mapper.readValue(new File(RESOURCE_PATH+"Project.json"),Project.class);

		org.onap.aai.domain.yang.Project projectAAI = new org.onap.aai.domain.yang.Project();
		projectAAI.setProjectName("projectName");

		Project actual = bbInputSetupMapperLayer.mapAAIProject(projectAAI);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testMapRequestProject() throws IOException {
		Project expected = mapper.readValue(new File(RESOURCE_PATH+"Project.json"),Project.class);

		org.openecomp.mso.serviceinstancebeans.Project requestProject = new org.openecomp.mso.serviceinstancebeans.Project();
		requestProject.setProjectName("projectName");

		Project actual = bbInputSetupMapperLayer.mapRequestProject(requestProject);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testMapAAIOwningEntity() throws IOException {
		OwningEntity expected = mapper.readValue(new File(RESOURCE_PATH+"OwningEntity.json"),OwningEntity.class);

		org.onap.aai.domain.yang.OwningEntity entityAAI = new org.onap.aai.domain.yang.OwningEntity();
		entityAAI.setOwningEntityId("owningEntityId");
		entityAAI.setOwningEntityName("owningEntityName");

		OwningEntity actual = bbInputSetupMapperLayer.mapAAIOwningEntity(entityAAI);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testMapRequestOwningEntity() throws IOException {
		OwningEntity expected = mapper.readValue(new File(RESOURCE_PATH+"OwningEntity.json"),OwningEntity.class);

		org.openecomp.mso.serviceinstancebeans.OwningEntity requestOwningEntity = new org.openecomp.mso.serviceinstancebeans.OwningEntity();
		requestOwningEntity.setOwningEntityId("owningEntityId");
		requestOwningEntity.setOwningEntityName("owningEntityName");

		OwningEntity actual = bbInputSetupMapperLayer.mapRequestOwningEntity(requestOwningEntity);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testMapAAIPlatform() throws IOException {
		Platform expected = mapper.readValue(new File(RESOURCE_PATH+"Platform.json"),Platform.class);

		org.onap.aai.domain.yang.Platform platformAAI = new org.onap.aai.domain.yang.Platform();
		platformAAI.setPlatformName("platformName");

		Platform actual = bbInputSetupMapperLayer.mapAAIPlatform(platformAAI);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testMapAAILineOfBusiness() throws IOException {
		LineOfBusiness expected = mapper.readValue(new File(RESOURCE_PATH+"LineOfBusiness.json"),LineOfBusiness.class);

		org.onap.aai.domain.yang.LineOfBusiness lobAAI = new org.onap.aai.domain.yang.LineOfBusiness();
		lobAAI.setLineOfBusinessName("lineOfBusinessName");

		LineOfBusiness actual = bbInputSetupMapperLayer.mapAAILineOfBusiness(lobAAI);

		assertThat(actual, sameBeanAs(expected));
	}
	
	@Test
	public void testMapAAINetworkPolicy() throws JsonParseException, JsonMappingException, IOException {
		NetworkPolicy expectedNetworkPolicy = mapper.readValue(new File(RESOURCE_PATH + "NetworkPolicy.json"), NetworkPolicy.class);
		
		org.onap.aai.domain.yang.NetworkPolicy aaiNetworkPolicy = new org.onap.aai.domain.yang.NetworkPolicy();
		aaiNetworkPolicy.setNetworkPolicyId("networkPolicyId");
		aaiNetworkPolicy.setNetworkPolicyFqdn("networkPolicyFqdn");
		aaiNetworkPolicy.setHeatStackId("heatStackId");
		aaiNetworkPolicy.setResourceVersion("resourceVersion");
		
		NetworkPolicy actualNetworkPolicy = bbInputSetupMapperLayer.mapAAINetworkPolicy(aaiNetworkPolicy);
		
		assertThat(actualNetworkPolicy, sameBeanAs(expectedNetworkPolicy));
	}
	
	@Test
	public void testMapAAIVolumeGroup() throws JsonParseException, JsonMappingException, IOException {
		VolumeGroup expectedVolumeGroup = mapper.readValue(new File(RESOURCE_PATH + "VolumeGroup.json"), VolumeGroup.class);
		
		org.onap.aai.domain.yang.VolumeGroup aaiVolumeGroup = mapper.readValue(
				new File(RESOURCE_PATH + "VolumeGroup_AAI.json"), org.onap.aai.domain.yang.VolumeGroup.class);
		
		VolumeGroup actualVolumeGroup = bbInputSetupMapperLayer.mapAAIVolumeGroup(aaiVolumeGroup);
		
		assertThat(actualVolumeGroup, sameBeanAs(expectedVolumeGroup));
	}

	@Test
	public void testMapCatalogServiceIntoServiceInstance() throws IOException {
		ModelInfoServiceInstance expected = mapper.readValue(
				new File(RESOURCE_PATH + "ModelInfoServiceInstance.json"),
				ModelInfoServiceInstance.class);

		Service catalogService = mapper.readValue(
				new File(RESOURCE_PATH + "CatalogServiceInput.json"), Service.class);

		ModelInfoServiceInstance actual = bbInputSetupMapperLayer.mapCatalogServiceIntoServiceInstance(catalogService);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testMapCatalogInstanceGroupToInstanceGroup() throws IOException {
		ModelInfoInstanceGroup expected = mapper.readValue(
				new File(RESOURCE_PATH + "ModelInfoInstanceGroup.json"),
				ModelInfoInstanceGroup.class);

		InstanceGroup instanceGroup = mapper.readValue(
				new File(RESOURCE_PATH + "InstanceGroup.json"), InstanceGroup.class);

		ModelInfoInstanceGroup actual = bbInputSetupMapperLayer.mapCatalogInstanceGroupToInstanceGroup(instanceGroup);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testMapCatalogCollectionToCollection() throws IOException {
		ModelInfoCollection expected = mapper.readValue(
				new File(RESOURCE_PATH + "ModelInfoCollection.json"),
				ModelInfoCollection.class);

		CollectionResourceCustomization collectionCust = mapper.readValue(
				new File(RESOURCE_PATH + "CollectionResourceCustomization.json"),
				CollectionResourceCustomization.class);

		CollectionResource collectionResource = mapper.readValue(
				new File(RESOURCE_PATH + "CollectionResource.json"),
				CollectionResource.class);

		ModelInfoCollection actual = bbInputSetupMapperLayer.mapCatalogCollectionToCollection(collectionCust, collectionResource);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testMapAAIServiceInstanceIntoServiceInstance() throws IOException {
		ServiceInstance expected = mapper.readValue(
				new File(RESOURCE_PATH + "ServiceInstance_aaiServiceInstanceToSI.json"),
				ServiceInstance.class);

		org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = mapper.readValue(
				new File(RESOURCE_PATH + "ServiceInstanceAAIInput.json"),
				org.onap.aai.domain.yang.ServiceInstance.class);

		ServiceInstance actual = bbInputSetupMapperLayer.mapAAIServiceInstanceIntoServiceInstance(serviceInstanceAAI);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testSetPlatformAndLOB() throws IOException {
		ServiceInstance expected = mapper.readValue(
				new File(RESOURCE_PATH + "ServiceInstance_aaiPlatformAndLOBToSI.json"),
				ServiceInstance.class);

		Map<ResourceKey, String> resourcesToBeOrchestrated = new HashMap<>();
		resourcesToBeOrchestrated.put(ResourceKey.GENERIC_VNF_ID, "vnfId");
		Platform platformMSO = new Platform();
		platformMSO.setPlatformName("platformName");
		LineOfBusiness lineOfBusinessMSO = new LineOfBusiness();
		lineOfBusinessMSO.setLineOfBusinessName("lineOfBusinessName");

		ServiceInstance actual = mapper.readValue(
				new File(RESOURCE_PATH + "ServiceInstanceAAIPlatformAndLOBInput.json"),
				ServiceInstance.class);

		bbInputSetupMapperLayer.setPlatformAndLOBIntoServiceInstance(platformMSO, lineOfBusinessMSO, actual,
				resourcesToBeOrchestrated);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testMapAAIL3NetworkIntoL3Network() throws IOException {
		L3Network expected = mapper.readValue(
				new File(RESOURCE_PATH + "l3NetworkExpected.json"), L3Network.class);

		org.onap.aai.domain.yang.L3Network aaiL3Network = mapper.readValue(
				new File(RESOURCE_PATH + "aaiL3NetworkInput.json"),
				org.onap.aai.domain.yang.L3Network.class);

		L3Network actual = bbInputSetupMapperLayer.mapAAIL3Network(aaiL3Network);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testMapAAIGenericVnfIntoGenericVnf() throws IOException {
		GenericVnf expected = mapper.readValue(new File(RESOURCE_PATH + "GenericVnfExpected.json"), GenericVnf.class);
		org.onap.aai.domain.yang.GenericVnf aaiGenericVnf = mapper.readValue(
				new File(RESOURCE_PATH + "aaiGenericVnfInput.json"), org.onap.aai.domain.yang.GenericVnf.class);
		
		GenericVnf actual = bbInputSetupMapperLayer.mapAAIGenericVnfIntoGenericVnf(aaiGenericVnf);

		assertThat(actual, sameBeanAs(expected));
	}
	
	@Test
	public void testMapAAICollectionIntoCollection() throws JsonParseException, JsonMappingException, IOException {
		org.onap.aai.domain.yang.Collection aaiCollection = mapper.readValue(
				new File(RESOURCE_PATH + "CollectionInput.json"), org.onap.aai.domain.yang.Collection.class);
		
		Collection expectedCollection = mapper.readValue(new File(RESOURCE_PATH + "CollectionExpected.json"), Collection.class);
		
		Collection actualCollection = bbInputSetupMapperLayer.mapAAICollectionIntoCollection(aaiCollection);
		
		assertThat(actualCollection, sameBeanAs(expectedCollection));
	}
	
	@Test
	public void testMapAAIInstanceGroupIntoInstanceGroup() throws JsonParseException, JsonMappingException, IOException {
		org.onap.aai.domain.yang.InstanceGroup aaiInstanceGroup = mapper.readValue(
				new File(RESOURCE_PATH + "InstanceGroupInput.json"), org.onap.aai.domain.yang.InstanceGroup.class);
		
		org.openecomp.mso.bpmn.servicedecomposition.bbobjects.InstanceGroup expectedInstanceGroup = mapper.readValue(
				new File(RESOURCE_PATH + "InstanceGroupExpected.json"), org.openecomp.mso.bpmn.servicedecomposition.bbobjects.InstanceGroup.class);
		
		org.openecomp.mso.bpmn.servicedecomposition.bbobjects.InstanceGroup actualInstanceGroup = 
				bbInputSetupMapperLayer.mapAAIInstanceGroupIntoInstanceGroup(aaiInstanceGroup);
		
		assertThat(actualInstanceGroup, sameBeanAs(expectedInstanceGroup));
	}
	
	@Test
	public void testMapAAIRouteTableReferenceIntoRouteTableReference() throws JsonParseException, JsonMappingException, IOException {
		org.onap.aai.domain.yang.RouteTableReference aaiRouteTableReference = mapper.readValue(
				new File(RESOURCE_PATH + "RouteTableReferenceInput.json"), org.onap.aai.domain.yang.RouteTableReference.class);
		
		RouteTableReference expectedRouteTableReference = mapper.readValue(
				new File(RESOURCE_PATH + "RouteTableReferenceExpected.json"), RouteTableReference.class);
		
		RouteTableReference actualRouteTableReference = bbInputSetupMapperLayer.mapAAIRouteTableReferenceIntoRouteTableReference(aaiRouteTableReference);
		
		assertThat(actualRouteTableReference, sameBeanAs(expectedRouteTableReference));
	}
	
	@Test
	public void testMapCatalogNetworkToNetwork() throws JsonParseException, JsonMappingException, IOException {
		NetworkResourceCustomization networkResourceCustomization = mapper.readValue(
				new File(RESOURCE_PATH + "NetworkResourceCustomizationInput.json"), NetworkResourceCustomization.class);
		
		ModelInfoNetwork expectedModelInfoNetwork = mapper.readValue(
				new File(RESOURCE_PATH + "ModelInfoNetworkExpected.json"), ModelInfoNetwork.class);
		
		ModelInfoNetwork actualModelInfoNetwork = bbInputSetupMapperLayer.mapCatalogNetworkToNetwork(networkResourceCustomization);
		
		assertThat(actualModelInfoNetwork, sameBeanAs(expectedModelInfoNetwork));
	}

	@Test
	public void testMapCatalogVnfToVnf() throws IOException {
		VnfResourceCustomization vnfResourceCustomization = mapper.readValue(
				new File(RESOURCE_PATH + "VnfResourceCustomizationInput.json"), VnfResourceCustomization.class);
		
		ModelInfoGenericVnf expectedModelInfoGenericVnf = mapper.readValue(
				new File(RESOURCE_PATH + "ModelInfoGenericVnfExpected.json"), ModelInfoGenericVnf.class);
		
		ModelInfoGenericVnf actualModelInfoGenericVnf = bbInputSetupMapperLayer.mapCatalogVnfToVnf(vnfResourceCustomization);
		
		assertThat(actualModelInfoGenericVnf, sameBeanAs(expectedModelInfoGenericVnf));
	}
	
	@Test
	public void testMapCatalogVfModuleToVfModule() throws JsonParseException, JsonMappingException, IOException {
		VfModuleCustomization vfResourceCustomization = mapper.readValue(
				new File(RESOURCE_PATH + "VfModuleCustomizationInput.json"), VfModuleCustomization.class);
		
		ModelInfoVfModule expectedModelInfoVfModule = mapper.readValue(new File(RESOURCE_PATH + "ModelInfoVfModuleExpected.json"), ModelInfoVfModule.class);
		
		ModelInfoVfModule actualModelInfoVfModule = bbInputSetupMapperLayer.mapCatalogVfModuleToVfModule(vfResourceCustomization);
		
		assertThat(actualModelInfoVfModule, sameBeanAs(expectedModelInfoVfModule));
	}
	
	@Test
	public void testMapRequestPlatform() throws JsonParseException, JsonMappingException, IOException {
		org.openecomp.mso.serviceinstancebeans.Platform platform = mapper.readValue(
				new File(RESOURCE_PATH + "RequestPlatformInput.json"), org.openecomp.mso.serviceinstancebeans.Platform.class);
		
		Platform expectedPlatform = mapper.readValue(new File(RESOURCE_PATH + "PlatformExpected.json"), Platform.class);
		
		Platform actualPlatform = bbInputSetupMapperLayer.mapRequestPlatform(platform);
		
		assertThat(actualPlatform, sameBeanAs(expectedPlatform));
	}
	
	@Test
	public void testMapRequestLineOfBusiness() throws JsonParseException, JsonMappingException, IOException {
		org.openecomp.mso.serviceinstancebeans.LineOfBusiness lineOfBusiness = mapper.readValue(
				new File(RESOURCE_PATH + "RequestLineOfBusinessInput.json"), org.openecomp.mso.serviceinstancebeans.LineOfBusiness.class);
		
		LineOfBusiness expectedLineOfBusiness = mapper.readValue(new File(RESOURCE_PATH + "LineOfBusinessExpected.json"), LineOfBusiness.class);
		
		LineOfBusiness actualLineOfBusiness = bbInputSetupMapperLayer.mapRequestLineOfBusiness(lineOfBusiness);
		
		assertThat(actualLineOfBusiness, sameBeanAs(expectedLineOfBusiness));
	}
	
	@Test
	public void testMapAAIConfiguration() throws JsonParseException, JsonMappingException, IOException {
		org.onap.aai.domain.yang.Configuration configurationAAI = mapper.readValue(
				new File(RESOURCE_PATH + "ConfigurationInput.json"), org.onap.aai.domain.yang.Configuration.class);
		
		Configuration expectedConfiguration = mapper.readValue(
				new File(RESOURCE_PATH + "ConfigurationExpected.json"), Configuration.class);
		
		Configuration actualConfiguration = bbInputSetupMapperLayer.mapAAIConfiguration(configurationAAI);
		
		assertThat(actualConfiguration, sameBeanAs(expectedConfiguration));
	}

	@Test
	public void testMapRequestContext() throws IOException {
		RequestContext expected = mapper.readValue(
				new File(RESOURCE_PATH + "RequestContextExpected.json"),
				RequestContext.class);

		RequestDetails requestDetails = mapper.readValue(
				new File(RESOURCE_PATH + "RequestDetailsInput_mapReqContext.json"),
				RequestDetails.class);
		RequestContext actual = bbInputSetupMapperLayer.mapRequestContext(requestDetails);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testMapOrchestrationContext() throws IOException {
		OrchestrationContext expected = new OrchestrationContext();
		expected.setIsRollbackEnabled(false);

		RequestDetails requestDetails = mapper.readValue(new File(RESOURCE_PATH + "RequestDetailsInput_mapReqContext.json"), RequestDetails.class);

		OrchestrationContext actual = bbInputSetupMapperLayer.mapOrchestrationContext(requestDetails);

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testMapLocationContext() {
		CloudRegion expected = new CloudRegion();
		expected.setCloudOwner("att-aic");
		expected.setLcpCloudRegionId("cloudRegionId");
		expected.setComplex("complexName");
		expected.setTenantId("tenantId");
		RequestDetails requestDetails = new RequestDetails();
		CloudConfiguration cloudConfig = new CloudConfiguration();
		cloudConfig.setTenantId("tenantId");
		cloudConfig.setLcpCloudRegionId("cloudRegionId");
		cloudConfig.setAicNodeClli("aicNodeClli");
		requestDetails.setCloudConfiguration(cloudConfig);
		org.onap.aai.domain.yang.CloudRegion cloudRegion = new org.onap.aai.domain.yang.CloudRegion();
		cloudRegion.setCloudOwner("att-aic");
		cloudRegion.setCloudRegionId("cloudRegionId");
		cloudRegion.setComplexName("complexName");

		CloudRegion actual = bbInputSetupMapperLayer.mapCloudRegion(requestDetails, cloudRegion, "att-aic");

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testMapCloudRegion() {
		CloudRegion expected = new CloudRegion();
		expected.setCloudOwner("att-aic");
		expected.setLcpCloudRegionId("cloudRegionId");
		expected.setTenantId("tenantId");
		expected.setCloudRegionVersion("cloudRegionVersion");

		RequestDetails requestDetails = new RequestDetails();
		CloudConfiguration cloudConfig = new CloudConfiguration();
		cloudConfig.setTenantId("tenantId");
		cloudConfig.setLcpCloudRegionId("cloudRegionId");
		cloudConfig.setAicNodeClli("aicNodeClli");
		requestDetails.setCloudConfiguration(cloudConfig);

		org.onap.aai.domain.yang.CloudRegion cloudRegion = new org.onap.aai.domain.yang.CloudRegion();
		cloudRegion.setCloudOwner("att-aic");
		cloudRegion.setCloudRegionId("cloudRegionId");
		cloudRegion.setCloudRegionVersion("cloudRegionVersion");

		CloudRegion actual = bbInputSetupMapperLayer.mapCloudRegion(requestDetails, cloudRegion, "att-aic");

		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	public void testMapCloudRegionWithNullCheck() {
		CloudRegion expected = new CloudRegion();

		RequestDetails requestDetails = new RequestDetails();

		CloudRegion actual = bbInputSetupMapperLayer.mapCloudRegion(requestDetails, null, null);

		assertThat(actual, sameBeanAs(expected));
	}
}
