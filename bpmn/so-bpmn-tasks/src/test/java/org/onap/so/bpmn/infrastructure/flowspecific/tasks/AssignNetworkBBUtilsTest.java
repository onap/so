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
package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;

public class AssignNetworkBBUtilsTest extends BaseTaskTest {
	//Necessary because AssignNetworkBBUtils is already mocked using MockBean in the base class for use in other tests
	private AssignNetworkBBUtils nonMockAssignNetworkBBUtils = new AssignNetworkBBUtils();
	
	private CloudRegion cloudRegion;
	
	@Before
	public void before() {
		cloudRegion = setCloudRegion();
	}
	
	@Test
	public void getCloudRegionTest25() throws Exception {
		cloudRegion.setCloudRegionVersion("2.5");
		
		nonMockAssignNetworkBBUtils.getCloudRegion(execution);

		assertEquals(cloudRegion.getLcpCloudRegionId(), execution.getVariable("cloudRegionPo"));
		assertEquals("AAIAIC25", execution.getVariable("cloudRegionSdnc"));
	}
	
	@Test
	public void getCloudRegionTest30() throws Exception {
		cloudRegion.setCloudRegionVersion("3.0");
		
		nonMockAssignNetworkBBUtils.getCloudRegion(execution);

		assertEquals(cloudRegion.getLcpCloudRegionId(), execution.getVariable("cloudRegionPo"));
		assertEquals(cloudRegion.getLcpCloudRegionId(), execution.getVariable("cloudRegionSdnc"));
	}
}
