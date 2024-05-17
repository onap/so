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

package org.onap.so.bpmn.common.scripts

import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.NetworkResource
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.json.DecomposeJsonUtil
import org.onap.so.bpmn.core.domain.ServiceInstance

import org.onap.so.bpmn.mock.FileUtil
import org.junit.Test;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;

class DecomposeServiceTest {
	
	@Test
	public void testDecomposeService() {
		
		String catalogDbResponse = FileUtil.readResourceFile("__files/decomposition/catalogDbResponse.json");
		
		ServiceDecomposition serviceDecomposition = new ServiceDecomposition();
		ServiceInstance serviceInstance = new ServiceInstance();
		serviceInstance.setInstanceId("serviceInstanceID");
		serviceDecomposition.setServiceType("");
		serviceDecomposition.setServiceRole("");
		
		ArrayList networkResources = new ArrayList();
		NetworkResource networkResource = new NetworkResource();
		networkResource.setNetworkType("testNetworkType");
		networkResource.setNetworkRole("testNetworkRole");
		networkResource.setNetworkScope("testNetworkScope");
		networkResource.setToscaNodeType("testToscaModelType")
		networkResource.setNetworkTechnology("testNetworkTechnology");
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelName("testModleName");
		modelInfo.setModelUuid("testModelUuid")
		modelInfo.setModelInvariantUuid("testModelInvariantId")
		modelInfo.setModelVersion("testModelVersion");
		modelInfo.setModelCustomizationUuid("testModelCustomizationUuid");
		modelInfo.setModelInstanceName("testModelInstanceName");
		networkResource.setModelInfo(modelInfo);
		
		networkResources.add(networkResource);
		serviceDecomposition.setNetworkResources(networkResources)
		serviceDecomposition.setServiceInstance(serviceInstance);
		
		ServiceDecomposition serviceDecompositionExtracted = DecomposeJsonUtil.jsonToServiceDecomposition(catalogDbResponse, "serviceInstanceID")
		
		assertThat(serviceDecompositionExtracted, sameBeanAs(serviceDecomposition).ignoring("modelInfo").ignoring("vnfResources").ignoring("allottedResources").ignoring("networkResources.resourceId"));
	}
}
