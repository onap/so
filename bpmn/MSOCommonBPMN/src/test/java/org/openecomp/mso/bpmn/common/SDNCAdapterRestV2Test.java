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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowMessageResource;
import org.openecomp.mso.bpmn.mock.FileUtil;

/**
 * Unit tests for SDNCAdapterRestV2.bpmn.
 * 
 * This version of SDNCAdapterRest allows for interim notifications to be sent for
 * any non-final response received from SDNC.
 */
public class SDNCAdapterRestV2Test extends WorkflowTest {
		
	private final CallbackSet callbacks = new CallbackSet();
	
	/**
	 * Constructor. Insert callbacks.
	 * 
	 * @throws IOException
	 */
	public SDNCAdapterRestV2Test() throws IOException {
		String sdncCallbackFinal = FileUtil.readResourceFile("__files/SDNCAdapterRestCallbackFinal.json");
		String sdncCallbackNonFinal = FileUtil.readResourceFile("__files/SDNCAdapterRestCallbackNonFinal.json");
		callbacks.put("nonfinal", sdncCallbackNonFinal);
		callbacks.put("final", sdncCallbackFinal);
	}

	/**
	 * Test the success path through the subflow.
	 */
	@Test
	@Deployment(resources = {
			"subprocess/SDNCAdapterRestV2.bpmn",
			"subprocess/GenericNotificationService.bpmn"		
		})
	public void success() throws IOException {
		logStart();
		mocks();

		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("mso-request-id", "a4383a52-b9de-4bc4-bedf-02f3f9466535");
		variables.put("mso-service-instance-id", "fd8bcdbb-b799-43ce-a7ff-ed8f2965a3b5");
		variables.put("isDebugLogEnabled", "true");
		variables.put("SDNCREST_Request",
			FileUtil.readResourceFile("__files/SDNCAdapterRestV2Request.json"));
		variables.put("SDNCREST_InterimNotification1",
			FileUtil.readResourceFile("__files/SDNCInterimNotification1.json"));
		
		invokeSubProcess("SDNCAdapterRestV2", businessKey, variables);

		injectSDNCRestCallbacks(callbacks, "nonfinal");

		// First non-final response will have done a notification
		Object interimNotification = getVariableFromHistory(businessKey, "SDNCREST_interimNotification");
		Assert.assertNotNull(interimNotification);
		
		injectSDNCRestCallbacks(callbacks, "nonfinal");
		
		// Second non-final response will not have done a notification
		interimNotification = getVariableFromHistory(businessKey, "SDNCREST_interimNotification");
		Assert.assertNull(interimNotification);

		injectSDNCRestCallbacks(callbacks, "final");
		
		interimNotification = this.getVariableFromHistory(businessKey, "SDNCREST_interimNotification");
		Assert.assertNull(interimNotification);

		waitForProcessEnd(businessKey, 10000);

		Assert.assertTrue(isProcessEnded(businessKey));
		
		logEnd();
	}

	/**
	 * Injects a single SDNC adapter callback request. The specified callback data
	 * may contain the placeholder string ((REQUEST-ID)) which is replaced with
	 * the actual SDNC request ID. Note: this is not the requestId in the original
	 * MSO request.
	 * @param contentType the HTTP content type for the callback
	 * @param content the content of the callback
	 * @param timeout the timeout in milliseconds
	 * @return true if the callback could be injected, false otherwise
	 */
	@Override
	protected boolean injectSDNCRestCallback(String contentType, String content, long timeout) {
		String sdncRequestId = (String) getProcessVariable("SDNCAdapterRestV2",
			"SDNCAResponse_CORRELATOR", timeout);

		if (sdncRequestId == null) {
			return false;
		}

		content = content.replace("((REQUEST-ID))", sdncRequestId);
		// Deprecated usage.  All test code should switch to the (( ... )) syntax.
		content = content.replace("{{REQUEST-ID}}", sdncRequestId);

		System.out.println("Injecting SDNC adapter callback");
		WorkflowMessageResource workflowMessageResource = new WorkflowMessageResource();
		workflowMessageResource.setProcessEngineServices4junit(processEngineRule);
		Response response = workflowMessageResource.deliver(contentType, "SDNCAResponse", sdncRequestId, content);
		System.out.println("Workflow response to SDNC adapter callback: " + response);
		return true;
	}

	/**
	 * Defines WireMock stubs needed by these tests.
	 */
	private void mocks() {
		stubFor(post(urlEqualTo("/SDNCAdapter/v1/sdnc/services"))
			.willReturn(aResponse()
				.withStatus(202)
				.withHeader("Content-Type", "application/json")));
	}
}
