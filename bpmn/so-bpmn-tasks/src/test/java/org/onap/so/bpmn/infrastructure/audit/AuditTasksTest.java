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
package org.onap.so.bpmn.infrastructure.audit;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.onap.so.audit.beans.AuditInventory;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.client.exception.BBObjectNotFoundException;

public class AuditTasksTest extends BaseTaskTest{
	
	@InjectMocks
	private AuditTasks auditTasks = new AuditTasks();
	private ServiceInstance serviceInstance;
	private GenericVnf genericVnf;
	private VfModule vfModule;

	
	@Rule
	public final ExpectedException exception = ExpectedException.none();
	
	@Before
	public void before() throws BBObjectNotFoundException {
		serviceInstance = setServiceInstance();		
		genericVnf = setGenericVnf();
		vfModule = setVfModule();
		setCloudRegion();
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID), any())).thenReturn(genericVnf);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID), any())).thenReturn(vfModule);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID), any())).thenReturn(serviceInstance);
	}
	
	@Test
	public void setupAuditVariableTest() throws Exception {
		AuditInventory expectedAuditInventory = new AuditInventory();
		expectedAuditInventory.setCloudOwner("testCloudOwner");
		expectedAuditInventory.setCloudRegion("testLcpCloudRegionId");
		expectedAuditInventory.setHeatStackName("testVfModuleName1");
		expectedAuditInventory.setTenantId("testTenantId");
		auditTasks.setupAuditVariable(execution);
		assertThat((AuditInventory)execution.getVariable("auditInventory"), sameBeanAs(expectedAuditInventory));
	}
}
