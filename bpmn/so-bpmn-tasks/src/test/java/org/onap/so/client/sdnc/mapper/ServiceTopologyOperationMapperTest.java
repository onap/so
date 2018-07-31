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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.junit.Test;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;

import org.onap.sdnc.apps.client.model.GenericResourceApiOnapmodelinformationOnapModelInformation;
import org.onap.sdnc.apps.client.model.GenericResourceApiRequestActionEnumeration;
import org.onap.sdnc.apps.client.model.GenericResourceApiServiceOperationInformation;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceTopologyOperationMapperTest {

	@Test
	public void reqMapperTest() throws Exception {
		// prepare and set service instance
		ServiceInstance serviceInstance = new ServiceInstance();
		ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
		modelInfoServiceInstance.setModelInvariantUuid("modelInvariantUuid");
		modelInfoServiceInstance.setModelName("modelName");
		modelInfoServiceInstance.setModelUuid("modelUuid");
		modelInfoServiceInstance.setModelVersion("modelVersion");
		serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);

		// prepare Customer object
		Customer customer = new Customer();
		customer.setGlobalCustomerId("globalCustomerId");

		customer.setServiceSubscription(new ServiceSubscription());
		// set Customer on service instance
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);

		//prepare RequestContext
		RequestContext requestContext = new RequestContext();
		HashMap<String, String> userParams = new HashMap<String, String>();
		userParams.put("key1", "value1");
		requestContext.setUserParams(userParams);
		requestContext.setProductFamilyId("productFamilyId");

		ServiceTopologyOperationMapper mapper = new ServiceTopologyOperationMapper();
		GenericResourceApiServiceOperationInformation serviceOpInformation = mapper.reqMapper(
				SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION, SDNCSvcAction.ASSIGN, GenericResourceApiRequestActionEnumeration.CREATESERVICEINSTANCE, serviceInstance, customer,
				requestContext);

		String jsonToCompare = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/BuildingBlocks/genericResourceApiEcompModelInformation.json")));

		ObjectMapper omapper = new ObjectMapper();
		GenericResourceApiOnapmodelinformationOnapModelInformation reqMapper1 = omapper.readValue(jsonToCompare,
				GenericResourceApiOnapmodelinformationOnapModelInformation.class);

		assertThat(reqMapper1, sameBeanAs(serviceOpInformation.getServiceInformation().getOnapModelInformation()));
	}
}
