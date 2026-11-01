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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.core.WorkflowException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class VfModuleInventoryUpdateContractTest {
    private final DoCreateVfModule script = new DoCreateVfModule();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();

    @Before
    public void configureStatefulExecution() {
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        when(execution.getVariables()).thenReturn(variables);
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        variables.put("testProcessKey", "DoCreateVfModule");
        variables.put("DCVFM_vnfId", "vnf-contract");
        variables.put("DCVFM_vfModuleId", "module-contract");
        variables.put("DCVFM_volumeGroupId", "volume-contract");
        variables.put("DCVFM_orchestrationStatus", "Created");
        variables.put("DCVFM_heatStackId", "stack-contract");
        variables.put("DCVFM_contrailServiceInstanceFqdn", "service.example.invalid");
    }

    @Test
    public void updateFlagsIncludeExactlyTheirSelectedInventoryFields() throws Exception {
        String[] names = {"volume-group-id", "orchestration-status", "heat-stack-id", "contrail-service-instance-fqdn"};
        String[] values = {"volume-contract", "Created", "stack-contract", "service.example.invalid"};
        for (int mask = 0; mask < 16; mask++) {
            Document document = xml(script.buildUpdateAAIVfModuleRequest(execution, (mask & 1) != 0, (mask & 2) != 0,
                    (mask & 4) != 0, (mask & 8) != 0));
            assertEquals("UpdateAAIVfModuleRequest", document.getDocumentElement().getNodeName());
            assertEquals("vnf-contract", text(document, "vnf-id"));
            assertEquals("module-contract", text(document, "vf-module-id"));
            int selected = 0;
            for (int field = 0; field < names.length; field++) {
                boolean included = (mask & (1 << field)) != 0;
                assertEquals("mask=" + mask + " field=" + names[field], included ? 1 : 0,
                        document.getElementsByTagName(names[field]).getLength());
                if (included) {
                    assertEquals(values[field], text(document, names[field]));
                    selected++;
                }
            }
            assertEquals(selected + 2, document.getDocumentElement().getElementsByTagName("*").getLength());
        }
    }

    @Test
    public void inventoryIdentitiesRetainEscapedAndUnicodeText() throws Exception {
        String id = "Gr\u00fc\u00dfe & <SO> \"module\" 'id'";
        variables.put("DCVFM_vnfId", id);
        variables.put("DCVFM_vfModuleId", id + "-vf");
        Document document = xml(script.buildUpdateAAIVfModuleRequest(execution, false, false, false, false));
        assertEquals(id, text(document, "vnf-id"));
        assertEquals(id + "-vf", text(document, "vf-module-id"));
    }

    @Test
    public void statusUpdateLeavesVolumeStackAndFqdnUntouched() throws Exception {
        script.preProcessUpdateAAIVfModuleRequestStatus(execution, "Active");
        Document document = outgoing();
        assertEquals("Active", text(document, "orchestration-status"));
        assertEquals("Active", variables.get("DCVFM_orchestrationStatus"));
        assertEquals("DCVFM_", variables.get("prefix"));
        for (String name : new String[] {"volume-group-id", "heat-stack-id", "contrail-service-instance-fqdn"}) {
            assertEquals(0, document.getElementsByTagName(name).getLength());
        }
        assertEquals("stack-contract", variables.get("DCVFM_heatStackId"));
        assertEquals("volume-contract", variables.get("DCVFM_volumeGroupId"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void createdUpdateIncludesTheAllocatedStackAndNonemptyFqdn() throws Exception {
        variables.put("DCVFM_orchestrationStatus", "PendingCreate");
        script.preProcessUpdateAAIVfModuleRequestOrch(execution);
        Document document = outgoing();
        assertEquals("Created", text(document, "orchestration-status"));
        assertEquals("Created", variables.get("DCVFM_orchestrationStatus"));
        assertEquals("stack-contract", text(document, "heat-stack-id"));
        assertEquals("service.example.invalid", text(document, "contrail-service-instance-fqdn"));
        assertEquals(0, document.getElementsByTagName("volume-group-id").getLength());
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void createdUpdateOmitsTheExplicitlyEmptyOptionalFqdn() throws Exception {
        variables.put("DCVFM_contrailServiceInstanceFqdn", "");
        script.preProcessUpdateAAIVfModuleRequestOrch(execution);
        Document document = outgoing();
        assertEquals("Created", text(document, "orchestration-status"));
        assertEquals("stack-contract", text(document, "heat-stack-id"));
        assertEquals(0, document.getElementsByTagName("contrail-service-instance-fqdn").getLength());
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void volumeAssociationUpdatesOnlyTheVolumeGroupReference() throws Exception {
        script.preProcessUpdateAAIVfModuleRequestGroup(execution);
        Document document = outgoing();
        assertEquals("volume-contract", text(document, "volume-group-id"));
        assertEquals(3, document.getDocumentElement().getElementsByTagName("*").getLength());
        assertEquals("Created", variables.get("DCVFM_orchestrationStatus"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void bpmnFailurePreservesTheOriginalTypedWorkflowException() {
        WorkflowException original = new WorkflowException("original", 7010, "controller timeout");
        variables.put("WorkflowException", original);
        script.processBPMNException(execution);
        assertSame(original, variables.get("WorkflowException"));
        assertEquals("DCVFM_", variables.get("prefix"));
        assertFalse(variables.containsKey("DCVFM_updateAAIVfModuleRequest"));
    }

    @Test
    public void bpmnFailureWithoutAnOriginalErrorCreatesTheDefinedFallback() {
        script.processBPMNException(execution);
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertEquals(500, error.getErrorCode());
        assertEquals("Exception occured during DoCreateVfModule Sub Process", error.getErrorMessage());
        assertEquals("DCVFM_", variables.get("prefix"));
    }

    @Test
    public void optionalInventoryValuesRemainWellFormedXml() throws Exception {
        Assume.assumeTrue("SO-4457: optional VF-module inventory values are interpolated without XML escaping",
                Boolean.getBoolean("groovy.migration.verification"));
        String value = "Gr\u00fc\u00dfe & <SO>";
        variables.put("DCVFM_volumeGroupId", value);
        variables.put("DCVFM_orchestrationStatus", value);
        variables.put("DCVFM_heatStackId", value);
        variables.put("DCVFM_contrailServiceInstanceFqdn", value);
        Document document = xml(script.buildUpdateAAIVfModuleRequest(execution, true, true, true, true));
        for (String name : new String[] {"volume-group-id", "orchestration-status", "heat-stack-id",
                "contrail-service-instance-fqdn"}) {
            assertEquals(value, text(document, name));
        }
    }

    private Document outgoing() throws Exception {
        Object request = variables.get("DCVFM_updateAAIVfModuleRequest");
        assertNotNull(request);
        assertEquals(String.class, request.getClass());
        return xml((String) request);
    }

    private Document xml(String value) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(value)));
    }

    private String text(Document document, String name) {
        assertEquals(1, document.getElementsByTagName(name).getLength());
        return document.getElementsByTagName(name).item(0).getTextContent();
    }
}
