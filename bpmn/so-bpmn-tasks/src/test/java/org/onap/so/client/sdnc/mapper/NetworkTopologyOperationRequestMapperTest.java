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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoNetwork;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;

import org.onap.sdnc.apps.client.model.GenericResourceApiNetworkOperationInformation;
import org.onap.sdnc.apps.client.model.GenericResourceApiRequestActionEnumeration;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NetworkTopologyOperationRequestMapperTest {

	private final static String JSON_FILE_LOCATION = "src/test/resources/__files/BuildingBlocks/";

	private ServiceInstance serviceInstance;
	private ServiceInstance serviceInstanceNoCollection;
	private Customer customer;
	private RequestContext requestContext;
	private L3Network network;
	private CloudRegion cloudRegion;

	@Before
	public void before() {
		// prepare and set service instance
		serviceInstance = new ServiceInstance();
		serviceInstanceNoCollection = new ServiceInstance();
		ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
		modelInfoServiceInstance.setModelInvariantUuid("modelInvariantUuid");
		modelInfoServiceInstance.setModelName("modelName");
		modelInfoServiceInstance.setModelUuid("modelUuid");
		modelInfoServiceInstance.setModelVersion("modelVersion");
		serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);
		serviceInstanceNoCollection.setModelInfoServiceInstance(modelInfoServiceInstance);
		// prepare Customer object
		customer = new Customer();
		customer.setGlobalCustomerId("globalCustomerId");
		//serviceInstance.setCustomer(customer);
		// set Customer on service instance
		ServiceSubscription serviceSubscription = new ServiceSubscription();
		serviceSubscription.setServiceType("productFamilyId");
		customer.setServiceSubscription(serviceSubscription);

		// set Customer on service instance
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		customer.getServiceSubscription().getServiceInstances().add(serviceInstanceNoCollection);
		//
		InstanceGroup networkInstanceGroup = new InstanceGroup();
		networkInstanceGroup.setId("networkInstanceGroupId");
		networkInstanceGroup.setInstanceGroupFunction("instanceGroupFunction");
		Collection networkCollection = new Collection();
		networkCollection.setInstanceGroup(networkInstanceGroup);
		serviceInstance.setCollection(networkCollection);
		//
		requestContext = new RequestContext();
		HashMap<String, String> userParams = new HashMap<String, String>();
		userParams.put("key1", "value1");
		requestContext.setUserParams(userParams);
		requestContext.setProductFamilyId("productFamilyId");

		network = new L3Network();
		network.setNetworkId("TEST_NETWORK_ID");
		network.setNetworkName("TEST_NETWORK_NAME");
		ModelInfoNetwork modelInfoNetwork = new ModelInfoNetwork();
		modelInfoNetwork.setModelInvariantUUID("modelInvariantUuid");
		modelInfoNetwork.setModelName("modelName");
		modelInfoNetwork.setModelVersion("modelVersion");
		modelInfoNetwork.setModelUUID("modelUuid");
		modelInfoNetwork.setModelCustomizationUUID("modelCustomizationUUID");
		network.setModelInfoNetwork(modelInfoNetwork);

		cloudRegion = new CloudRegion();
	}

	@Test
	public void createGenericResourceApiNetworkOperationInformationTest() throws Exception {

		NetworkTopologyOperationRequestMapper mapper = new NetworkTopologyOperationRequestMapper();
		GenericResourceApiNetworkOperationInformation networkSDNCrequest = mapper.reqMapper(
				SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.ASSIGN, GenericResourceApiRequestActionEnumeration.CREATENETWORKINSTANCE, network, serviceInstance, customer,
				requestContext, cloudRegion);

		ObjectMapper omapper = new ObjectMapper();
		GenericResourceApiNetworkOperationInformation reqMapper1 = omapper.readValue(
				getJson("genericResourceApiNetworkOperationInformation.json"),
				GenericResourceApiNetworkOperationInformation.class);

		assertThat(networkSDNCrequest, sameBeanAs(reqMapper1).ignoring("sdncRequestHeader.svcRequestId")
				.ignoring("requestInformation.requestId"));
	}

	@Test
	public void reqMapperTest() throws Exception {

		NetworkTopologyOperationRequestMapper mapper = new NetworkTopologyOperationRequestMapper();
		GenericResourceApiNetworkOperationInformation networkSDNCrequest = mapper.reqMapper(
				SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.ASSIGN, GenericResourceApiRequestActionEnumeration.CREATENETWORKINSTANCE, network, serviceInstance, customer,
				requestContext, cloudRegion);

		assertNull(networkSDNCrequest.getServiceInformation().getOnapModelInformation().getModelCustomizationUuid());
		assertEquals("modelCustomizationUUID", networkSDNCrequest.getNetworkInformation().getOnapModelInformation().getModelCustomizationUuid());
	}

	@Test
	public void reqMapperNoCollectionTest() throws Exception {
		NetworkTopologyOperationRequestMapper mapper = new NetworkTopologyOperationRequestMapper();
		GenericResourceApiNetworkOperationInformation networkSDNCrequest = mapper.reqMapper(
				SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.ASSIGN, GenericResourceApiRequestActionEnumeration.CREATENETWORKINSTANCE, network, serviceInstanceNoCollection, customer,
				requestContext, cloudRegion);

		assertNull(networkSDNCrequest.getServiceInformation().getOnapModelInformation().getModelCustomizationUuid());
		assertEquals("modelCustomizationUUID", networkSDNCrequest.getNetworkInformation().getOnapModelInformation().getModelCustomizationUuid());
	}
	@Test
	public void createGenericResourceApiNetworkOperationInformation_UnassignTest() throws Exception {

		NetworkTopologyOperationRequestMapper mapperUnassign = new NetworkTopologyOperationRequestMapper();
		GenericResourceApiNetworkOperationInformation networkSDNCrequestUnassign = mapperUnassign.reqMapper(
				SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.UNASSIGN, GenericResourceApiRequestActionEnumeration.DELETENETWORKINSTANCE, network, serviceInstance, customer,
				requestContext, cloudRegion);

		ObjectMapper omapperUnassign = new ObjectMapper();
		GenericResourceApiNetworkOperationInformation reqMapperUnassign = omapperUnassign.readValue(
				getJson("genericResourceApiNetworkOperationInformationUnAssign.json"),
				GenericResourceApiNetworkOperationInformation.class);

		assertThat(reqMapperUnassign, sameBeanAs(networkSDNCrequestUnassign).ignoring("sdncRequestHeader.svcRequestId")
				.ignoring("requestInformation.requestId"));

 	}
	
	@Test
	public void createGenericResourceApiNetworkOperationInformationNoNetworkNameTest() throws Exception {

		NetworkTopologyOperationRequestMapper mapper = new NetworkTopologyOperationRequestMapper();
		//set network name NULL
		network.setNetworkName(null);
		GenericResourceApiNetworkOperationInformation networkSDNCrequest = mapper.reqMapper(
				SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.ASSIGN, GenericResourceApiRequestActionEnumeration.CREATENETWORKINSTANCE, network, serviceInstance, customer,
				requestContext, cloudRegion);

		ObjectMapper omapper = new ObjectMapper();
		GenericResourceApiNetworkOperationInformation reqMapper1 = omapper.readValue(
				getJson("genericResourceApiNetworkOperationInformationNoNetworkName.json"),
				GenericResourceApiNetworkOperationInformation.class);

		assertThat(reqMapper1, sameBeanAs(networkSDNCrequest).ignoring("sdncRequestHeader.svcRequestId")
				.ignoring("requestInformation.requestId"));
	}

	/*
	 * Helper method to load JSON data
	 */
	private String getJson(String filename) throws IOException {
		return new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + filename)));
	}

}
