/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.onap.so.bpmn.infrastructure.process;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingXPath;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Assume;
import org.onap.so.bpmn.core.WorkflowException;
import org.junit.Before;
import org.junit.Test;
import org.json.JSONObject;
import org.onap.so.bpmn.common.recipe.ResourceInput;
import org.onap.so.BaseBPMNTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.onap.so.bpmn.core.domain.ModelInfo;

public class DeviceResourceProcessTest extends BaseBPMNTest {
    @Autowired
    private ProcessEngine engine;
    private WireMockServer status;

    @Before
    public void verifyProductionDeviceWorkflow() throws Exception {
        String name = "process/CreateDeviceResource.bpmn";
        URL resource = getClass().getClassLoader().getResource(name);
        assertNotNull(resource);
        String location = resource.toExternalForm();
        assertTrue(location,
                location.contains("/so-bpmn-infrastructure-flows/target/classes/")
                        || (location.matches("jar:.*/so-bpmn-infrastructure-flows-[^/]+\\.jar!/" + name)
                                && !location.contains("-tests.jar!/")));
        var definition = engine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionKey("CreateDeviceResource").singleResult();
        try (InputStream original = resource.openStream();
                InputStream deployed = engine.getRepositoryService().getResourceAsStream(definition.getDeploymentId(),
                        definition.getResourceName())) {
            assertArrayEquals(original.readAllBytes(), deployed.readAllBytes());
        }
        status = new WireMockServer(0);
        status.start();
        status.stubFor(post(urlEqualTo("/status")).willReturn(aResponse().withStatus(200).withBody("<ok/>")));
    }

    @After
    public void stopStatusBoundary() {
        if (status != null) {
            status.stop();
        }
    }

    @Test
    public void routerDeviceCompletesThroughTheProductionDefaultRoute() throws Exception {
        assertDefaultRoute("ROUTER", "ROUTER");
    }

    @Test
    public void blankDeviceClassDefaultsToOtherBeforeTheStatusUpdate() throws Exception {
        assertDefaultRoute(" ", "OTHER");
    }

    @Test
    public void anEmptySuccessfulStatusResponseStillCompletesTheDefaultRoute() throws Exception {
        Assume.assumeTrue("SO-4457: a successful empty device status response must complete the workflow",
                Boolean.getBoolean("groovy.migration.verification"));
        status.stubFor(post(urlEqualTo("/status")).willReturn(aResponse().withStatus(204)));
        assertDefaultRoute("OTHER", "OTHER", 204);
    }

    @Test
    public void rejectedStatusWritesMustNotReportSuccessfulDeviceCompletion() throws Exception {
        Assume.assumeTrue("SO-4457: rejected device status writes must fail the workflow",
                Boolean.getBoolean("groovy.migration.verification"));
        List<Integer> silentlyAccepted = new ArrayList<>();
        for (int code : new int[] {400, 500}) {
            status.resetAll();
            status.stubFor(post(urlEqualTo("/status")).willReturn(aResponse().withStatus(code).withBody("rejected")));
            ProcessInstance instance = startDevice("ROUTER");
            status.verify(1, postRequestedFor(urlEqualTo("/status")));
            assertEquals(Integer.valueOf(code), historic(instance, "CVFMI_dbResponseCode"));
            var error = engine.getHistoryService().createHistoricVariableInstanceQuery()
                    .processInstanceId(instance.getId()).variableName("WorkflowException").singleResult();
            if (error == null || !(error.getValue() instanceof WorkflowException)) {
                silentlyAccepted.add(code);
            }
        }
        assertTrue("Status writes silently accepted: " + silentlyAccepted, silentlyAccepted.isEmpty());
    }

    private ProcessInstance startDevice(String requestedClass) throws Exception {
        ModelInfo model = new ModelInfo();
        model.setModelInvariantUuid("invariant-1");
        model.setModelUuid("model-2");
        model.setModelCustomizationUuid("customization-3");
        model.setModelName("device-model");
        ResourceInput resource = new ResourceInput();
        resource.setResourceModelInfo(model);
        resource.setResourceInstanceName("router-1");
        resource.setServiceInstanceId("service-1");
        resource.setOperationId("operation-1");
        resource.setOperationType("CREATE");
        resource.setResourceParameters(new JSONObject()
                .put("requestInputs",
                        new JSONObject().put("device_class", requestedClass).put("enabled", false).put("count", 0))
                .toString());
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", "request-1");
        variables.put("resourceInput", new ObjectMapper().writeValueAsString(resource));
        variables.put("mso.adapters.openecomp.db.endpoint", "http://localhost:" + status.port() + "/status");
        return engine.getRuntimeService().startProcessInstanceByKey("CreateDeviceResource", variables);
    }

    @Test
    public void persistedDeviceInputsRetainFalseAndZeroValues() throws Exception {
        Assume.assumeTrue("SO-4457: device request values must survive process persistence",
                Boolean.getBoolean("groovy.migration.verification"));
        ProcessInstance instance = startDevice("ROUTER");
        Object restored = historic(instance, "CREDEVRES_ResourceRequestInputs");
        assertNotNull("Persisted device request inputs", restored);
        JSONObject inputs = new JSONObject(restored.toString());
        assertTrue("Persisted input representation (" + restored.getClass().getName() + "): " + restored,
                inputs.has("enabled") && inputs.has("count"));
        assertEquals(Boolean.FALSE, inputs.get("enabled"));
        assertEquals(Integer.valueOf(0), inputs.get("count"));
    }

    private void assertDefaultRoute(String requestedClass, String expectedClass) throws Exception {
        assertDefaultRoute(requestedClass, expectedClass, 200);
    }

    private void assertDefaultRoute(String requestedClass, String expectedClass, int responseCode) throws Exception {
        ProcessInstance instance = startDevice(requestedClass);
        assertNull(engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(instance.getId())
                .singleResult());
        assertNotNull(engine.getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(instance.getId()).finished().singleResult());
        assertEquals(expectedClass, historic(instance, "device_class"));
        assertEquals("finished", historic(instance, "status"));
        assertEquals("100", historic(instance, "progress"));
        assertEquals(Integer.valueOf(responseCode), historic(instance, "CVFMI_dbResponseCode"));
        assertTrue(historic(instance, "CREDEVRES_ResourceInput") instanceof ResourceInput);
        assertEquals(0, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(instance.getId()).activityId("Task_0bga3e8").count());
        for (String activity : new String[] {"ScriptTask_14dav1d", "ScriptTask_00y93jj", "ScriptTask_0p4b5vu",
                "ServiceTask_08e6hpm"}) {
            assertEquals(activity, 1, engine.getHistoryService().createHistoricActivityInstanceQuery()
                    .processInstanceId(instance.getId()).activityId(activity).finished().count());
        }
        status.verify(1,
                postRequestedFor(urlEqualTo("/status")).withHeader("content-type", containing("application/soap+xml"))
                        .withRequestBody(matchingXPath("//*[local-name()='operationId' and text()='operation-1']"))
                        .withRequestBody(matchingXPath("//*[local-name()='serviceId' and text()='service-1']"))
                        .withRequestBody(matchingXPath("//*[local-name()='status' and text()='finished']")));
    }

    private Object historic(ProcessInstance instance, String name) {
        var value = engine.getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(instance.getId())
                .variableName(name).singleResult();
        assertNotNull(name, value);
        return value.getValue();
    }
}
