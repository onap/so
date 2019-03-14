/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 TechMahindra.
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

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;

public class ConfigAssignVnfTest extends BaseTaskTest {
	@InjectMocks
	private ConfigAssignVnf configAssignVnf = new ConfigAssignVnf();

	private GenericVnf genericVnf;
	private ServiceInstance serviceInstance;
	private RequestContext requestContext;
	private String msoRequestId;

	@Before
	public void before() throws BBObjectNotFoundException {
		genericVnf = setGenericVnf();
		serviceInstance = setServiceInstance();
		msoRequestId = UUID.randomUUID().toString();
		requestContext = setRequestContext();
		requestContext.setMsoRequestId(msoRequestId);
		gBBInput.setRequestContext(requestContext);

		doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
		when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID), any())).thenReturn(genericVnf);
		when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID), any())).thenReturn(serviceInstance);
	}

	@Test
	public void preProcessRequestTest() throws Exception {
		VnfResourceCustomization vnfResourceCustomization = new VnfResourceCustomization();
		vnfResourceCustomization.setBlueprintName("blueprintName");
		vnfResourceCustomization.setBlueprintVersion("blueprintVersion");

		doReturn(vnfResourceCustomization).when(catalogDbClient).getVnfResourceCustomizationByModelCustomizationUUID(genericVnf.getModelInfoGenericVnf().getModelCustomizationUuid());

		configAssignVnf.preProcessRequest(execution);

		assertTrue(true);
	}
	
	@Test
	public void preProcessAbstractCDSProcessingTest() throws Exception {
		execution.setVariable("originatorId", "SO");
		execution.setVariable("requestId", gBBInput.getRequestContext().getMsoRequestId());
		execution.setVariable("subRequestId", "123456");
		execution.setVariable("actionName", "config-assign");
		execution.setVariable("mode", "sync");
		
		configAssignVnf.preProcessAbstractCDSProcessing(execution);
		
		assertTrue(true);
	}
	
}
