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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jboss.resteasy.spi.AsynchronousResponse;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Rule;
import org.openecomp.mso.bpmn.common.adapter.sdnc.CallbackHeader;
import org.openecomp.mso.bpmn.common.adapter.sdnc.SDNCAdapterCallbackRequest;
import org.openecomp.mso.bpmn.common.adapter.sdnc.SDNCAdapterResponse;
import org.openecomp.mso.bpmn.common.adapter.vnf.CreateVnfNotification;
import org.openecomp.mso.bpmn.common.adapter.vnf.DeleteVnfNotification;
import org.openecomp.mso.bpmn.common.adapter.vnf.MsoExceptionCategory;
import org.openecomp.mso.bpmn.common.adapter.vnf.MsoRequest;
import org.openecomp.mso.bpmn.common.adapter.vnf.UpdateVnfNotification;
import org.openecomp.mso.bpmn.common.adapter.vnf.VnfRollback;
import org.openecomp.mso.bpmn.common.workflow.service.SDNCAdapterCallbackServiceImpl;
import org.openecomp.mso.bpmn.common.workflow.service.VnfAdapterNotifyServiceImpl;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowAsyncResource;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowMessageResource;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;
import org.openecomp.mso.bpmn.core.utils.CamundaDBSetup;
import org.openecomp.mso.bpmn.core.PropertyConfigurationSetup;
import org.openecomp.mso.bpmn.core.domain.Resource;
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition;

import static org.openecomp.mso.bpmn.core.json.JsonUtils.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;



/**
 * A base class for Workflow tests.
 * <p>
 * WireMock response transformers may be specified by declaring public
 * static fields with the @WorkflowTestTransformer annotation. For example:
 * <pre>
 *     @WorkflowTestTransformer
 *     public static final ResponseTransformer sdncAdapterMockTransformer =
 *         new SDNCAdapterMockTransformer();
 * </pre>
 */
public class WorkflowTest {
	@Rule
	public final ProcessEngineRule processEngineRule = new ProcessEngineRule();

	@Rule
	public final WireMockRule wireMockRule;

	/**
	 * Content-Type for XML.
	 */
	protected static final String XML = "application/xml";

	/**
	 * Content-Type for JSON.
	 */
	protected static final String JSON = "application/json; charset=UTF-8";


	/**
	 * Constructor.
	 */
	public WorkflowTest() throws RuntimeException {
		// Process WorkflowTestTransformer annotations
		List<ResponseTransformer> transformerList = new ArrayList<ResponseTransformer>();

		for (Field field : getClass().getFields()) {
			WorkflowTestTransformer annotation = (WorkflowTestTransformer)
				field.getAnnotation(WorkflowTestTransformer.class);

			if (annotation == null) {
				continue;
			}

			if (!Modifier.isStatic(field.getModifiers())) {
				throw new RuntimeException(field.getDeclaringClass().getName()
					+ "#" + field.getName() + " has a @WorkflowTestTransformer "
					+ " annotation but it is not declared static");
			}

			ResponseTransformer transformer;

			try {
				transformer = (ResponseTransformer) field.get(null);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(field.getDeclaringClass().getName()
					+ "#" + field.getName() + " is not accessible", e);
			} catch (ClassCastException e) {
				throw new RuntimeException(field.getDeclaringClass().getName()
					+ "#" + field.getName() + " is not a ResponseTransformer", e);
			}

			if (transformer == null) {
				continue;
			}

			transformerList.add(transformer);
		}

		ResponseTransformer[] transformerArray =
			transformerList.toArray(new ResponseTransformer[transformerList.size()]);

		wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig()
			.port(28090).extensions(transformerArray));
	}

	@Before
	public void testSetup() throws Exception {
		CamundaDBSetup.configure();
		PropertyConfigurationSetup.init();
	}

	/**
	 * The current request ID.  Normally set when an "invoke" method is called.
	 */
	protected volatile String msoRequestId = null;

	/**
	 * The current service instance ID.  Normally set when an "invoke" method
	 * is called.
	 */
	protected volatile String msoServiceInstanceId = null;

	/**
	 * Logs a test start method.
	 */
	protected void logStart() {
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		String method = st[2].getMethodName();
		System.out.println("STARTED TEST: " + method);
	}

	/**
	 * Logs a test end method.
	 */
	protected void logEnd() {
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		String method = st[2].getMethodName();
		System.out.println("ENDED TEST: " + method);
	}

	/**
	 * Invokes a subprocess.
	 * @param processKey the process key
	 * @param businessKey a unique key that will identify the process instance
	 * @param injectedVariables variables to inject into the process
	 */
	protected void invokeSubProcess(String processKey, String businessKey, Map<String, Object> injectedVariables) {
		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		List<String> arguments = runtimeMxBean.getInputArguments();
		System.out.println("JVM args = " + arguments);

		msoRequestId = (String) injectedVariables.get("mso-request-id");
		String requestId = (String) injectedVariables.get("msoRequestId");

		if (msoRequestId == null && requestId == null) {
			String msg = "mso-request-id variable was not provided";
			System.out.println(msg);
			fail(msg);
		}

		// Note: some scenarios don't have a service-instance-id, may be null
		msoServiceInstanceId = (String) injectedVariables.get("mso-service-instance-id");

		RuntimeService runtimeService = processEngineRule.getRuntimeService();
		runtimeService.startProcessInstanceByKey(processKey, businessKey, injectedVariables);
	}

	/**
	 * Invokes an asynchronous process.
	 * Errors are handled with junit assertions and will cause the test to fail.
	 * @param processKey the process key
	 * @param schemaVersion the API schema version, e.g. "v1"
	 * @param businessKey a unique key that will identify the process instance
	 * @param request the request
	 * @return a TestAsyncResponse object associated with the test
	 */
	protected TestAsyncResponse invokeAsyncProcess(String processKey,
			String schemaVersion, String businessKey, String request) {
		return invokeAsyncProcess(processKey, schemaVersion, businessKey, request, null);
	}

	/**
	 * Invokes an asynchronous process.
	 * Errors are handled with junit assertions and will cause the test to fail.
	 * @param processKey the process key
	 * @param schemaVersion the API schema version, e.g. "v1"
	 * @param businessKey a unique key that will identify the process instance
	 * @param request the request
	 * @param injectedVariables optional variables to inject into the process
	 * @return a TestAsyncResponse object associated with the test
	 */
	protected TestAsyncResponse invokeAsyncProcess(String processKey,
			String schemaVersion, String businessKey, String request,
			Map<String, Object> injectedVariables) {

		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		List<String> arguments = runtimeMxBean.getInputArguments();
		System.out.println("JVM args = " + arguments);

		Map<String, Object> variables = createVariables(schemaVersion, businessKey,
			request, injectedVariables, false);
		VariableMapImpl variableMapImpl = createVariableMapImpl(variables);

		System.out.println("Sending " + request + " to " + processKey + " process");
		WorkflowAsyncResource workflowResource = new WorkflowAsyncResource();
		workflowResource.setProcessEngineServices4junit(processEngineRule);

		TestAsyncResponse asyncResponse = new TestAsyncResponse();
		workflowResource.startProcessInstanceByKey(asyncResponse, processKey, variableMapImpl);
		return asyncResponse;
	}

	/**
	 * Invokes an asynchronous process.
	 * Errors are handled with junit assertions and will cause the test to fail.
	 * @param processKey the process key
	 * @param schemaVersion the API schema version, e.g. "v1"
	 * @param businessKey a unique key that will identify the process instance
	 * @param request the request
	 * @param injectedVariables optional variables to inject into the process
	 * @param serviceInstantiationModel indicates whether this method is being
	 * invoked for a flow that is designed using the service instantiation model
	 * @return a TestAsyncResponse object associated with the test
	 */
	protected TestAsyncResponse invokeAsyncProcess(String processKey,
			String schemaVersion, String businessKey, String request,
			Map<String, Object> injectedVariables, boolean serviceInstantiationModel) {

		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		List<String> arguments = runtimeMxBean.getInputArguments();
		System.out.println("JVM args = " + arguments);

		Map<String, Object> variables = createVariables(schemaVersion, businessKey,
			request, injectedVariables, serviceInstantiationModel);
		VariableMapImpl variableMapImpl = createVariableMapImpl(variables);

		System.out.println("Sending " + request + " to " + processKey + " process");
		WorkflowAsyncResource workflowResource = new WorkflowAsyncResource();
		workflowResource.setProcessEngineServices4junit(processEngineRule);

		TestAsyncResponse asyncResponse = new TestAsyncResponse();
		workflowResource.startProcessInstanceByKey(asyncResponse, processKey, variableMapImpl);
		return asyncResponse;
	}

	/**
	 * Private helper method that creates a variable map for a request.
	 * Errors are handled with junit assertions and will cause the test to fail.
	 * @param schemaVersion the API schema version, e.g. "v1"
	 * @param businessKey a unique key that will identify the process instance
	 * @param request the request
	 * @param injectedVariables optional variables to inject into the process
	 * @param serviceInstantiationModel indicates whether this method is being
	 * invoked for a flow that is designed using the service instantiation model
	 * @return a variable map
	 */
	private Map<String, Object> createVariables(String schemaVersion,
			String businessKey, String request, Map<String, Object> injectedVariables,
			boolean serviceInstantiationModel) {

		Map<String, Object> variables = new HashMap<>();

		// These variables may be overridded by injected variables.
		variables.put("mso-service-request-timeout", "180");
		variables.put("isDebugLogEnabled", "true");

		// These variables may not be overridded by injected variables.
		String[] notAllowed = new String[] {
				"mso-schema-version",
				"mso-business-key",
				"bpmnRequest",
				"mso-request-id",
				"mso-service-instance-id"
		};

		if (injectedVariables != null) {
			for (String key : injectedVariables.keySet()) {
				for (String var : notAllowed) {
					if (var.equals(key)) {
						String msg = "Cannot specify " + var + " in injected variables";
						System.out.println(msg);
						fail(msg);
					}
				}

				variables.put(key, injectedVariables.get(key));
			}
		}

		variables.put("mso-schema-version", schemaVersion);
		variables.put("mso-business-key", businessKey);
		variables.put("bpmnRequest", request);

		if (serviceInstantiationModel) {

			/*
			 * The request ID and the service instance ID are generated for flows
			 * that follow the service instantiation model unless "requestId" and
			 * "serviceInstanceId" are injected variables.
			 */

			try {
				msoRequestId = (String) injectedVariables.get("requestId");
				variables.put("mso-request-id", msoRequestId);
				msoServiceInstanceId = (String) injectedVariables.get("serviceInstanceId");
				variables.put("mso-service-instance-id", msoServiceInstanceId);
			}
			catch(Exception e) {
			}
			if (msoRequestId == null || msoRequestId.trim().equals("")) {
				System.out.println("No requestId element in injectedVariables");
				variables.put("mso-request-id", UUID.randomUUID().toString());
			}
			if (msoServiceInstanceId == null || msoServiceInstanceId.trim().equals("")) {
				System.out.println("No seviceInstanceId element in injectedVariables");
				variables.put("mso-service-instance-id", UUID.randomUUID().toString());
			}

		} else {
			msoRequestId = getXMLTextElement(request, "request-id");

			if (msoRequestId == null) {
				//check in injected variables
				try {
					msoRequestId = (String) injectedVariables.get("requestId");
				}
				catch(Exception e) {
				}
				if (msoRequestId == null || msoRequestId.trim().equals("")) {
					String msg = "No request-id element in " + request;
					System.out.println(msg);
					fail(msg);
				}
			}

			variables.put("mso-request-id", msoRequestId);

			// Note: some request types don't have a service-instance-id
			msoServiceInstanceId = getXMLTextElement(request, "service-instance-id");

			if (msoServiceInstanceId != null) {
				variables.put("mso-service-instance-id", msoServiceInstanceId);
			}
		}

		return variables;
	}

	/**
	 * Private helper method that creates a camunda VariableMapImpl from a simple
	 * variable map.
	 * @param variables the simple variable map
	 * @return a VariableMap
	 */
	private VariableMapImpl createVariableMapImpl(Map<String, Object> variables) {
		Map<String, Object> wrappedVariables = new HashMap<>();

		for (String key : variables.keySet()) {
			Object value = variables.get(key);
			wrappedVariables.put(key, wrapVariableValue(value));
		}

		VariableMapImpl variableMapImpl = new VariableMapImpl();
		variableMapImpl.put("variables", wrappedVariables);
		return variableMapImpl;
	}

	/**
	 * Private helper method that wraps a variable value for inclusion in a
	 * camunda VariableMapImpl.
	 * @param value the variable value
	 * @return the wrapped variable
	 */
	private Map<String, Object> wrapVariableValue(Object value) {
		HashMap<String, Object> valueMap = new HashMap<>();
		valueMap.put("value", value);
		return valueMap;
	}

	/**
	 * Receives a response from an asynchronous process.
	 * Errors are handled with junit assertions and will cause the test to fail.
	 * @param businessKey the process business key
	 * @param asyncResponse the TestAsyncResponse object associated with the test
	 * @param timeout the timeout in milliseconds
	 * @return the WorkflowResponse
	 */
	protected WorkflowResponse receiveResponse(String businessKey,
			TestAsyncResponse asyncResponse, long timeout) {
		System.out.println("Waiting " + timeout + "ms for process with business key " + businessKey
			+ " to send a response");

		long now = System.currentTimeMillis() + timeout;
		long endTime = now + timeout;

		while (now <= endTime) {
			Response response = asyncResponse.getResponse();

			if (response != null) {
				System.out.println("Received a response from process with business key " + businessKey);

				Object entity = response.getEntity();

				if (!(entity instanceof WorkflowResponse)) {
					String msg = "Response entity is " +
						(entity == null ? "null" : entity.getClass().getName()) +
						", expected WorkflowResponse";
					System.out.println(msg);
					fail(msg);
					return null; // unreachable
				}

				return (WorkflowResponse) entity;
			}

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				String msg = "Interrupted waiting for a response from process with business key " +
					businessKey;
				System.out.println(msg);
				fail(msg);
				return null; // unreachable
			}

			now = System.currentTimeMillis();
		}

		String msg = "No response received from process with business key " + businessKey +
			" within " + timeout + "ms";
		System.out.println(msg);
		fail("Process with business key " + businessKey + " did not end within 10000ms");
		return null; // unreachable
	}

	/**
	 * Runs a program to inject SDNC callback data into the test environment.
	 * A program is essentially just a list of keys that identify callback data
	 * to be injected, in sequence. An example program:
	 * <pre>
	 *     reserve, assign, delete:ERR
	 * </pre>
	 * Errors are handled with junit assertions and will cause the test to fail.
	 * @param callbacks an object containing callback data for the program
	 * @param program the program to execute
	 */
	protected void injectSDNCRestCallbacks(CallbackSet callbacks, String program) {

		String[] cmds = program.replaceAll("\\s+", "").split(",");

		for (String cmd : cmds) {
			String action = cmd;
			String modifier = "STD";

			if (cmd.contains(":")) {
				String[] parts = cmd.split(":");
				action = parts[0];
				modifier = parts[1];
			}

			String content = null;
			String contentType = null;

			if ("STD".equals(modifier)) {
				CallbackData callbackData = callbacks.get(action);

				if (callbackData == null) {
					String msg = "No callback defined for '" + action + "' SDNC request";
					System.out.println(msg);
					fail(msg);
				}

				content = callbackData.getContent();
				contentType = callbackData.getContentType();
			} else if ("ERR".equals(modifier)) {
				content = "{\"SDNCServiceError\":{\"sdncRequestId\":\"((REQUEST-ID))\",\"responseCode\":\"500\",\"responseMessage\":\"SIMULATED ERROR FROM SDNC ADAPTER\",\"ackFinalIndicator\":\"Y\"}}";
				contentType = JSON;
			} else {
				String msg = "Invalid SDNC program modifier: '" + modifier + "'";
				System.out.println(msg);
				fail(msg);
			}

			if (contentType == null) {
				// Default for backward compatibility with existing tests.
				contentType = JSON;
			}

			if (!injectSDNCRestCallback(contentType, content, 10000)) {
				fail("Failed to inject SDNC '" + action + "' callback");
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				fail("Interrupted after injection of SDNC '" + action + "' callback");
			}
		}
	}

	/**
	 * Runs a program to inject SDNC events into the test environment.
	 * A program is essentially just a list of keys that identify event data
	 * to be injected, in sequence. An example program:
	 * <pre>
	 *     event1, event2
	 * </pre>
	 * NOTE: Each callback must have a message type associated with it, e.g.
	 * "SDNCAEvent".
	 * Errors are handled with junit assertions and will cause the test to fail.
	 * @param callbacks an object containing event data for the program
	 * @param program the program to execute
	 */
	protected void injectSDNCEvents(CallbackSet callbacks, String program) {
		injectWorkflowMessages(callbacks, program);
	}

	/**
	 * Runs a program to inject SDNC callback data into the test environment.
	 * A program is essentially just a list of keys that identify callback data
	 * to be injected, in sequence. An example program:
	 * <pre>
	 *     reserve, assign, delete:ERR
	 * </pre>
	 * Errors are handled with junit assertions and will cause the test to fail.
	 * @param callbacks an object containing callback data for the program
	 * @param program the program to execute
	 */
	protected void injectSDNCCallbacks(CallbackSet callbacks, String program) {

		String[] cmds = program.replaceAll("\\s+", "").split(",");

		for (String cmd : cmds) {
			String action = cmd;
			String modifier = "STD";

			if (cmd.contains(":")) {
				String[] parts = cmd.split(":");
				action = parts[0];
				modifier = parts[1];
			}

			String content = null;
			int respCode = 200;
			String respMsg = "OK";

			if ("STD".equals(modifier)) {
				CallbackData callbackData = callbacks.get(action);

				if (callbackData == null) {
					String msg = "No callback defined for '" + action + "' SDNC request";
					System.out.println(msg);
					fail(msg);
				}

				content = callbackData.getContent();
				respCode = 200;
				respMsg = "OK";
			} else if ("CREATED".equals(modifier)) {
				CallbackData callbackData = callbacks.get(action);

				if (callbackData == null) {
					String msg = "No callback defined for '" + action + "' SDNC request";
					System.out.println(msg);
					fail(msg);
				}

				content = callbackData.getContent();
				respCode = 201;
				respMsg = "Created";
			} else if ("ERR".equals(modifier)) {
				content = "<svc-request-id>((REQUEST-ID))</svc-request-id><response-code>500</response-code><response-message>SIMULATED ERROR FROM SDNC ADAPTER</response-message>";
				respCode = 500;
				respMsg = "SERVER ERROR";
			} else {
				String msg = "Invalid SDNC program modifier: '" + modifier + "'";
				System.out.println(msg);
				fail(msg);
			}

			if (!injectSDNCCallback(respCode, respMsg, content, 10000)) {
				fail("Failed to inject SDNC '" + action + "' callback");
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				fail("Interrupted after injection of SDNC '" + action + "' callback");
			}
		}
	}

	/**
	 * Runs a program to inject VNF adapter REST callback data into the test environment.
	 * A program is essentially just a list of keys that identify callback data
	 * to be injected, in sequence. An example program:
	 * <pre>
	 *     create, rollback
	 * </pre>
	 * Errors are handled with junit assertions and will cause the test to fail.
	 * @param callbacks an object containing callback data for the program
	 * @param program the program to execute
	 */
	protected void injectVNFRestCallbacks(CallbackSet callbacks, String program) {

		String[] cmds = program.replaceAll("\\s+", "").split(",");

		for (String cmd : cmds) {
			String action = cmd;
			String modifier = "STD";

			if (cmd.contains(":")) {
				String[] parts = cmd.split(":");
				action = parts[0];
				modifier = parts[1];
			}

			String content = null;
			String contentType = null;

			if ("STD".equals(modifier)) {
				CallbackData callbackData = callbacks.get(action);

				if (callbackData == null) {
					String msg = "No callback defined for '" + action + "' VNF REST request";
					System.out.println(msg);
					fail(msg);
				}

				content = callbackData.getContent();
				contentType = callbackData.getContentType();
			} else if ("ERR".equals(modifier)) {
				content = "SIMULATED ERROR FROM VNF ADAPTER";
				contentType = "text/plain";
			} else {
				String msg = "Invalid VNF REST program modifier: '" + modifier + "'";
				System.out.println(msg);
				fail(msg);
			}

			if (contentType == null) {
				// Default for backward compatibility with existing tests.
				contentType = XML;
			}

			if (!injectVnfAdapterRestCallback(contentType, content, 10000)) {
				fail("Failed to inject VNF REST '" + action + "' callback");
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				fail("Interrupted after injection of VNF REST '" + action + "' callback");
			}
		}
	}

	/**
	 * Runs a program to inject VNF callback data into the test environment.
	 * A program is essentially just a list of keys that identify callback data
	 * to be injected, in sequence. An example program:
	 * <pre>
	 *     createVnf, deleteVnf
	 * </pre>
	 * Errors are handled with junit assertions and will cause the test to fail.
	 * @param callbacks an object containing callback data for the program
	 * @param program the program to execute
	 */
	protected void injectVNFCallbacks(CallbackSet callbacks, String program) {

		String[] cmds = program.replaceAll("\\s+", "").split(",");

		for (String cmd : cmds) {
			String action = cmd;
			String modifier = "STD";

			if (cmd.contains(":")) {
				String[] parts = cmd.split(":");
				action = parts[0];
				modifier = parts[1];
			}

			String content = null;

			if ("STD".equals(modifier)) {
				CallbackData callbackData = callbacks.get(action);

				if (callbackData == null) {
					String msg = "No callback defined for '" + action + "' VNF request";
					System.out.println(msg);
					fail(msg);
				}

				content = callbackData.getContent();
			} else if ("ERR".equals(modifier)) {
				String msg = "Currently unsupported VNF program modifier: '" + modifier + "'";
				System.out.println(msg);
				fail(msg);
			} else {
				String msg = "Invalid VNF program modifier: '" + modifier + "'";
				System.out.println(msg);
				fail(msg);
			}

			boolean injected = false;

			if (content.contains("createVnfNotification")) {
				injected = injectCreateVNFCallback(content, 10000);
			} else if (content.contains("deleteVnfNotification")) {
				injected = injectDeleteVNFCallback(content, 10000);
			} else if (content.contains("updateVnfNotification")) {
				injected = injectUpdateVNFCallback(content, 10000);
			}

			if (!injected) {
				String msg = "Failed to inject VNF '" + action + "' callback";
				System.out.println(msg);
				fail(msg);
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				fail("Interrupted after injection of VNF '" + action + "' callback");
			}
		}
	}

	/**
	 * Waits for the number of running processes with the specified process
	 * definition key to equal a particular count.
	 * @param processKey the process definition key
	 * @param count the desired count
	 * @param timeout the timeout in milliseconds
	 */
	protected void waitForRunningProcessCount(String processKey, int count, long timeout) {
		System.out.println("Waiting " + timeout + "ms for there to be " + count + " "
			+ processKey + " instances");

		long now = System.currentTimeMillis() + timeout;
		long endTime = now + timeout;
		int last = -1;

		while (now <= endTime) {
			int actual = processEngineRule.getRuntimeService()
				.createProcessInstanceQuery()
				.processDefinitionKey(processKey)
				.list().size();

			if (actual != last) {
				System.out.println("There are now " + actual + " "
					+ processKey + " instances");
				last = actual;
			}

			if (actual == count) {
				return;
			}

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				String msg = "Interrupted waiting for there to be " + count + " "
					+ processKey + " instances";
				System.out.println(msg);
				fail(msg);
			}

			now = System.currentTimeMillis();
		}

		String msg = "Timed out waiting for there to be " + count + " "
			+ processKey + " instances";
		System.out.println(msg);
		fail(msg);
	}

	/**
	 * Waits for the specified process variable to be set.
	 * @param processKey the process definition key
	 * @param variable the variable name
	 * @param timeout the timeout in milliseconds
	 * @return the variable value, or null if it cannot be obtained
	 *         in the specified time
	 */
	protected Object getProcessVariable(String processKey, String variable,
			long timeout) {

		System.out.println("Waiting " + timeout + "ms for "
			+ processKey + "." + variable + " to be set");

		long now = System.currentTimeMillis() + timeout;
		long endTime = now + timeout;

		ProcessInstance processInstance = null;
		Object value = null;

		while (value == null) {
			if (now > endTime) {
				if (processInstance == null) {
					System.out.println("Timed out waiting for "
						+ processKey + " to start");
				} else {
					System.out.println("Timed out waiting for "
						+ processKey + "[" + processInstance.getId()
						+ "]." + variable + " to be set");
				}

				return null;
			}

			if (processInstance == null) {
				processInstance = processEngineRule.getRuntimeService()
					.createProcessInstanceQuery()
					.processDefinitionKey(processKey)
					.singleResult();
			}

			if (processInstance != null) {
				value = processEngineRule.getRuntimeService()
					.getVariable(processInstance.getId(), variable);
			}

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				System.out.println("Interrupted waiting for "
					+ processKey + "." + variable + " to be set");
				return null;
			}

			now = System.currentTimeMillis();
		}

		System.out.println(processKey + "["
			+ processInstance.getId() + "]." + variable + "="
			+ value);

		return value;
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
	protected boolean injectSDNCRestCallback(String contentType, String content, long timeout) {
		String sdncRequestId = (String) getProcessVariable("SDNCAdapterRestV1",
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
	 * Injects a single SDNC adapter callback request. The specified callback data
	 * may contain the placeholder string ((REQUEST-ID)) which is replaced with
	 * the actual SDNC request ID. Note: this is not the requestId in the original
	 * MSO request.
	 * @param content the content of the callback
	 * @param respCode the response code (normally 200)
	 * @param respMsg the response message (normally "OK")
	 * @param timeout the timeout in milliseconds
	 * @return true if the callback could be injected, false otherwise
	 */
	protected boolean injectSDNCCallback(int respCode, String respMsg,
			String content, long timeout) {

		String sdncRequestId = (String) getProcessVariable("sdncAdapter",
			"SDNCA_requestId", timeout);

		if (sdncRequestId == null) {
			return false;
		}

		content = content.replace("((REQUEST-ID))", sdncRequestId);
		// Deprecated usage.  All test code should switch to the (( ... )) syntax.
		content = content.replace("{{REQUEST-ID}}", sdncRequestId);

		System.out.println("Injecting SDNC adapter callback");
		CallbackHeader callbackHeader = new CallbackHeader();
		callbackHeader.setRequestId(sdncRequestId);
		callbackHeader.setResponseCode(String.valueOf(respCode));
		callbackHeader.setResponseMessage(respMsg);
		SDNCAdapterCallbackRequest sdncAdapterCallbackRequest = new SDNCAdapterCallbackRequest();
		sdncAdapterCallbackRequest.setCallbackHeader(callbackHeader);
		sdncAdapterCallbackRequest.setRequestData(content);
		SDNCAdapterCallbackServiceImpl callbackService = new SDNCAdapterCallbackServiceImpl();
		callbackService.setProcessEngineServices4junit(processEngineRule);
		SDNCAdapterResponse sdncAdapterResponse = callbackService.sdncAdapterCallback(sdncAdapterCallbackRequest);
		System.out.println("Workflow response to SDNC adapter callback: " + sdncAdapterResponse);

		return true;
	}

	/**
	 * Injects a single VNF adapter callback request. The specified callback data
	 * may contain the placeholder string ((MESSAGE-ID)) which is replaced with
	 * the actual message ID. Note: this is not the requestId in the original
	 * MSO request.
	 * @param contentType the HTTP content type for the callback
	 * @param content the content of the callback
	 * @param timeout the timeout in milliseconds
	 * @return true if the callback could be injected, false otherwise
	 */
	protected boolean injectVnfAdapterRestCallback(String contentType, String content, long timeout) {
		String messageId = (String) getProcessVariable("vnfAdapterRestV1",
			"VNFAResponse_CORRELATOR", timeout);

		if (messageId == null) {
			return false;
		}

		content = content.replace("((MESSAGE-ID))", messageId);
		// Deprecated usage.  All test code should switch to the (( ... )) syntax.
		content = content.replace("{{MESSAGE-ID}}", messageId);

		System.out.println("Injecting VNF adapter callback");
		WorkflowMessageResource workflowMessageResource = new WorkflowMessageResource();
		workflowMessageResource.setProcessEngineServices4junit(processEngineRule);
		Response response = workflowMessageResource.deliver(contentType, "VNFAResponse", messageId, content);
		System.out.println("Workflow response to VNF adapter callback: " + response);
		return true;
	}

	/**
	 * Injects a Create VNF adapter callback request. The specified callback data
	 * may contain the placeholder string ((MESSAGE-ID)) which is replaced with
	 * the actual message ID.  It may also contain the placeholder string
	 * ((REQUEST-ID)) which is replaced request ID of the original MSO request.
	 * @param content the content of the callback
	 * @param timeout the timeout in milliseconds
	 * @return true if the callback could be injected, false otherwise
	 * @throws JAXBException if the content does not adhere to the schema
	 */
	protected boolean injectCreateVNFCallback(String content, long timeout) {

		String messageId = (String) getProcessVariable("vnfAdapterCreateV1",
			"VNFC_messageId", timeout);

		if (messageId == null) {
			return false;
		}

		content = content.replace("((MESSAGE-ID))", messageId);
		// Deprecated usage.  All test code should switch to the (( ... )) syntax.
		content = content.replace("{{MESSAGE-ID}}", messageId);

		if(content.contains("((REQUEST-ID))")){
			content = content.replace("((REQUEST-ID))", msoRequestId);
		// Deprecated usage.  All test code should switch to the (( ... )) syntax.
			content = content.replace("{{REQUEST-ID}}", msoRequestId);
		}

		System.out.println("Injecting VNF adapter callback");

		// Is it possible to unmarshal this with JAXB?  I couldn't.

		CreateVnfNotification createVnfNotification = new CreateVnfNotification();
		XPathTool xpathTool = new VnfNotifyXPathTool();
		xpathTool.setXML(content);

		try {
			String completed = xpathTool.evaluate(
				"/tns:createVnfNotification/tns:completed/text()");
			createVnfNotification.setCompleted("true".equals(completed));

			String vnfId = xpathTool.evaluate(
				"/tns:createVnfNotification/tns:vnfId/text()");
			createVnfNotification.setVnfId(vnfId);

			NodeList entries = (NodeList) xpathTool.evaluate(
				"/tns:createVnfNotification/tns:outputs/tns:entry",
				XPathConstants.NODESET);

			CreateVnfNotificationOutputs outputs = new CreateVnfNotificationOutputs();

			for (int i = 0; i < entries.getLength(); i++) {
				Node node = entries.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element entry = (Element) node;
					String key = entry.getElementsByTagNameNS("*", "key").item(0).getTextContent();
					String value = entry.getElementsByTagNameNS("*", "value").item(0).getTextContent();
					outputs.add(key, value);
				}
			}

			createVnfNotification.setOutputs(outputs);

			VnfRollback rollback = new VnfRollback();

			String cloudSiteId = xpathTool.evaluate(
				"/tns:createVnfNotification/tns:rollback/tns:cloudSiteId/text()");
			rollback.setCloudSiteId(cloudSiteId);

			String requestId = xpathTool.evaluate(
				"/tns:createVnfNotification/tns:rollback/tns:msoRequest/tns:requestId/text()");
			String serviceInstanceId = xpathTool.evaluate(
				"/tns:createVnfNotification/tns:rollback/tns:msoRequest/tns:serviceInstanceId/text()");

			if (requestId != null || serviceInstanceId != null) {
				MsoRequest msoRequest = new MsoRequest();
				msoRequest.setRequestId(requestId);
				msoRequest.setServiceInstanceId(serviceInstanceId);
				rollback.setMsoRequest(msoRequest);
			}

			String tenantCreated = xpathTool.evaluate(
				"/tns:createVnfNotification/tns:rollback/tns:tenantCreated/text()");
			rollback.setTenantCreated("true".equals(tenantCreated));

			String tenantId = xpathTool.evaluate(
				"/tns:createVnfNotification/tns:rollback/tns:tenantId/text()");
			rollback.setTenantId(tenantId);

			String vnfCreated = xpathTool.evaluate(
				"/tns:createVnfNotification/tns:rollback/tns:vnfCreated/text()");
			rollback.setVnfCreated("true".equals(vnfCreated));

			String rollbackVnfId = xpathTool.evaluate(
				"/tns:createVnfNotification/tns:rollback/tns:vnfId/text()");
			rollback.setVnfId(rollbackVnfId);

			createVnfNotification.setRollback(rollback);

		} catch (Exception e) {
			System.out.println("Failed to unmarshal VNF callback content:");
			System.out.println(content);
			return false;
		}

		VnfAdapterNotifyServiceImpl notifyService = new VnfAdapterNotifyServiceImpl();
		notifyService.setProcessEngineServices4junit(processEngineRule);

		notifyService.createVnfNotification(
			messageId,
			createVnfNotification.isCompleted(),
			createVnfNotification.getException(),
			createVnfNotification.getErrorMessage(),
			createVnfNotification.getVnfId(),
			createVnfNotification.getOutputs(),
			createVnfNotification.getRollback());

		return true;
	}

	/**
	 * Injects a Delete VNF adapter callback request. The specified callback data
	 * may contain the placeholder string ((MESSAGE-ID)) which is replaced with
	 * the actual message ID.  It may also contain the placeholder string
	 * ((REQUEST-ID)) which is replaced request ID of the original MSO request.
	 * @param content the content of the callback
	 * @param timeout the timeout in milliseconds
	 * @return true if the callback could be injected, false otherwise
	 * @throws JAXBException if the content does not adhere to the schema
	 */
	protected boolean injectDeleteVNFCallback(String content, long timeout) {

		String messageId = (String) getProcessVariable("vnfAdapterDeleteV1",
			"VNFDEL_uuid", timeout);

		if (messageId == null) {
			return false;
		}

		content = content.replace("((MESSAGE-ID))", messageId);
		// Deprecated usage.  All test code should switch to the (( ... )) syntax.
		content = content.replace("{{MESSAGE-ID}}", messageId);

		System.out.println("Injecting VNF adapter delete callback");

		// Is it possible to unmarshal this with JAXB?  I couldn't.

		DeleteVnfNotification deleteVnfNotification = new DeleteVnfNotification();
		XPathTool xpathTool = new VnfNotifyXPathTool();
		xpathTool.setXML(content);

		try {
			String completed = xpathTool.evaluate(
				"/tns:deleteVnfNotification/tns:completed/text()");
			deleteVnfNotification.setCompleted("true".equals(completed));
			// if notification failure, set the exception and error message
			if (deleteVnfNotification.isCompleted() == false) {
				deleteVnfNotification.setException(MsoExceptionCategory.INTERNAL);
				deleteVnfNotification.setErrorMessage(xpathTool.evaluate(
						"/tns:deleteVnfNotification/tns:errorMessage/text()")) ;
			}

		} catch (Exception e) {
			System.out.println("Failed to unmarshal VNF Delete callback content:");
			System.out.println(content);
			return false;
		}

		VnfAdapterNotifyServiceImpl notifyService = new VnfAdapterNotifyServiceImpl();
		notifyService.setProcessEngineServices4junit(processEngineRule);

		notifyService.deleteVnfNotification(
			messageId,
			deleteVnfNotification.isCompleted(),
			deleteVnfNotification.getException(),
			deleteVnfNotification.getErrorMessage());

		return true;
	}

	/**
	 * Injects a Update VNF adapter callback request. The specified callback data
	 * may contain the placeholder string ((MESSAGE-ID)) which is replaced with
	 * the actual message ID.  It may also contain the placeholder string
	 * ((REQUEST-ID)) which is replaced request ID of the original MSO request.
	 * @param content the content of the callback
	 * @param timeout the timeout in milliseconds
	 * @return true if the callback could be injected, false otherwise
	 * @throws JAXBException if the content does not adhere to the schema
	 */
	protected boolean injectUpdateVNFCallback(String content, long timeout) {

		String messageId = (String) getProcessVariable("vnfAdapterUpdate",
			"VNFU_messageId", timeout);

		if (messageId == null) {
			return false;
		}

		content = content.replace("((MESSAGE-ID))", messageId);
		// Deprecated usage.  All test code should switch to the (( ... )) syntax.
		content = content.replace("{{MESSAGE-ID}}", messageId);

		content = content.replace("((REQUEST-ID))", msoRequestId);
		// Deprecated usage.  All test code should switch to the (( ... )) syntax.
		content = content.replace("{{REQUEST-ID}}", msoRequestId);

		System.out.println("Injecting VNF adapter callback");

		// Is it possible to unmarshal this with JAXB?  I couldn't.

		UpdateVnfNotification updateVnfNotification = new UpdateVnfNotification();
		XPathTool xpathTool = new VnfNotifyXPathTool();
		xpathTool.setXML(content);

		try {
			String completed = xpathTool.evaluate(
				"/tns:updateVnfNotification/tns:completed/text()");
			updateVnfNotification.setCompleted("true".equals(completed));

			NodeList entries = (NodeList) xpathTool.evaluate(
				"/tns:updateVnfNotification/tns:outputs/tns:entry",
				XPathConstants.NODESET);

			UpdateVnfNotificationOutputs outputs = new UpdateVnfNotificationOutputs();

			for (int i = 0; i < entries.getLength(); i++) {
				Node node = entries.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element entry = (Element) node;
					String key = entry.getElementsByTagNameNS("*", "key").item(0).getTextContent();
					String value = entry.getElementsByTagNameNS("*", "value").item(0).getTextContent();
					outputs.add(key, value);
				}
			}

			updateVnfNotification.setOutputs(outputs);

			VnfRollback rollback = new VnfRollback();

			String cloudSiteId = xpathTool.evaluate(
				"/tns:updateVnfNotification/tns:rollback/tns:cloudSiteId/text()");
			rollback.setCloudSiteId(cloudSiteId);

			String requestId = xpathTool.evaluate(
				"/tns:updateVnfNotification/tns:rollback/tns:msoRequest/tns:requestId/text()");
			String serviceInstanceId = xpathTool.evaluate(
				"/tns:updateVnfNotification/tns:rollback/tns:msoRequest/tns:serviceInstanceId/text()");

			if (requestId != null || serviceInstanceId != null) {
				MsoRequest msoRequest = new MsoRequest();
				msoRequest.setRequestId(requestId);
				msoRequest.setServiceInstanceId(serviceInstanceId);
				rollback.setMsoRequest(msoRequest);
			}

			String tenantCreated = xpathTool.evaluate(
				"/tns:updateVnfNotification/tns:rollback/tns:tenantCreated/text()");
			rollback.setTenantCreated("true".equals(tenantCreated));

			String tenantId = xpathTool.evaluate(
				"/tns:updateVnfNotification/tns:rollback/tns:tenantId/text()");
			rollback.setTenantId(tenantId);

			String vnfCreated = xpathTool.evaluate(
				"/tns:updateVnfNotification/tns:rollback/tns:vnfCreated/text()");
			rollback.setVnfCreated("true".equals(vnfCreated));

			String rollbackVnfId = xpathTool.evaluate(
				"/tns:updateVnfNotification/tns:rollback/tns:vnfId/text()");
			rollback.setVnfId(rollbackVnfId);

			updateVnfNotification.setRollback(rollback);

		} catch (Exception e) {
			System.out.println("Failed to unmarshal VNF callback content:");
			System.out.println(content);
			return false;
		}

		VnfAdapterNotifyServiceImpl notifyService = new VnfAdapterNotifyServiceImpl();
		notifyService.setProcessEngineServices4junit(processEngineRule);

		notifyService.updateVnfNotification(
			messageId,
			updateVnfNotification.isCompleted(),
			updateVnfNotification.getException(),
			updateVnfNotification.getErrorMessage(),
			updateVnfNotification.getOutputs(),
			updateVnfNotification.getRollback());

		return true;
	}

	/**
	 * Runs a program to inject workflow messages into the test environment.
	 * A program is essentially just a list of keys that identify event data
	 * to be injected, in sequence. An example program:
	 * <pre>
	 *     event1, event2
	 * </pre>
	 * Errors are handled with junit assertions and will cause the test to fail.
	 * NOTE: Each callback must have a workflow message type associated with it.
	 * @param callbacks an object containing event data for the program
	 * @param program the program to execute
	 */
	protected void injectWorkflowMessages(CallbackSet callbacks, String program) {

		String[] cmds = program.replaceAll("\\s+", "").split(",");

		for (String cmd : cmds) {
			String action = cmd;
			String modifier = "STD";

			if (cmd.contains(":")) {
				String[] parts = cmd.split(":");
				action = parts[0];
				modifier = parts[1];
			}

			String messageType = null;
			String content = null;
			String contentType = null;

			if ("STD".equals(modifier)) {
				CallbackData callbackData = callbacks.get(action);

				if (callbackData == null) {
					String msg = "No '" + action + "' workflow message callback is defined";
					System.out.println(msg);
					fail(msg);
				}

				messageType = callbackData.getMessageType();

				if (messageType == null || messageType.trim().equals("")) {
					String msg = "No workflow message type is defined in the '" + action + "' callback";
					System.out.println(msg);
					fail(msg);
				}

				content = callbackData.getContent();
				contentType = callbackData.getContentType();
			} else {
				String msg = "Invalid workflow message program modifier: '" + modifier + "'";
				System.out.println(msg);
				fail(msg);
			}

			if (!injectWorkflowMessage(contentType, messageType, content, 10000)) {
				fail("Failed to inject '" + action + "' workflow message");
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				fail("Interrupted after injection of '" + action + "' workflow message");
			}
		}
	}

	/**
	 * Injects a workflow message. The specified callback data may contain the
	 * placeholder string ((CORRELATOR)) which is replaced with the actual
	 * correlator value.
	 * @param contentType the HTTP contentType for the message (possibly null)
	 * @param messageType the message type
	 * @param content the message content (possibly null)
	 * @param timeout the timeout in milliseconds
	 * @return true if the message could be injected, false otherwise
	 */
	protected boolean injectWorkflowMessage(String contentType, String messageType, String content, long timeout) {
		String correlator = (String) getProcessVariable("ReceiveWorkflowMessage",
			messageType + "_CORRELATOR", timeout);

		if (correlator == null) {
			return false;
		}

		if (content != null) {
			content = content.replace("((CORRELATOR))", correlator);
		}

		System.out.println("Injecting " + messageType + " message");
		WorkflowMessageResource workflowMessageResource = new WorkflowMessageResource();
		workflowMessageResource.setProcessEngineServices4junit(processEngineRule);
		Response response = workflowMessageResource.deliver(contentType, messageType, correlator, content);
		System.out.println("Workflow response to " + messageType + " message: " + response);
		return true;
	}

	/**
	 * Runs a program to inject sniro workflow messages into the test environment.
	 * A program is essentially just a list of keys that identify event data
	 * to be injected, in sequence. For more details, see
	 * injectSNIROCallbacks(String contentType, String messageType, String content, long timeout)
	 *
	 * Errors are handled with junit assertions and will cause the test to fail.
	 * NOTE: Each callback must have a workflow message type associated with it.
	 *
	 * @param callbacks an object containing event data for the program
	 * @param program the program to execute
	 */
	protected void injectSNIROCallbacks(CallbackSet callbacks, String program) {

		String[] cmds = program.replaceAll("\\s+", "").split(",");

		for (String cmd : cmds) {
			String action = cmd;
			String modifier = "STD";

			if (cmd.contains(":")) {
				String[] parts = cmd.split(":");
				action = parts[0];
				modifier = parts[1];
			}

			String messageType = null;
			String content = null;
			String contentType = null;

			if ("STD".equals(modifier)) {
				CallbackData callbackData = callbacks.get(action);

				if (callbackData == null) {
					String msg = "No '" + action + "' workflow message callback is defined";
					System.out.println(msg);
					fail(msg);
				}

				messageType = callbackData.getMessageType();

				if (messageType == null || messageType.trim().equals("")) {
					String msg = "No workflow message type is defined in the '" + action + "' callback";
					System.out.println(msg);
					fail(msg);
				}

				content = callbackData.getContent();
				contentType = callbackData.getContentType();
			} else {
				String msg = "Invalid workflow message program modifier: '" + modifier + "'";
				System.out.println(msg);
				fail(msg);
			}

			if (!injectSNIROCallbacks(contentType, messageType, content, 10000)) {
				fail("Failed to inject '" + action + "' workflow message");
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				fail("Interrupted after injection of '" + action + "' workflow message");
			}
		}
	}

	/**
	 * Injects a sniro workflow message. The specified callback response may
	 * contain the placeholder strings ((CORRELATOR)) and ((SERVICE_RESOURCE_ID))
	 * The ((CORRELATOR)) is replaced with the actual correlator value from the
	 * request. The ((SERVICE_RESOURCE_ID)) is replaced with the actual serviceResourceId
	 * value from the sniro request. Currently this only works with sniro request
	 * that contain only 1 resource.
	 *
	 * @param contentType the HTTP contentType for the message (possibly null)
	 * @param messageType the message type
	 * @param content the message content (possibly null)
	 * @param timeout the timeout in milliseconds
	 * @return true if the message could be injected, false otherwise
	 */
	protected boolean injectSNIROCallbacks(String contentType, String messageType, String content, long timeout) {
		String correlator = (String) getProcessVariable("ReceiveWorkflowMessage",
			messageType + "_CORRELATOR", timeout);

		if (correlator == null) {
			return false;
		}
		if (content != null) {
			content = content.replace("((CORRELATOR))", correlator);
			if(messageType.equalsIgnoreCase("SNIROResponse")){
				//TODO figure out a solution for when there is more than 1 resource being homed (i.e. more than 1 reason in the placement list)
				ServiceDecomposition decomp = (ServiceDecomposition) getProcessVariable("Homing", "serviceDecomposition", timeout);
				List<Resource> resourceList = decomp.getServiceResources();
				if(resourceList.size() == 1){
					String resourceId = "";
					for(Resource resource:resourceList){
						resourceId = resource.getResourceId();
					}
					String homingList = getJsonValue(content, "solutionInfo.placement");
					JSONArray placementArr = new JSONArray(homingList);
					if(placementArr.length() == 1){
						content = content.replace("((SERVICE_RESOURCE_ID))", resourceId);
					}
					String licenseInfoList = getJsonValue(content, "solutionInfo.licenseInfo");
					JSONArray licenseArr = new JSONArray(licenseInfoList);
					if(licenseArr.length() == 1){
						content = content.replace("((SERVICE_RESOURCE_ID))", resourceId);
					}
				}
			}
		}
		System.out.println("Injecting " + messageType + " message");
		WorkflowMessageResource workflowMessageResource = new WorkflowMessageResource();
		workflowMessageResource.setProcessEngineServices4junit(processEngineRule);
		Response response = workflowMessageResource.deliver(contentType, messageType, correlator, content);
		System.out.println("Workflow response to " + messageType + " message: " + response);
		return true;
	}


	/**
	 * Wait for the process to end.
	 * @param businessKey the process business key
	 * @param timeout the amount of time to wait, in milliseconds
	 */
	protected void waitForProcessEnd(String businessKey, long timeout) {
		System.out.println("Waiting " + timeout + "ms for process with business key " +
			businessKey + " to end");

		long now = System.currentTimeMillis() + timeout;
		long endTime = now + timeout;

		while (now <= endTime) {
			if (isProcessEnded(businessKey)) {
				System.out.println("Process with business key " + businessKey + " has ended");
				return;
			}

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				String msg = "Interrupted waiting for process with business key " +
					businessKey + " to end";
				System.out.println(msg);
				fail(msg);
			}

			now = System.currentTimeMillis();
		}

		String msg = "Process with business key " + businessKey +
			" did not end within " + timeout + "ms";
		System.out.println(msg);
		fail(msg);
	}

	/**
	 * Verifies that the specified historic process variable has the specified value.
	 * If the variable does not have the specified value, the test is failed.
	 * @param businessKey the process business key
	 * @param variable the variable name
	 * @param value the expected variable value
	 */
	protected void checkVariable(String businessKey, String variable, Object value) {
		if (!isProcessEnded(businessKey)) {
			fail("Cannot get historic variable " + variable + " because process with business key " +
				businessKey + " has not ended");
		}

		Object variableValue = getVariableFromHistory(businessKey, variable);
		assertEquals(value, variableValue);
	}

	/**
	 * Checks to see if the specified process is ended.
	 * @param businessKey the process business Key
	 * @return true if the process is ended
	 */
	protected boolean isProcessEnded(String businessKey) {
		HistoricProcessInstance processInstance = processEngineRule.getHistoryService()
			.createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();
		return processInstance != null && processInstance.getEndTime() != null;
	}

	/**
	 * Gets a variable value from a historical process instance.
	 * @param businessKey the process business key
	 * @param variableName the variable name
	 * @return the variable value, or null if the variable could not be
	 * obtained
	 */
	protected Object getVariableFromHistory(String businessKey, String variableName) {
		try {
			HistoricProcessInstance processInstance = processEngineRule.getHistoryService()
				.createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();

			if (processInstance == null) {
				return null;
			}

			HistoricVariableInstance v = processEngineRule.getHistoryService()
				.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId())
				.variableName(variableName).singleResult();
			return v == null ? null : v.getValue();
		} catch (Exception e) {
			System.out.println("Error retrieving variable " + variableName +
				" from historical process with business key " + businessKey + ": " + e);
			return null;
		}
	}

	/**
	 * Gets the value of a subflow variable from the specified subflow's
	 * historical process instance.
	 *
	 * @param subflowName - the name of the subflow that contains the variable
	 * @param variableName the variable name
	 *
	 * @return the variable value, or null if the variable could not be obtained
	 *
	 */
	protected Object getVariableFromSubflowHistory(String subflowName, String variableName) {
		try {
			List<HistoricProcessInstance> processInstanceList = processEngineRule.getHistoryService()
					.createHistoricProcessInstanceQuery().processDefinitionName(subflowName).list();

			if (processInstanceList == null) {
				return null;
			}

			processInstanceList.sort((m1, m2) -> m1.getStartTime().compareTo(m2.getStartTime()));

			HistoricProcessInstance processInstance = processInstanceList.get(0);

			HistoricVariableInstance v = processEngineRule.getHistoryService()
				.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId())
				.variableName(variableName).singleResult();
			return v == null ? null : v.getValue();
		} catch (Exception e) {
			System.out.println("Error retrieving variable " + variableName +
					" from sub flow: " + subflowName + ", Exception is: " + e);
			return null;
		}
	}

	/**
	 * Gets the value of a subflow variable from the subflow's
	 * historical process x instance.
	 *
	 * @param subflowName - the name of the subflow that contains the variable
	 * @param variableName the variable name
	 * @param subflowInstanceIndex - the instance of the subflow (use when same subflow is called more than once from mainflow)
	 *
	 * @return the variable value, or null if the variable could not be obtained
	 */
	protected Object getVariableFromSubflowHistory(int subflowInstanceIndex, String subflowName, String variableName) {
		try {
			List<HistoricProcessInstance> processInstanceList = processEngineRule.getHistoryService()
					.createHistoricProcessInstanceQuery().processDefinitionName(subflowName).list();

			if (processInstanceList == null) {
				return null;
			}

			processInstanceList.sort((m1, m2) -> m1.getStartTime().compareTo(m2.getStartTime()));

			HistoricProcessInstance processInstance = processInstanceList.get(subflowInstanceIndex);

			HistoricVariableInstance v = processEngineRule.getHistoryService()
				.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId())
				.variableName(variableName).singleResult();
			return v == null ? null : v.getValue();
		} catch (Exception e) {
			System.out.println("Error retrieving variable " + variableName +
				" from " + subflowInstanceIndex + " instance index of sub flow: " + subflowName + ", Exception is: " + e);
			return null;
		}
	}


	/**
	 * Extracts text from an XML element. This method is not namespace aware
	 * (namespaces are ignored).  The first matching element is selected.
	 * @param xml the XML document or fragment
	 * @param tag the desired element, e.g. "<name>"
	 * @return the element text, or null if the element was not found
	 */
	protected String getXMLTextElement(String xml, String tag) {
		xml = removeXMLNamespaces(xml);

		if (!tag.startsWith("<")) {
			tag = "<" + tag + ">";
		}

		int start = xml.indexOf(tag);

		if (start == -1) {
			return null;
		}

		int end = xml.indexOf('<', start + tag.length());

		if (end == -1) {
			return null;
		}

		return xml.substring(start + tag.length(), end);
	}

	/**
	 * Removes namespace definitions and prefixes from XML, if any.
	 */
	private String removeXMLNamespaces(String xml) {
		// remove xmlns declaration
		xml = xml.replaceAll("xmlns.*?(\"|\').*?(\"|\')", "");

		// remove opening tag prefix
		xml = xml.replaceAll("(<)(\\w+:)(.*?>)", "$1$3");

		// remove closing tags prefix
		xml = xml.replaceAll("(</)(\\w+:)(.*?>)", "$1$3");

		// remove extra spaces left when xmlns declarations are removed
		xml = xml.replaceAll("\\s+>", ">");

		return xml;
	}

	/**
	 * Asserts that two XML documents are semantically equivalent.  Differences
	 * in whitespace or in namespace usage do not affect the comparison.
	 * @param expected the expected XML
	 * @param actual the XML to test
	 * @throws SAXException
	 * @throws IOException
	 */
    public static void assertXMLEquals(String expected, String actual)
    		throws SAXException, IOException {
    	XMLUnit.setIgnoreWhitespace(true);
    	XMLUnit.setIgnoreAttributeOrder(true);
    	DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(expected, actual));
    	List<?> allDifferences = diff.getAllDifferences();
    	assertEquals("Differences found: " + diff.toString(), 0, allDifferences.size());
    }

	/**
	 * A test implementation of AsynchronousResponse.
	 */
	public class TestAsyncResponse implements AsynchronousResponse {
		Response response = null;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public synchronized void setResponse(Response response) {
			this.response = response;
		}

		/**
		 * Gets the response.
		 * @return the response, or null if none has been produced yet
		 */
		public synchronized Response getResponse() {
			return response;
		}
	}

	/**
	 * An object that contains callback data for a "program".
	 */
	public class CallbackSet {
		private final Map<String, CallbackData> map = new HashMap<>();

		/**
		 * Add untyped callback data to the set.
		 * @param action the action with which the data is associated
		 * @param content the callback data
		 */
		public void put(String action, String content) {
			map.put(action, new CallbackData(null, null, content));
		}

		/**
		 * Add callback data to the set.
		 * @param action the action with which the data is associated
		 * @param messageType the callback message type
		 * @param content the callback data
		 */
		public void put(String action, String messageType, String content) {
			map.put(action, new CallbackData(null, messageType, content));
		}

		/**
		 * Add callback data to the set.
		 * @param action the action with which the data is associated
		 * @param contentType the callback HTTP content type
		 * @param messageType the callback message type
		 * @param content the callback data
		 */
		public void put(String action, String contentType, String messageType, String content) {
			map.put(action, new CallbackData(contentType, messageType, content));
		}

		/**
		 * Retrieve callback data from the set.
		 * @param action the action with which the data is associated
		 * @return the callback data, or null if there is none for the specified operation
		 */
		public CallbackData get(String action) {
			return map.get(action);
		}
	}

	/**
	 * Represents a callback data item.
	 */
	public class CallbackData {
		private final String contentType;
		private final String messageType;
		private final String content;

		/**
		 * Constructor
		 * @param contentType the HTTP content type (optional)
		 * @param messageType the callback message type (optional)
		 * @param content the content
		 */
		public CallbackData(String contentType, String messageType, String content) {
			this.contentType = contentType;
			this.messageType = messageType;
			this.content = content;
		}

		/**
		 * Gets the callback HTTP content type, possibly null.
		 */
		public String getContentType() {
			return contentType;
		}

		/**
		 * Gets the callback message type, possibly null.
		 */
		public String getMessageType() {
			return messageType;
		}

		/**
		 * Gets the callback content.
		 */
		public String getContent() {
			return content;
		}
	}

	/**
	 * A tool for evaluating XPath expressions.
	 */
	protected class XPathTool {
		private final DocumentBuilderFactory factory;
		private final SimpleNamespaceContext context = new SimpleNamespaceContext();
		private final XPath xpath = XPathFactory.newInstance().newXPath();
		private String xml = null;
		private Document doc = null;

		/**
		 * Constructor.
		 */
		public XPathTool() {
			factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			xpath.setNamespaceContext(context);
		}

		/**
		 * Adds a namespace.
		 * @param prefix the namespace prefix
		 * @param uri the namespace uri
		 */
		public synchronized void addNamespace(String prefix, String uri) {
			context.add(prefix, uri);
		}

		/**
		 * Sets the XML content to be operated on.
		 * @param xml the XML content
		 */
		public synchronized void setXML(String xml) {
			this.xml = xml;
			this.doc = null;
		}

		/**
		 * Returns the document object.
		 * @return the document object, or null if XML has not been set
		 * @throws SAXException
		 * @throws IOException
		 * @throws ParserConfigurationException
		 */
		public synchronized Document getDocument()
				throws ParserConfigurationException, IOException, SAXException {
			if (xml == null) {
				return null;
			}

			buildDocument();
			return doc;
		}

		/**
		 * Evaluates the specified XPath expression and returns a string result.
		 * This method throws exceptions on error.
		 * @param expression the expression
		 * @return the result object
		 * @throws ParserConfigurationException
		 * @throws IOException
		 * @throws SAXException
		 * @throws XPathExpressionException on error
		 */
		public synchronized String evaluate(String expression)
				throws ParserConfigurationException, SAXException,
				IOException, XPathExpressionException {
			return (String) evaluate(expression, XPathConstants.STRING);
		}

		/**
		 * Evaluates the specified XPath expression.
		 * This method throws exceptions on error.
		 * @param expression the expression
		 * @param returnType the return type
		 * @return the result object
		 * @throws ParserConfigurationException
		 * @throws IOException
		 * @throws SAXException
		 * @throws XPathExpressionException on error
		 */
		public synchronized Object evaluate(String expression, QName returnType)
				throws ParserConfigurationException, SAXException,
				IOException, XPathExpressionException {

			buildDocument();
			XPathExpression expr = xpath.compile(expression);
			return expr.evaluate(doc, returnType);
		}

		/**
		 * Private helper method that builds the document object.
		 * Assumes the calling method is synchronized.
		 * @throws ParserConfigurationException
		 * @throws IOException
		 * @throws SAXException
		 */
		private void buildDocument() throws ParserConfigurationException,
				IOException, SAXException {
			if (doc == null) {
				if (xml == null) {
					throw new IOException("XML input is null");
				}

				DocumentBuilder builder = factory.newDocumentBuilder();
				InputSource source = new InputSource(new StringReader(xml));
				doc = builder.parse(source);
			}
		}
	}

	/**
	 * A NamespaceContext class based on a Map.
	 */
	private class SimpleNamespaceContext implements NamespaceContext {
		private Map<String, String> prefixMap = new HashMap<>();
		private Map<String, String> uriMap = new HashMap<>();

		public synchronized void add(String prefix, String uri) {
			prefixMap.put(prefix, uri);
			uriMap.put(uri, prefix);
		}

		@Override
		public synchronized String getNamespaceURI(String prefix) {
			return prefixMap.get(prefix);
		}

		@Override
		public Iterator<String> getPrefixes(String uri) {
			List<String> list = new ArrayList<>();
			String prefix = uriMap.get(uri);
			if (prefix != null) {
				list.add(prefix);
			}
			return list.iterator();
		}

		@Override
		public String getPrefix(String uri) {
			return uriMap.get(uri);
		}
	}

	/**
	 * A VnfNotify XPathTool.
	 */
	protected class VnfNotifyXPathTool extends XPathTool {
		public VnfNotifyXPathTool() {
			addNamespace("tns", "http://org.openecomp.mso/vnfNotify");
		}
	}

	/**
	 * Helper class to make it easier to create this type.
	 */
	private static class CreateVnfNotificationOutputs
			extends CreateVnfNotification.Outputs {
		public void add(String key, String value) {
			Entry entry = new Entry();
			entry.setKey(key);
			entry.setValue(value);
			getEntry().add(entry);
		}
	}

	/**
	 * Helper class to make it easier to create this type.
	 */
	private static class UpdateVnfNotificationOutputs
			extends UpdateVnfNotification.Outputs {
		public void add(String key, String value) {
			Entry entry = new Entry();
			entry.setKey(key);
			entry.setValue(value);
			getEntry().add(entry);
		}
	}
}
