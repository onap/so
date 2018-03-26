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

package org.openecomp.mso.bpmn.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openecomp.mso.bpmn.mock.StubResponseVNFAdapter.mockVNFDelete;
import static org.openecomp.mso.bpmn.mock.StubResponseVNFAdapter.mockVNFPost;
import static org.openecomp.mso.bpmn.mock.StubResponseVNFAdapter.mockVNFPut;
import static org.openecomp.mso.bpmn.mock.StubResponseVNFAdapter.mockVNFRollbackDelete;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.bpmn.core.WorkflowException;

/**
 * Unit tests for VnfAdapterRestV1.
 */
public class VnfAdapterRestV1Test extends WorkflowTest {

	private static final String EOL = "\n";

	private final CallbackSet callbacks = new CallbackSet();

	private final String CREATE_VF_MODULE_REQUEST =
		"<createVfModuleRequest>" + EOL +
		"  <cloudSiteId>cloudSiteId</cloudSiteId>" + EOL +
		"  <tenantId>tenantId</tenantId>" + EOL +
		"  <vnfId>vnfId</vnfId>" + EOL +
		"  <vfModuleName>vfModuleName</vfModuleName>" + EOL +
		"  <vfModuleId>vfModuleId</vfModuleId>" + EOL +
		"  <vnfType>vnfType</vnfType>" + EOL +
		"  <vnfVersion>vnfVersion</vnfVersion>" + EOL +
		"  <vfModuleType>vfModuleType</vfModuleType>" + EOL +
		"  <volumeGroupId>volumeGroupId</volumeGroupId>" + EOL +
		"  <volumeGroupStackId>volumeGroupStackId</volumeGroupStackId>" + EOL +
		"  <baseVfModuleId>baseVfModuleId</baseVfModuleId>" + EOL +
		"  <baseVfModuleStackId>baseVfModuleStackId</baseVfModuleStackId>" + EOL +
		"  <skipAAI>true</skipAAI>" + EOL +
		"  <backout>false</backout>" + EOL +
		"  <failIfExists>true</failIfExists>" + EOL +
		"  <vfModuleParams>" + EOL +
		"    <entry>" + EOL +
		"      <key>key1</key>" + EOL +
		"      <value>value1</value>" + EOL +
		"    </entry>" + EOL +
		"    <entry>" + EOL +
		"      <key>key2</key>" + EOL +
		"      <value>value2</value>" + EOL +
		"    </entry>" + EOL +
		"  </vfModuleParams>" + EOL +
		"  <msoRequest>" + EOL +
		"    <requestId>requestId</requestId>" + EOL +
		"    <serviceInstanceId>serviceInstanceId</serviceInstanceId>" + EOL +
		"  </msoRequest>" + EOL +
		"  <messageId>{{MESSAGE-ID}}</messageId>" + EOL +
		"  <notificationUrl>http://localhost:28080/mso/WorkflowMessage</notificationUrl>" + EOL +
		"</createVfModuleRequest>" + EOL;

	private final String UPDATE_VF_MODULE_REQUEST =
		"<updateVfModuleRequest>" + EOL +
		"  <cloudSiteId>cloudSiteId</cloudSiteId>" + EOL +
		"  <tenantId>tenantId</tenantId>" + EOL +
		"  <vnfId>vnfId</vnfId>" + EOL +
		"  <vfModuleName>vfModuleName</vfModuleName>" + EOL +
		"  <vfModuleId>vfModuleId</vfModuleId>" + EOL +
		"  <vfModuleStackId>vfModuleStackId</vfModuleStackId>" + EOL +
		"  <vnfType>vnfType</vnfType>" + EOL +
		"  <vnfVersion>vnfVersion</vnfVersion>" + EOL +
		"  <vfModuleType>vfModuleType</vfModuleType>" + EOL +
		"  <volumeGroupId>volumeGroupId</volumeGroupId>" + EOL +
		"  <volumeGroupStackId>volumeGroupStackId</volumeGroupStackId>" + EOL +
		"  <baseVfModuleId>baseVfModuleId</baseVfModuleId>" + EOL +
		"  <baseVfModuleStackId>baseVfModuleStackId</baseVfModuleStackId>" + EOL +
		"  <skipAAI>true</skipAAI>" + EOL +
		"  <backout>false</backout>" + EOL +
		"  <failIfExists>true</failIfExists>" + EOL +
		"  <vfModuleParams>" + EOL +
		"    <entry>" + EOL +
		"      <key>key1</key>" + EOL +
		"      <value>value1</value>" + EOL +
		"    </entry>" + EOL +
		"    <entry>" + EOL +
		"      <key>key2</key>" + EOL +
		"      <value>value2</value>" + EOL +
		"    </entry>" + EOL +
		"  </vfModuleParams>" + EOL +
		"  <msoRequest>" + EOL +
		"    <requestId>requestId</requestId>" + EOL +
		"    <serviceInstanceId>serviceInstanceId</serviceInstanceId>" + EOL +
		"  </msoRequest>" + EOL +
		"  <messageId>{{MESSAGE-ID}}</messageId>" + EOL +
		"  <notificationUrl>http://localhost:28080/mso/WorkflowMessage</notificationUrl>" + EOL +
		"</updateVfModuleRequest>" + EOL;

	private final String DELETE_VF_MODULE_REQUEST =
		"<deleteVfModuleRequest>" + EOL +
		"  <cloudSiteId>cloudSiteId</cloudSiteId>" + EOL +
		"  <tenantId>tenantId</tenantId>" + EOL +
		"  <vnfId>vnfId</vnfId>" + EOL +
		"  <vfModuleId>vfModuleId</vfModuleId>" + EOL +
		"  <vfModuleStackId>vfModuleStackId</vfModuleStackId>" + EOL +
		"  <skipAAI>true</skipAAI>" + EOL +
		"  <msoRequest>" + EOL +
		"    <requestId>requestId</requestId>" + EOL +
		"    <serviceInstanceId>serviceInstanceId</serviceInstanceId>" + EOL +
		"  </msoRequest>" + EOL +
		"  <messageId>{{MESSAGE-ID}}</messageId>" + EOL +
		"  <notificationUrl>http://localhost:28080/mso/WorkflowMessage</notificationUrl>" + EOL +
		"</deleteVfModuleRequest>" + EOL;

	private final String ROLLBACK_VF_MODULE_REQUEST =
			"<rollbackVfModuleRequest>" + EOL +
			"  <messageId>{{MESSAGE-ID}}</messageId>" + EOL +
			"  <notificationUrl>http://localhost:28080/mso/WorkflowMessage</notificationUrl>" + EOL +
			"  <skipAAI>true</skipAAI>" + EOL +
			"  <vfModuleRollback>" + EOL +
			"    <cloudSiteId>cloudSiteId</cloudSiteId>" + EOL +
			"    <tenantId>tenantId</tenantId>" + EOL +
			"    <vnfId>vnfId</vnfId>" + EOL +
			"    <vfModuleId>vfModuleId</vfModuleId>" + EOL +
			"    <vfModuleStackId>vfModuleStackId</vfModuleStackId>" + EOL +
			"    <msoRequest>" + EOL +
			"      <requestId>requestId</requestId>" + EOL +
			"      <serviceInstanceId>serviceInstanceId</serviceInstanceId>" + EOL +
			"    </msoRequest>" + EOL +
			"    <messageId>{{MESSAGE-ID}}</messageId>" + EOL +
			"    <vfModuleCreated>true</vfModuleCreated>" + EOL +
			"  </vfModuleRollback>" + EOL +
			"</rollbackVfModuleRequest>" + EOL;

	public VnfAdapterRestV1Test() throws IOException {
		callbacks.put("createVfModule",
			"<createVfModuleResponse>" + EOL +
			"  <vnfId>vnfId</vnfId>" + EOL +
			"  <vfModuleId>vfModuleId</vfModuleId>" + EOL +
			"  <vfModuleStackId>vfModuleStackId</vfModuleStackId>" + EOL +
			"  <vfModuleCreated>true</vfModuleCreated>" + EOL +
			"  <vfModuleOutputs>" + EOL +
			"    <entry>" + EOL +
			"      <key>key1</key>" + EOL +
			"      <value>value1</value>" + EOL +
			"    </entry>" + EOL +
			"    <entry>" + EOL +
			"      <key>key2</key>" + EOL +
			"      <value>value2</value>" + EOL +
			"    </entry>" + EOL +
			"  </vfModuleOutputs>" + EOL +
			"  <rollback>" + EOL +
			"    <vnfId>vnfId</vnfId>" + EOL +
			"    <vfModuleId>vfModuleId</vfModuleId>" + EOL +
			"    <vfModuleStackId>vfModuleStackId</vfModuleStackId>" + EOL +
			"    <vfModuleCreated>true</vfModuleCreated>" + EOL +
			"    <tenantId>tenantId</tenantId>" + EOL +
			"    <cloudSiteId>cloudSiteId</cloudSiteId>" + EOL +
			"    <msoRequest>" + EOL +
			"      <requestId>requestId</requestId>" + EOL +
			"      <serviceInstanceId>serviceInstanceId</serviceInstanceId>" + EOL +
			"    </msoRequest>" + EOL +
			"    <messageId>messageId</messageId>" + EOL +
			"  </rollback>" + EOL +
			"  <messageId>{{MESSAGE-ID}}</messageId>" + EOL +
			"</createVfModuleResponse>" + EOL);

		callbacks.put("updateVfModule",
			"<updateVfModuleResponse>" + EOL +
			"  <vnfId>vnfId</vnfId>" + EOL +
			"  <vfModuleId>vfModuleId</vfModuleId>" + EOL +
			"  <vfModuleStackId>vfModuleStackId</vfModuleStackId>" + EOL +
			"  <vfModuleOutputs>" + EOL +
			"    <entry>" + EOL +
			"      <key>key1</key>" + EOL +
			"      <value>value1</value>" + EOL +
			"    </entry>" + EOL +
			"    <entry>" + EOL +
			"      <key>key2</key>" + EOL +
			"      <value>value2</value>" + EOL +
			"    </entry>" + EOL +
			"  </vfModuleOutputs>" + EOL +
			"  <messageId>{{MESSAGE-ID}}</messageId>" + EOL +
			"</updateVfModuleResponse>" + EOL);

		callbacks.put("deleteVfModule",
			"<deleteVfModuleResponse>" + EOL +
			"  <vnfId>vnfId</vnfId>" + EOL +
			"  <vfModuleId>vfModuleId</vfModuleId>" + EOL +
			"  <vfModuleDeleted>true</vfModuleDeleted>" + EOL +
			"  <messageId>{{MESSAGE-ID}}</messageId>" + EOL +
			"</deleteVfModuleResponse>" + EOL);

		callbacks.put("rollbackVfModule",
			"<rollbackVfModuleResponse>" + EOL +
			"  <messageId>{{MESSAGE-ID}}</messageId>" + EOL +
			"  <vfModuleRolledback>true</vfModuleRolledback>" + EOL +
			"</rollbackVfModuleResponse>" + EOL);

		callbacks.put("vfModuleException",
			"<vfModuleException>" + EOL +
			"  <message>message</message>" + EOL +
			"  <category>category</category>" + EOL +
			"  <rolledBack>false</rolledBack>" + EOL +
			"  <messageId>{{MESSAGE-ID}}</messageId>" + EOL +
			"</vfModuleException>" + EOL);
	}

	@Test
	@Deployment(resources = {
		"subprocess/VnfAdapterRestV1.bpmn"
		})
	public void testCreateVfModuleSuccess() throws Exception {
		logStart();

		mockVNFPost("", 202, "vnfId");

		String requestId = "dffbae0e-5588-4bd6-9749-b0f0adb52312";
		String messageId = requestId + "-" + System.currentTimeMillis();
		String request = CREATE_VF_MODULE_REQUEST.replace("{{MESSAGE-ID}}", messageId);

		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		variables.put("mso-request-id", requestId);
		variables.put("isDebugLogEnabled", "true");
		variables.put("vnfAdapterRestV1Request", request);

		invokeSubProcess("vnfAdapterRestV1", businessKey, variables);
		injectVNFRestCallbacks(callbacks, "createVfModule");
		waitForProcessEnd(businessKey, 10000);

		String response = (String) getVariableFromHistory(businessKey, "vnfAdapterRestV1Response");
		System.out.println("Response:\n" + response);
		assertTrue(response!=null && response.contains("<createVfModuleResponse>"));
		assertTrue((boolean) getVariableFromHistory(businessKey, "VNFREST_SuccessIndicator"));

		logEnd();
	}

	@Test
	@Deployment(resources = {
		"subprocess/VnfAdapterRestV1.bpmn"
		})
	public void testUpdateVfModuleSuccess() throws Exception {
		logStart();

		mockVNFPut("/vfModuleId", 202);

		String requestId = "dffbae0e-5588-4bd6-9749-b0f0adb52312";
		String messageId = requestId + "-" + System.currentTimeMillis();
		String request = UPDATE_VF_MODULE_REQUEST.replace("{{MESSAGE-ID}}", messageId);

		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		variables.put("mso-request-id", requestId);
		variables.put("isDebugLogEnabled", "true");
		variables.put("vnfAdapterRestV1Request", request);

		invokeSubProcess("vnfAdapterRestV1", businessKey, variables);
		injectVNFRestCallbacks(callbacks, "updateVfModule");
		waitForProcessEnd(businessKey, 10000);

		String response = (String) getVariableFromHistory(businessKey, "vnfAdapterRestV1Response");
		System.out.println("Response:\n" + response);
		assertTrue(response.contains("<updateVfModuleResponse>"));
		assertTrue((boolean) getVariableFromHistory(businessKey, "VNFREST_SuccessIndicator"));

		logEnd();
	}

	@Test
	@Deployment(resources = {
		"subprocess/VnfAdapterRestV1.bpmn"
		})
	public void testDeleteVfModuleSuccess() throws Exception {
		logStart();

		mockVNFDelete("vnfId", "/vfModuleId", 202);

		String requestId = "dffbae0e-5588-4bd6-9749-b0f0adb52312";
		String messageId = requestId + "-" + System.currentTimeMillis();
		String request = DELETE_VF_MODULE_REQUEST.replace("{{MESSAGE-ID}}", messageId);

		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		variables.put("mso-request-id", requestId);
		variables.put("isDebugLogEnabled", "true");
		variables.put("vnfAdapterRestV1Request", request);

		invokeSubProcess("vnfAdapterRestV1", businessKey, variables);
		injectVNFRestCallbacks(callbacks, "deleteVfModule");
		waitForProcessEnd(businessKey, 10000);

		String response = (String) getVariableFromHistory(businessKey, "vnfAdapterRestV1Response");
		System.out.println("Response:\n" + response);
		assertTrue(response.contains("<deleteVfModuleResponse>"));
		assertTrue((boolean) getVariableFromHistory(businessKey, "VNFREST_SuccessIndicator"));

		logEnd();
	}

	@Test
	@Deployment(resources = {
		"subprocess/VnfAdapterRestV1.bpmn"
		})
	public void testRollbackVfModuleSuccess() throws Exception {
		logStart();

		mockVNFRollbackDelete("/vfModuleId", 202);

		String requestId = "dffbae0e-5588-4bd6-9749-b0f0adb52312";
		String messageId = requestId + "-" + System.currentTimeMillis();
		String request = ROLLBACK_VF_MODULE_REQUEST.replace("{{MESSAGE-ID}}", messageId);

		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		variables.put("mso-request-id", requestId);
		variables.put("isDebugLogEnabled", "true");
		variables.put("vnfAdapterRestV1Request", request);

		invokeSubProcess("vnfAdapterRestV1", businessKey, variables);
		injectVNFRestCallbacks(callbacks, "rollbackVfModule");
		waitForProcessEnd(businessKey, 10000);

		String response = (String) getVariableFromHistory(businessKey, "vnfAdapterRestV1Response");
		System.out.println("Response:\n" + response);
		assertTrue(response.contains("<rollbackVfModuleResponse>"));
		assertTrue((boolean) getVariableFromHistory(businessKey, "VNFREST_SuccessIndicator"));

		logEnd();
	}

	@Test
	@Deployment(resources = {
		"subprocess/VnfAdapterRestV1.bpmn"
		})
	public void testCreateVfModuleException() throws Exception {
		logStart();

		mockVNFPost("", 202, "vnfId");

		String requestId = "dffbae0e-5588-4bd6-9749-b0f0adb52312";
		String messageId = requestId + "-" + System.currentTimeMillis();
		String request = CREATE_VF_MODULE_REQUEST.replace("{{MESSAGE-ID}}", messageId);

		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		variables.put("mso-request-id", requestId);
		variables.put("isDebugLogEnabled", "true");
		variables.put("vnfAdapterRestV1Request", request);

		invokeSubProcess("vnfAdapterRestV1", businessKey, variables);
		injectVNFRestCallbacks(callbacks, "vfModuleException");
		waitForProcessEnd(businessKey, 10000);

		WorkflowException wfe = (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
		assertNotNull(wfe);
		System.out.println(wfe.toString());

		String response = (String) getVariableFromHistory(businessKey, "WorkflowResponse");
		System.out.println("Response:\n" + response);
		assertTrue(response.contains("<vfModuleException>"));
		assertFalse((boolean) getVariableFromHistory(businessKey, "VNFREST_SuccessIndicator"));

		logEnd();
	}
}

