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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.domain.yang.VfModule;
import org.onap.so.bpmn.core.WorkflowException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class VfModuleDeletionRequestContractTest {
    private static final String ADAPTER = "http://org.onap/workflow/sdnc/adapter/schema/v1";
    private static final String WORKFLOW = "http://org.onap/so/workflow/schema/v1";
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final DoDeleteVfModule script = new DoDeleteVfModule();

    @Before
    public void configureDistinctRequestIdentitiesAndCallbackEndpoints() {
        variables.put("testProcessKey", "DoDeleteVfModule");
        variables.put("requestId", "request-1");
        variables.put("serviceInstanceId", "adapter-service & <\u96ea>");
        variables.put("srvInstId", "unrelated-legacy-service");
        variables.put("cloudSiteId", "region & <\u96ea>");
        variables.put("cloudOwner", "owner & <\u96ea>");
        variables.put("tenantId", "tenant & <\u96ea>");
        variables.put("vnfId", "generic-vnf & <\u96ea>");
        variables.put("vfModuleId", "module & <\u96ea>");
        variables.put("DoDVfMod_heatStackId", "stack & <\u96ea>");
        variables.put("mso.workflow.message.endpoint", "http://localhost/messages///");
        variables.put("mso.use.qualified.host", "false");
        variables.put("mso.workflow.sdncadapter.callback", "http://localhost/sdnc?a=1&b=\u96ea");
        variables.put("testReqId", "wire & <\u96ea>");
        variables.put("source", "source & <\u96ea>");
        variables.put("serviceId", "service-model & <\u96ea>");
        variables.put("DoDVfMod_serviceInstanceIdToSdnc", "controller-service & <\u96ea>");
        variables.put("vfModuleName", "requested-name & <\u96ea>");
        variables.put("vfModuleModelName", "module-model & <\u96ea>");
        variables.put("retainResources", false);
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void adapterDeletionKeepsEscapedIdentitiesAndCorrelatesTheCallback() throws Exception {
        script.prepVNFAdapterRequest(execution);
        Document payload = xml("vnfAdapterTaskRequest");
        assertEquals("deleteVfModuleRequest", payload.getDocumentElement().getLocalName());
        field(payload, "cloudSiteId", "region & <\u96ea>");
        field(payload, "cloudOwner", "owner & <\u96ea>");
        field(payload, "tenantId", "tenant & <\u96ea>");
        field(payload, "vnfId", "generic-vnf & <\u96ea>");
        field(payload, "vfModuleId", "module & <\u96ea>");
        field(payload, "vfModuleStackId", "stack & <\u96ea>");
        field(payload, "skipAAI", "true");
        field(payload, "requestId", "request-1");
        field(payload, "serviceInstanceId", "adapter-service & <\u96ea>");
        String correlation = value(payload, "messageId");
        assertTrue(correlation, correlation.matches("request-1-[0-9]+"));
        field(payload, "notificationUrl", "http://localhost/messages/VNFAResponse/" + correlation);
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void missingCallbackEndpointRaisesExactErrorBeforePublishingAnAdapterRequest() {
        for (String endpoint : Arrays.asList(null, "")) {
            variables.put("mso.workflow.message.endpoint", endpoint);
            BpmnError failure = assertThrows(BpmnError.class, () -> script.prepVNFAdapterRequest(execution));
            assertEquals("2000", failure.getErrorCode());
            assertTrue(variables.get("WorkflowException") instanceof WorkflowException);
            WorkflowException error = (WorkflowException) variables.get("WorkflowException");
            assertEquals("DoDeleteVfModule", error.getProcessKey());
            assertEquals(2000, error.getErrorCode());
            assertEquals("mso:workflow:message:endpoint URN mapping is not set", error.getErrorMessage());
            assertFalse(variables.containsKey("vnfAdapterTaskRequest"));
        }
    }

    @Test
    public void sdncDeletionAndReleaseKeepHeaderModuleGenericVnfAndServiceIdentitiesDistinct() throws Exception {
        for (String action : Arrays.asList("delete", "release")) {
            script.prepSDNCAdapterRequest(execution, action);
            Document payload = xml("sdncAdapterWorkflowRequest");
            assertEquals(WORKFLOW, payload.getDocumentElement().getNamespaceURI());
            assertEquals("SDNCAdapterWorkflowRequest", payload.getDocumentElement().getLocalName());
            assertEquals("wire & <\u96ea>", value(payload, ADAPTER, "RequestId"));
            assertEquals("module & <\u96ea>", value(payload, ADAPTER, "SvcInstanceId"));
            assertEquals(action, value(payload, ADAPTER, "SvcAction"));
            assertEquals("vnf-topology-operation", value(payload, ADAPTER, "SvcOperation"));
            assertEquals("http://localhost/sdnc?a=1&b=\u96ea", value(payload, ADAPTER, "CallbackUrl"));
            field(payload, "request-id", "request-1");
            field(payload, "request-action", "DisconnectVNFRequest");
            field(payload, "source", "source & <\u96ea>");
            field(payload, "service-id", "service-model & <\u96ea>");
            field(payload, "service-type", "service-model & <\u96ea>");
            field(payload, "service-instance-id", "controller-service & <\u96ea>");
            field(payload, "vnf-id", "module & <\u96ea>");
            field(payload, "vnf-type", "module-model & <\u96ea>");
            field(payload, "vnf-name", "requested-name & <\u96ea>");
            field(payload, "generic-vnf-id", "generic-vnf & <\u96ea>");
            field(payload, "aic-cloud-region", "region & <\u96ea>");
            field(payload, "tenant", "tenant & <\u96ea>");
        }
    }

    @Test
    public void onlyRetainedResourcesEmitTheRetentionSubAction() throws Exception {
        for (boolean retain : new boolean[] {false, true}) {
            variables.put("retainResources", retain);
            script.prepSDNCAdapterRequest(execution, "delete");
            Document payload = xml("sdncAdapterWorkflowRequest");
            assertEquals(retain ? 1 : 0, payload.getElementsByTagName("request-sub-action").getLength());
            if (retain) {
                field(payload, "request-sub-action", "RetainResource");
            }
        }
    }

    @Test
    public void absentOrEmptyRequestedNameFallsBackToTheInventoryModule() throws Exception {
        VfModule inventory = new VfModule();
        inventory.setVfModuleName("inventory-name & <\u96ea>");
        variables.put("vfModuleFromAAI", inventory);
        for (String requested : Arrays.asList(null, "")) {
            variables.put("vfModuleName", requested);
            script.prepSDNCAdapterRequest(execution, "delete");
            field(xml("sdncAdapterWorkflowRequest"), "vnf-name", "inventory-name & <\u96ea>");
        }
    }

    @Test
    public void explicitRequestedNameTakesPrecedenceOverInventory() throws Exception {
        VfModule inventory = new VfModule();
        inventory.setVfModuleName("different-inventory-name");
        variables.put("vfModuleFromAAI", inventory);
        script.prepSDNCAdapterRequest(execution, "delete");
        field(xml("sdncAdapterWorkflowRequest"), "vnf-name", "requested-name & <\u96ea>");
    }

    @Test
    public void generatedSdncRequestIdKeepsTheBusinessRequestPrefix() throws Exception {
        variables.remove("testReqId");
        script.prepSDNCAdapterRequest(execution, "delete");
        Document payload = xml("sdncAdapterWorkflowRequest");
        String header = value(payload, ADAPTER, "RequestId");
        assertTrue(header, header.matches("request-1-[0-9]+"));
        field(payload, "request-id", "request-1");
    }

    private Document xml(String variable) throws Exception {
        assertTrue(variable, variables.get(variable) instanceof String);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
        factory.setNamespaceAware(true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader((String) variables.get(variable))));
    }

    private static String value(Document document, String namespace, String name) {
        assertEquals(name, 1, document.getElementsByTagNameNS(namespace, name).getLength());
        return document.getElementsByTagNameNS(namespace, name).item(0).getTextContent();
    }

    private static String value(Document document, String name) {
        return value(document, "", name);
    }

    private static void field(Document document, String name, String expected) {
        assertEquals(name, expected, value(document, name));
    }
}
