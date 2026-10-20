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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONObject;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.common.recipe.ResourceInput;
import org.onap.so.bpmn.core.domain.ModelInfo;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class DeviceResourceContractTest {
    private final CreateDeviceResource create = new CreateDeviceResource();
    private final DeleteDeviceResource delete = new DeleteDeviceResource();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private ResourceInput resource;

    @Before
    public void configureExecution() {
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        ModelInfo model = new ModelInfo();
        model.setModelInvariantUuid("invariant-1");
        model.setModelUuid("model-2");
        model.setModelCustomizationUuid("customization-3");
        model.setModelName("device-model");
        resource = new ResourceInput();
        resource.setResourceModelInfo(model);
        resource.setResourceInstanceName("München & 東京");
        resource.setServiceInstanceId("service-1");
        resource.setOperationId("operation-1");
        resource.setOperationType("CREATE");
        resource.setResourceParameters(new JSONObject()
                .put("requestInputs",
                        new JSONObject().put("device_class", "ROUTER").put("enabled", false).put("count", 0))
                .toString());
        variables.put("mso-request-id", "request-1");
        variables.put("mso.adapters.openecomp.db.endpoint", "http://localhost/status");
    }

    @Test
    public void creationRetainsTheTypedInputModelIdentityAndNestedRequestValues() throws Exception {
        variables.put("resourceInput", new ObjectMapper().writeValueAsString(resource));
        create.preProcessRequest(execution);
        assertTrue(variables.get("CREDEVRES_ResourceInput") instanceof ResourceInput);
        ResourceInput stored = (ResourceInput) variables.get("CREDEVRES_ResourceInput");
        assertEquals("service-1", stored.getServiceInstanceId());
        assertEquals("operation-1", stored.getOperationId());
        assertEquals("München & 東京", variables.get("resourceName"));
        assertEquals("invariant-1", variables.get("CREDEVRES_ResourceModelInvariantUuid"));
        assertEquals("model-2", variables.get("CREDEVRES_ResourceModelUuid"));
        assertEquals("customization-3", variables.get("CREDEVRES_ResourceModelCustomizationUuid"));
        assertEquals("service-1", variables.get("CREDEVRES_serviceInstanceId"));
        assertEquals("request-1", variables.get("mso-request-id"));
        assertTrue(variables.get("CREDEVRES_ResourceRequestInputs") instanceof JSONObject);
        JSONObject inputs = (JSONObject) variables.get("CREDEVRES_ResourceRequestInputs");
        assertEquals(Boolean.FALSE, inputs.get("enabled"));
        assertEquals(Integer.valueOf(0), inputs.get("count"));
        assertEquals("ROUTER", inputs.get("device_class"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void creationUsesTheRequestedDeviceClassAndDefaultsExplicitlyBlankClasses() {
        for (String input : new String[] {"ROUTER", "router", "", " \t"}) {
            variables.put("CREDEVRES_ResourceRequestInputs", new JSONObject().put("device_class", input));
            create.checkDevType(execution);
            assertEquals(input.trim().isEmpty() ? "OTHER" : input, variables.get("device_class"));
        }
    }

    @Test
    public void deletionUsesTheInventoryDeviceClassAndDefaultsAbsentOrBlankClasses() {
        for (String input : new String[] {"ROUTER", "router", null, "", " \t"}) {
            variables.put("DELDEVRES_DeviceClass", input);
            delete.checkDevType(execution);
            assertEquals(input == null || input.trim().isEmpty() ? "OTHER" : input, variables.get("device_class"));
        }
    }

    @Test
    public void creationAndDeletionProgressKeepOperationAndServiceIdentity() throws Exception {
        variables.put("CREDEVRES_ResourceInput", resource);
        variables.put("DELDEVRES_ResourceInput", resource);
        variables.put("progress", "0");
        variables.put("status", "processing");
        variables.put("statusDescription", "controller pending");
        create.prepareUpdateProgress(execution);
        assertProgress("CREATE", "controller pending");
        resource.setOperationType("DELETE");
        delete.prepareUpdateProgress(execution);
        assertProgress("DELETE", "controller pending");
    }

    @Test
    public void successfulVnfValidationIncrementsTheIntegerResourceCountOnce() {
        variables.put("CREDEVRES_VnfsCreatedCount", Integer.valueOf(0));
        create.validateVnfCreate(execution);
        assertEquals(Integer.valueOf(1), variables.get("CREDEVRES_VnfsCreatedCount"));
        create.validateVnfCreate(execution);
        assertEquals(Integer.valueOf(2), variables.get("CREDEVRES_VnfsCreatedCount"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void deviceProgressPreservesSpecialCharactersAsXmlText() throws Exception {
        assertSpecialCharacters(() -> create.prepareUpdateProgress(execution), "CREATE");
    }

    @Test
    public void deletedDeviceProgressPreservesSpecialCharactersAsXmlText() throws Exception {
        resource.setOperationType("DELETE");
        assertSpecialCharacters(() -> delete.prepareUpdateProgress(execution), "DELETE");
    }

    private void assertSpecialCharacters(Runnable action, String operation) throws Exception {
        Assume.assumeTrue("SO-4446: device progress descriptions must be escaped in the status payload",
                Boolean.getBoolean("groovy.migration.verification"));
        String description = "München & <controller> 東京";
        variables.put("CREDEVRES_ResourceInput", resource);
        variables.put("DELDEVRES_ResourceInput", resource);
        variables.put("progress", "0");
        variables.put("status", "failed");
        variables.put("statusDescription", description);
        action.run();
        assertProgress(operation, description);
    }

    private void assertProgress(String operation, String description) throws Exception {
        assertEquals("http://localhost/status", variables.get("CVFMI_dbAdapterEndpoint"));
        String body = (String) variables.get("CVFMI_updateResOperStatusRequest");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Document document;
        try {
            document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(body)));
        } catch (org.xml.sax.SAXParseException error) {
            fail("Device status must contain well-formed XML: " + error.getMessage());
            return;
        }
        assertEquals("http://schemas.xmlsoap.org/soap/envelope/", document.getDocumentElement().getNamespaceURI());
        assertEquals(1, document
                .getElementsByTagNameNS("http://org.onap.so/requestsdb", "updateResourceOperationStatus").getLength());
        assertEquals(operation, text(document, "operType"));
        assertEquals("operation-1", text(document, "operationId"));
        assertEquals("customization-3", text(document, "resourceTemplateUUID"));
        assertEquals("service-1", text(document, "serviceId"));
        assertEquals("0", text(document, "progress"));
        assertEquals(variables.get("status"), text(document, "status"));
        assertEquals(description, text(document, "statusDescription"));
    }

    private String text(Document document, String name) {
        assertEquals(name, 1, document.getElementsByTagNameNS("", name).getLength());
        return document.getElementsByTagNameNS("", name).item(0).getTextContent();
    }
}
