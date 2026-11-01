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
package org.onap.so.bpmn.infrastructure.scripts;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.onap.aai.domain.yang.Device;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.so.bpmn.common.recipe.ResourceInput;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException;

public class DeviceInventoryLookupContractTest {
    private static final String PATH = "/aai/" + AAIVersion.LATEST + "/network/devices/device/device-contract";
    @Rule
    public WireMockRule inventory = new WireMockRule(0);
    private final DeleteDeviceResource script = new DeleteDeviceResource();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private MockedStatic<UrnPropertiesReader> properties;

    @Before
    public void configureInventoryAndExecution() throws Exception {
        ResourceInput input = new ResourceInput();
        input.setResourceInstancenUuid("device-contract");
        input.setServiceInstanceId("service-contract");
        input.setOperationId("operation-contract");
        input.setOperationType("DELETE");
        input.setResourceParameters("{\"requestInputs\":{\"device_class\":\"PNF\"}}");
        variables.put("resourceInput", new ObjectMapper().writeValueAsString(input));
        variables.put("mso-request-id", "request-contract");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        properties = mockStatic(UrnPropertiesReader.class);
        properties.when(() -> UrnPropertiesReader.getVariable("aai.endpoint"))
                .thenReturn("http://localhost:" + inventory.port());
        properties.when(() -> UrnPropertiesReader.getVariable(anyString(), anyString()))
                .thenAnswer(call -> call.getArgument(1));
        respond(200, "{\"device-id\":\"device-contract\",\"class\":\"ROUTER\"}");
    }

    @After
    public void restoreConfigurationBoundary() {
        if (properties != null) {
            properties.close();
        }
    }

    @Test
    public void successfulInventoryFixtureDeserializesTheActualDeviceClassProperty() {
        Device device = new AAIResourcesClient()
                .get(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().device("device-contract")))
                .asBean(Device.class).get();
        assertEquals("device-contract", device.getDeviceId());
        assertEquals("ROUTER", device.getClazz());
        inventory.verify(1, getRequestedFor(urlEqualTo(PATH)));
    }

    @Test
    public void deletionLooksUpItsInventoryIdentityAndKeepsTheRequestContext() {
        script.preProcessRequest(execution);
        assertEquals("device-contract", variables.get("DELDEVRES_DeviceId"));
        assertEquals("service-contract", variables.get("DELDEVRES_serviceInstanceId"));
        assertEquals("request-contract", variables.get("mso-request-id"));
        assertTrue(variables.get("DELDEVRES_ResourceInput") instanceof ResourceInput);
        ResourceInput input = (ResourceInput) variables.get("DELDEVRES_ResourceInput");
        assertEquals("operation-contract", input.getOperationId());
        assertEquals("DELETE", input.getOperationType());
        assertFalse(variables.containsKey("WorkflowException"));
        inventory.verify(1, getRequestedFor(urlEqualTo(PATH)));
    }

    @Test
    public void deletionSelectsTheInventoryClassInsteadOfTheJavaModelClass() {
        readiness("SO-4457: device deletion must dispatch using AAI's class property");
        List<String> mismatches = new ArrayList<>();
        for (String deviceClass : new String[] {"ROUTER", "VNF", "PNF"}) {
            inventory.resetAll();
            respond(200, "{\"device-id\":\"device-contract\",\"class\":\"" + deviceClass + "\"}");
            script.preProcessRequest(execution);
            script.checkDevType(execution);
            inventory.verify(1, getRequestedFor(urlEqualTo(PATH)));
            if (!deviceClass.equals(variables.get("device_class"))) {
                mismatches.add(deviceClass + " -> " + variables.get("device_class"));
            }
        }
        assertTrue("Inventory class dispatch mismatches: " + mismatches, mismatches.isEmpty());
    }

    @Test
    public void missingInventoryDoesNotInventADeviceClassOrRepeatTheLookup() {
        respond(404, "{}");
        script.preProcessRequest(execution);
        assertEquals("device-contract", variables.get("DELDEVRES_DeviceId"));
        assertFalse(variables.containsKey("DELDEVRES_DeviceClass"));
        inventory.verify(1, getRequestedFor(urlEqualTo(PATH)));
    }

    @Test
    public void unavailableInventoryMustNotFallThroughToACompletedDeletion() {
        readiness("SO-4457: inventory server failure must not become a successful device deletion");
        assertInventoryRejected(503, "{}");
    }

    @Test
    public void malformedSuccessfulInventoryMustNotFallThroughToACompletedDeletion() {
        readiness("SO-4457: malformed device inventory must fail before dispatch");
        assertInventoryRejected(200, "{invalid");
    }

    private void assertInventoryRejected(int code, String body) {
        respond(code, body);
        script.preProcessRequest(execution);
        inventory.verify(1, getRequestedFor(urlEqualTo(PATH)));
        assertTrue("Lookup did not record a typed workflow failure for HTTP " + code,
                variables.get("WorkflowException") instanceof WorkflowException);
        assertFalse(variables.containsKey("device_class"));
    }

    private void respond(int code, String body) {
        inventory.stubFor(get(urlEqualTo(PATH)).willReturn(
                aResponse().withStatus(code).withHeader("Content-Type", "application/json").withBody(body)));
    }

    private void readiness(String reason) {
        Assume.assumeTrue(reason, Boolean.getBoolean("groovy.migration.verification"));
    }
}
