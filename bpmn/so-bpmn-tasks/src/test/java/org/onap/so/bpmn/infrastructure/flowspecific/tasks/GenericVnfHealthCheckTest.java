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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.onap.appc.client.lcm.model.Action;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;

public class GenericVnfHealthCheckTest extends BaseTaskTest {
	
	@InjectMocks
	private GenericVnfHealthCheck genericVnfHealthCheck = new GenericVnfHealthCheck();
	
	private GenericVnf genericVnf;
	private RequestContext requestContext;
	private String msoRequestId;

	@Before
	public void before() throws BBObjectNotFoundException {
		genericVnf = setGenericVnf();
		msoRequestId = UUID.randomUUID().toString();
		requestContext = setRequestContext();
		requestContext.setMsoRequestId(msoRequestId);
		gBBInput.setRequestContext(requestContext);
		
		doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));	
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(genericVnf);
	}
	
	@Test
	public void setParamsForGenericVnfHealthCheckTest() throws Exception {
		ControllerSelectionReference controllerSelectionReference = new ControllerSelectionReference();
		controllerSelectionReference.setControllerName("testName");
		controllerSelectionReference.setActionCategory("testAction");
		controllerSelectionReference.setVnfType("testVnfType");
		
		doReturn(controllerSelectionReference).when(catalogDbClient).getControllerSelectionReferenceByVnfTypeAndActionCategory(genericVnf.getVnfType(), Action.HealthCheck.toString());
		
		genericVnfHealthCheck.setParamsForGenericVnfHealthCheck(execution);
		
		assertEquals(genericVnf.getVnfId(), execution.getVariable("vnfId"));
		assertEquals(genericVnf.getVnfName(), execution.getVariable("vnfName"));
		assertEquals(genericVnf.getIpv4OamAddress(), execution.getVariable("oamIpAddress"));
		assertEquals("HealthCheck", execution.getVariable("action"));
		assertEquals(requestContext.getMsoRequestId(), execution.getVariable("msoRequestId"));
		assertEquals(controllerSelectionReference.getControllerName(), execution.getVariable("controllerType"));
	}
	@Test
	public void callAppcClientTest() throws Exception {
		Action action = Action.HealthCheck;
		String vnfId = genericVnf.getVnfId();
		String payload = "{\"testName\":\"testValue\",}";
		String controllerType = "testType";
		HashMap<String, String> payloadInfo = new HashMap<String, String>();
		payloadInfo.put("vnfName", "testVnfName");
		payloadInfo.put("vfModuleId", "testVfModuleId");
		payloadInfo.put("oamIpAddress", "testOamIpAddress");
		payloadInfo.put("vnfHostIpAddress", "testOamIpAddress");
		execution.setVariable("action", Action.HealthCheck.toString());
		execution.setVariable("msoRequestId", msoRequestId);
		execution.setVariable("controllerType", controllerType);
		execution.setVariable("vnfId", "testVnfId1");
		execution.setVariable("vnfName", "testVnfName");
		execution.setVariable("vfModuleId", "testVfModuleId");
		execution.setVariable("oamIpAddress", "testOamIpAddress");
		execution.setVariable("vnfHostIpAddress", "testOamIpAddress");
		execution.setVariable("payload", payload);
		
		doNothing().when(appCClient).runAppCCommand(action, msoRequestId, vnfId, Optional.of(payload), payloadInfo, controllerType);
		
		genericVnfHealthCheck.callAppcClient(execution);
		verify(appCClient, times(1)).runAppCCommand(action, msoRequestId, vnfId, Optional.of(payload), payloadInfo, controllerType);
	}
	
	@Test
	public void callAppcClientExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		Action action = Action.HealthCheck;
		String vnfId = genericVnf.getVnfId();
		String payload = "{\"testName\":\"testValue\",}";
		String controllerType = "testType";
		HashMap<String, String> payloadInfo = new HashMap<String, String>();
		payloadInfo.put("vnfName", "testVnfName");
		payloadInfo.put("vfModuleId", "testVfModuleId");
		payloadInfo.put("oamIpAddress", "testOamIpAddress");
		payloadInfo.put("vnfHostIpAddress", "testOamIpAddress");
		execution.setVariable("action", Action.HealthCheck.toString());
		execution.setVariable("msoRequestId", msoRequestId);
		execution.setVariable("controllerType", controllerType);
		execution.setVariable("vnfId", "testVnfId1");
		execution.setVariable("vnfName", "testVnfName");
		execution.setVariable("vfModuleId", "testVfModuleId");
		execution.setVariable("oamIpAddress", "testOamIpAddress");
		execution.setVariable("vnfHostIpAddress", "testOamIpAddress");
		execution.setVariable("payload", payload);
		
		doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(1002), eq("APPC Client Failed"));	
		doThrow(new RuntimeException("APPC Client Failed")).when(appCClient).runAppCCommand(action, msoRequestId, vnfId, Optional.of(payload), payloadInfo, controllerType);
		
		
		genericVnfHealthCheck.callAppcClient(execution);
		verify(appCClient, times(1)).runAppCCommand(action, msoRequestId, vnfId, Optional.of(payload), payloadInfo, controllerType);
	}
	
	@Test
	@Ignore //The runAppCCommand method in not capable of throwing this exception
	public void callAppcClientTimeOutExceptionTest() {
		expectedException.expect(java.util.concurrent.TimeoutException.class);
		Action action = Action.HealthCheck;
		String vnfId = genericVnf.getVnfId();
		String payload = "{\"testName\":\"testValue\",}";
		String controllerType = "testType";
		HashMap<String, String> payloadInfo = new HashMap<String, String>();
		payloadInfo.put("vnfName", "testVnfName");
		payloadInfo.put("vfModuleId", "testVfModuleId");
		payloadInfo.put("oamIpAddress", "testOamIpAddress");
		payloadInfo.put("vnfHostIpAddress", "testOamIpAddress");
		execution.setVariable("action", Action.HealthCheck.toString());
		execution.setVariable("msoRequestId", msoRequestId);
		execution.setVariable("controllerType", controllerType);
		execution.setVariable("vnfId", "testVnfId1");
		execution.setVariable("vnfName", "testVnfName");
		execution.setVariable("vfModuleId", "testVfModuleId");
		execution.setVariable("oamIpAddress", "testOamIpAddress");
		execution.setVariable("vnfHostIpAddress", "testOamIpAddress");
		execution.setVariable("payload", payload);
		
		doThrow(java.util.concurrent.TimeoutException.class).when(appCClient).runAppCCommand(action, msoRequestId, vnfId, Optional.of(payload), payloadInfo, controllerType);
		
		
		genericVnfHealthCheck.callAppcClient(execution);
		verify(appCClient, times(1)).runAppCCommand(action, msoRequestId, vnfId, Optional.of(payload), payloadInfo, controllerType);
	}
}
