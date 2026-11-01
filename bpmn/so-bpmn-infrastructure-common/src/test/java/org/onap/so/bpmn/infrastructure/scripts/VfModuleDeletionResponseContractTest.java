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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;
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
import org.onap.so.bpmn.core.WorkflowException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class VfModuleDeletionResponseContractTest {
    private static final String PREFIX = "DoDVfMod_";
    private final Map<String, Object> variables = new HashMap<>();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final DoDeleteVfModule script = new DoDeleteVfModule();

    @Before
    public void configureStatefulPublicExecution() {
        variables.put("testProcessKey", "DoDeleteVfModule");
        when(execution.getVariable(anyString())).thenAnswer(invocation -> variables.get(invocation.getArgument(0)));
        doAnswer(invocation -> {
            variables.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        script.initProcessVariables(execution);
    }

    @Test
    public void successfulResponsePublishesBooleanCompletionWithoutInventingOutputs() {
        respond("<deleteVfModuleResponse/>");
        assertEquals(Boolean.TRUE, variables.get(PREFIX + "vnfVfModuleDeleteCompleted"));
        assertNull(variables.get(PREFIX + "contrailNetworkPolicyFqdnList"));
        assertNull(variables.get(PREFIX + "oamManagementV4Address"));
        assertNull(variables.get(PREFIX + "oamManagementV6Address"));
        assertNull(variables.get("WorkflowException"));
    }

    @Test
    public void namespacedOutputsRetainPolicyOrderUnicodeAndBothManagementAddresses() {
        respond("<v:deleteVfModuleResponse xmlns:v='urn:adapter'><v:vfModuleOutputs>"
                + entry("first_contrail_network_policy_fqdn", "policy &amp; &lt;\u96ea&gt;")
                + entry("oam_management_v6_address", "2001:db8::5") + entry("ignored", "not-a-policy")
                + entry("second_contrail_network_policy_fqdn", "second-policy")
                + entry("oam_management_v4_address", "192.0.2.5") + "</v:vfModuleOutputs></v:deleteVfModuleResponse>");
        assertEquals(Arrays.asList("policy & <\u96ea>", "second-policy"),
                variables.get(PREFIX + "contrailNetworkPolicyFqdnList"));
        assertEquals("192.0.2.5", variables.get(PREFIX + "oamManagementV4Address"));
        assertEquals("2001:db8::5", variables.get(PREFIX + "oamManagementV6Address"));
        assertEquals(Boolean.TRUE, variables.get(PREFIX + "vnfVfModuleDeleteCompleted"));
    }

    @Test
    public void emptyAndUnrelatedOutputsDoNotCreatePolicyCleanup() {
        respond("<deleteVfModuleResponse><vfModuleOutputs>"
                + "<entry><key>unrelated</key><value>ignored</value></entry>"
                + "</vfModuleOutputs></deleteVfModuleResponse>");
        assertNull(variables.get(PREFIX + "contrailNetworkPolicyFqdnList"));
        assertEquals(Boolean.TRUE, variables.get(PREFIX + "vnfVfModuleDeleteCompleted"));
    }

    @Test
    public void absentResponsePreservesExactEmptyResponseBusinessError() {
        assertRejected(null, "Empty response from VNF Adapter");
    }

    @Test
    public void emptyResponsePreservesExactAdapterError() {
        assertRejected("", "VNF Adapter Error");
    }

    @Test
    public void wrongOperationResponseDoesNotPublishDeleteCompletion() {
        assertRejected("<createVfModuleResponse/>", "VNF Adapter Error");
    }

    @Test
    public void malformedOutputsExposeInternalPostProcessingError() {
        variables.put(PREFIX + "doDeleteVfModuleResponse",
                "<deleteVfModuleResponse><vfModuleOutputs><entry></vfModuleOutputs></deleteVfModuleResponse>");
        BpmnError failure = assertThrows(BpmnError.class, () -> script.postProcessVNFAdapterRequest(execution));
        assertError(failure, "Internal Error Occured in PostProcess Method");
        assertNull(variables.get(PREFIX + "contrailNetworkPolicyFqdnList"));
    }

    @Test
    public void inventoryModuleUpdateUsesEscapedIdentitiesAndDeleteSentinels() throws Exception {
        variables.put("vnfId", "vnf & <\u96ea>");
        variables.put("vfModuleId", "module & <\u96ea>");
        script.prepUpdateAAIVfModule(execution);
        Document document = xml("UpdateAAIVfModuleRequest");
        assertEquals("UpdateAAIVfModuleRequest", document.getDocumentElement().getNodeName());
        assertElement(document, "vnf-id", "vnf & <\u96ea>");
        assertElement(document, "vf-module-id", "module & <\u96ea>");
        assertElement(document, "heat-stack-id", "DELETE");
        assertElement(document, "orchestration-status", "deleted");
    }

    @Test
    public void inventoryModuleDeletionRetainsBothEscapedIdentities() throws Exception {
        variables.put("vnfId", "vnf & <\u96ea>");
        variables.put("vfModuleId", "module & <\u96ea>");
        script.prepDeleteAAIVfModule(execution);
        Document document = xml("DeleteAAIVfModuleRequest");
        assertEquals("DeleteAAIVfModuleRequest", document.getDocumentElement().getNodeName());
        assertElement(document, "vnf-id", "vnf & <\u96ea>");
        assertElement(document, "vf-module-id", "module & <\u96ea>");
        assertEquals(0, document.getElementsByTagName("orchestration-status").getLength());
    }

    @Test
    public void managementCleanupOnlyDeletesAddressesPresentInAdapterOutputs() throws Exception {
        variables.put("vnfId", "vnf & <\u96ea>");
        for (int mask = 0; mask < 4; mask++) {
            variables.put(PREFIX + "oamManagementV4Address", (mask & 1) == 0 ? null : "192.0.2.5");
            variables.put(PREFIX + "oamManagementV6Address", (mask & 2) == 0 ? null : "2001:db8::5");
            script.prepUpdateAAIGenericVnf(execution);
            Document document = xml(PREFIX + "updateAAIGenericVnfRequest");
            assertElement(document, "vnf-id", "vnf & <\u96ea>");
            assertOptionalDeletion(document, "ipv4-oam-address", (mask & 1) != 0);
            assertOptionalDeletion(document, "management-v6-address", (mask & 2) != 0);
        }
    }

    private void respond(String response) {
        variables.put(PREFIX + "doDeleteVfModuleResponse", response);
        script.postProcessVNFAdapterRequest(execution);
    }

    private void assertRejected(String response, String message) {
        BpmnError failure = assertThrows(BpmnError.class, () -> respond(response));
        assertError(failure, message);
        assertFalse(variables.containsKey(PREFIX + "vnfVfModuleDeleteCompleted"));
        assertNull(variables.get(PREFIX + "contrailNetworkPolicyFqdnList"));
    }

    private void assertError(BpmnError failure, String message) {
        assertEquals("1002", failure.getErrorCode());
        assertTrue(variables.get("WorkflowException") instanceof WorkflowException);
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertEquals(1002, error.getErrorCode());
        assertEquals(message, error.getErrorMessage());
        assertEquals("DoDeleteVfModule", error.getProcessKey());
    }

    private static String entry(String key, String value) {
        return "<v:entry><v:key>" + key + "</v:key><v:value>" + value + "</v:value></v:entry>";
    }

    private Document xml(String variable) throws Exception {
        assertTrue(variable, variables.get(variable) instanceof String);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
        factory.setNamespaceAware(true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader((String) variables.get(variable))));
    }

    private static void assertElement(Document document, String name, String value) {
        assertEquals(name, 1, document.getElementsByTagName(name).getLength());
        assertEquals(name, value, document.getElementsByTagName(name).item(0).getTextContent());
    }

    private static void assertOptionalDeletion(Document document, String name, boolean present) {
        assertEquals(name, present ? 1 : 0, document.getElementsByTagName(name).getLength());
        if (present) {
            assertElement(document, name, "DELETE");
        }
    }
}
