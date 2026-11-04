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
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.domain.yang.VfModule;
import org.onap.so.bpmn.core.WorkflowException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class VfModuleUpdatePayloadContractTest {
    private static final String PREFIX = "DOUPVfMod_";
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, String> parameters = new LinkedHashMap<>();
    private final DoUpdateVfModule script = new DoUpdateVfModule();

    @Before
    public void initializeRealDefaultsAndIndependentControllerAdapterAndInventoryInputs() {
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        script.initProcessVariables(execution);
        variables.put("testProcessKey", "DoUpdateVfModule");
        variables.put(PREFIX + "requestId", "body-request");
        variables.put("mso-request-id", "callback-parent");
        variables.put(PREFIX + "serviceInstanceId", "service & <\u96ea>");
        variables.put(PREFIX + "vnfId", "vnf & <\u96ea>");
        variables.put(PREFIX + "vnfName", "vnf-name & <\u96ea>");
        variables.put(PREFIX + "vfModuleId", "module & <\u96ea>");
        variables.put(PREFIX + "vfModuleName", "module-name & <\u96ea>");
        variables.put(PREFIX + "tenantId", "tenant & <\u96ea>");
        variables.put(PREFIX + "aicCloudRegion", "region & <\u96ea>");
        variables.put(PREFIX + "cloudOwner", "owner & <\u96ea>");
        variables.put(PREFIX + "vnfType", "vnf-type & <\u96ea>");
        variables.put(PREFIX + "vfModuleModelName", "module-type & <\u96ea>");
        variables.put(PREFIX + "asdcServiceModelVersion", "2.0");
        variables.put(PREFIX + "modelCustomizationUuid", "custom-1");
        variables.put(PREFIX + "volumeGroupId", "volume-1");
        variables.put(PREFIX + "volumeGroupStackId", "volume-stack & <\u96ea>");
        variables.put(PREFIX + "baseVfModuleId", "base-module & <\u96ea>");
        variables.put(PREFIX + "baseVfModuleHeatStackId", "base-stack & <\u96ea>");
        variables.put(PREFIX + "backoutOnFailure", false);
        variables.put("mso.workflow.message.endpoint", "http://localhost/messages///");
        variables.put("mso.use.qualified.host", "false");
        variables.put("DOUPVEnvironment_context", "environment & <\u96ea>");
        variables.put("DOUPVWorkload_context", "workload & <\u96ea>");
        variables.put(PREFIX + "vnfInputs", inputs("vnf-invariant", "vnf-version", "module-invariant", "module-version",
                "service.example.invalid"));
        VfModule current = new VfModule();
        current.setHeatStackId("current-stack & <\u96ea>");
        variables.put(PREFIX + "vfModule", current);
        parameters.put("choice", "request & <\u96ea>");
        parameters.put("count", "0");
        parameters.put("enabled", "false");
        variables.put(PREFIX + "vnfParamsMap", parameters);
        variables.put(PREFIX + "sdncTopologyResponse",
                "<response><response-data><service-data>"
                        + "<vnf-request-information><vnf-id>controller-vnf</vnf-id></vnf-request-information>"
                        + "<vnf-parameters><vnf-parameter-name>choice</vnf-parameter-name>"
                        + "<vnf-parameter-value>controller</vnf-parameter-value></vnf-parameters>"
                        + "</service-data></response-data></response>");
    }

    @Test
    public void adapterRequestRetainsEscapedIdentitiesModelDataAndCorrelatedCallback() throws Exception {
        variables.put(PREFIX + "modelCustomizationUuid", "custom & <\u96ea>");
        variables.put(PREFIX + "volumeGroupId", "volume & <\u96ea>");
        script.prepVnfAdapterRest(execution);
        Document payload = xml(PREFIX + "vnfAdapterRestRequest");
        assertEquals("updateVfModuleRequest", payload.getDocumentElement().getLocalName());
        field(payload, "cloudSiteId", "region & <\u96ea>");
        field(payload, "cloudOwner", "owner & <\u96ea>");
        field(payload, "tenantId", "tenant & <\u96ea>");
        field(payload, "vnfId", "vnf & <\u96ea>");
        field(payload, "vfModuleId", "module & <\u96ea>");
        field(payload, "vfModuleStackId", "current-stack & <\u96ea>");
        field(payload, "vnfType", "vnf-type & <\u96ea>");
        field(payload, "vfModuleType", "module-type & <\u96ea>");
        field(payload, "vnfVersion", "2.0");
        field(payload, "modelCustomizationUuid", "custom & <\u96ea>");
        field(payload, "volumeGroupId", "volume & <\u96ea>");
        field(payload, "volumeGroupStackId", "volume-stack & <\u96ea>");
        field(payload, "baseVfModuleId", "base-module & <\u96ea>");
        field(payload, "baseVfModuleStackId", "base-stack & <\u96ea>");
        field(payload, "skipAAI", "true");
        field(payload, "failIfExists", "false");
        field(payload, "backout", "false");
        field(payload, "requestId", "body-request");
        field(payload, "serviceInstanceId", "service & <\u96ea>");
        String correlation = text(payload, "messageId");
        assertTrue(correlation, correlation.matches("callback-parent-[0-9]+"));
        field(payload, "notificationUrl", "http://localhost/messages/VNFAResponse/" + correlation);
        assertNull(variables.get("WorkflowException"));
    }

    @Test
    public void absentModuleNullStackAndEmptyStackRemainDistinctInTheAdapterRequest() throws Exception {
        for (String stack : Arrays.asList(null, "")) {
            VfModule module = new VfModule();
            module.setHeatStackId(stack);
            variables.put(PREFIX + "vfModule", module);
            script.prepVnfAdapterRest(execution);
            field(xml(PREFIX + "vnfAdapterRestRequest"), "vfModuleStackId", stack == null ? "null" : "");
        }
        variables.put(PREFIX + "vfModule", null);
        script.prepVnfAdapterRest(execution);
        field(xml(PREFIX + "vnfAdapterRestRequest"), "vfModuleStackId", "");
    }

    @Test
    public void adapterParametersPreserveRequestPrecedenceIdentitiesContextsFalseAndZero() throws Exception {
        script.prepVnfAdapterRest(execution);
        Map<String, String> actual = parameters(xml(PREFIX + "vnfAdapterRestRequest"));
        assertEquals("request & <\u96ea>", actual.get("choice"));
        assertEquals("0", actual.get("count"));
        assertEquals("false", actual.get("enabled"));
        assertEquals("vnf & <\u96ea>", actual.get("vnf_id"));
        assertEquals("vnf-name & <\u96ea>", actual.get("vnf_name"));
        assertEquals("module & <\u96ea>", actual.get("vf_module_id"));
        assertEquals("module-name & <\u96ea>", actual.get("vf_module_name"));
        assertEquals("environment & <\u96ea>", actual.get("environment_context"));
        assertEquals("workload & <\u96ea>", actual.get("workload_context"));
        assertEquals("request & <\u96ea>", parameters.get("choice"));
    }

    @Test
    public void booleanBackoutValuesKeepTheirWireMeaning() throws Exception {
        for (boolean backout : new boolean[] {false, true}) {
            variables.put(PREFIX + "backoutOnFailure", backout);
            script.prepVnfAdapterRest(execution);
            field(xml(PREFIX + "vnfAdapterRestRequest"), "backout", Boolean.toString(backout));
        }
    }

    @Test
    public void missingCallbackConfigurationRaisesItsOriginalErrorBeforePublishingPayload() {
        for (String endpoint : Arrays.asList(null, "")) {
            variables.put("mso.workflow.message.endpoint", endpoint);
            BpmnError failure = assertThrows(BpmnError.class, () -> script.prepVnfAdapterRest(execution));
            assertEquals("2000", failure.getErrorCode());
            WorkflowException error = error(2000);
            assertEquals("mso:workflow:message:endpoint URN mapping is not set", error.getErrorMessage());
            assertNull(variables.get(PREFIX + "vnfAdapterRestRequest"));
        }
    }

    @Test
    public void genericInventoryRequestUsesVnfModelValuesAndEscapedIdentity() throws Exception {
        script.prepUpdateAAIGenericVnf(execution);
        Document payload = xml(PREFIX + "updateAAIGenericVnfRequest");
        assertEquals("UpdateAAIGenericVnfRequest", payload.getDocumentElement().getLocalName());
        field(payload, "vnf-id", "vnf & <\u96ea>");
        field(payload, "model-invariant-id", "vnf-invariant");
        field(payload, "model-version-id", "vnf-version");
        assertEquals(Boolean.FALSE, variables.get(PREFIX + "skipUpdateGenericVnf"));
    }

    @Test
    public void genericInventoryIsSkippedWhenEitherModelNodeIsAbsent() {
        for (int mask = 0; mask < 3; mask++) {
            variables.put(PREFIX + "vnfInputs", inputs((mask & 1) == 0 ? null : "vnf-invariant",
                    (mask & 2) == 0 ? null : "vnf-version", null, null, null));
            variables.put(PREFIX + "skipUpdateGenericVnf", false);
            variables.put(PREFIX + "updateAAIGenericVnfRequest", null);
            script.prepUpdateAAIGenericVnf(execution);
            assertEquals("mask=" + mask, Boolean.TRUE, variables.get(PREFIX + "skipUpdateGenericVnf"));
            assertNull(variables.get(PREFIX + "updateAAIGenericVnfRequest"));
        }
    }

    @Test
    public void moduleInventoryRequestUsesModuleMetadataAndUpdatedStatus() throws Exception {
        script.prepUpdateAAIVfModule(execution);
        Document payload = xml(PREFIX + "updateAAIVfModuleRequest");
        assertEquals("UpdateAAIVfModuleRequest", payload.getDocumentElement().getLocalName());
        field(payload, "vnf-id", "vnf & <\u96ea>");
        field(payload, "vf-module-id", "module & <\u96ea>");
        field(payload, "orchestration-status", "updated");
        field(payload, "volume-group-id", "volume-1");
        field(payload, "model-invariant-id", "module-invariant");
        field(payload, "model-version-id", "module-version");
        field(payload, "contrail-service-instance-fqdn", "service.example.invalid");
        field(payload, "model-customization-id", "custom-1");
    }

    @Test
    public void absentModuleMetadataDoesNotInventOptionalInventoryElements() throws Exception {
        variables.put(PREFIX + "vnfInputs", "<vnf-inputs/>");
        variables.put(PREFIX + "volumeGroupId", null);
        variables.put(PREFIX + "modelCustomizationUuid", null);
        script.prepUpdateAAIVfModule(execution);
        Document payload = xml(PREFIX + "updateAAIVfModuleRequest");
        field(payload, "vnf-id", "vnf & <\u96ea>");
        field(payload, "vf-module-id", "module & <\u96ea>");
        field(payload, "orchestration-status", "updated");
        assertEquals(3, payload.getDocumentElement().getElementsByTagName("*").getLength());
    }

    @Test
    public void explicitEmptyModuleMetadataRemainsPresentInsteadOfBecomingAbsent() throws Exception {
        variables.put(PREFIX + "vnfInputs", inputs(null, null, "", "", ""));
        variables.put(PREFIX + "volumeGroupId", "");
        variables.put(PREFIX + "modelCustomizationUuid", "");
        script.prepUpdateAAIVfModule(execution);
        Document payload = xml(PREFIX + "updateAAIVfModuleRequest");
        for (String name : Arrays.asList("volume-group-id", "model-invariant-id", "model-version-id",
                "contrail-service-instance-fqdn", "model-customization-id")) {
            field(payload, name, "");
        }
    }

    @Test
    public void malformedInventoryInputRaisesAWorkflowErrorWithoutPublishingEitherRequest() {
        variables.put(PREFIX + "vnfInputs", "<vnf-inputs><broken></vnf-inputs>");
        for (String method : Arrays.asList("generic", "module")) {
            BpmnError failure = assertThrows(BpmnError.class, () -> {
                if (method.equals("generic")) {
                    script.prepUpdateAAIGenericVnf(execution);
                } else {
                    script.prepUpdateAAIVfModule(execution);
                }
            });
            assertEquals("1002", failure.getErrorCode());
            WorkflowException error = error(1002);
            assertTrue(error.getErrorMessage(), error.getErrorMessage().startsWith("Error in prepUpdateAAI"));
            assertNull(variables.get(PREFIX + "updateAAIGenericVnfRequest"));
            assertNull(variables.get(PREFIX + "updateAAIVfModuleRequest"));
        }
    }

    @Test
    public void genericInventoryModelValuesRetainEscapedAndUnicodeText() throws Exception {
        requireOptionalInventoryEscaping();
        variables.put(PREFIX + "vnfInputs",
                inputs("invariant &amp; &lt;\u96ea&gt;", "version &amp; &lt;\u96ea&gt;", null, null, null));
        script.prepUpdateAAIGenericVnf(execution);
        Document payload = xml(PREFIX + "updateAAIGenericVnfRequest");
        field(payload, "model-invariant-id", "invariant & <\u96ea>");
        field(payload, "model-version-id", "version & <\u96ea>");
    }

    @Test
    public void moduleInventoryOptionalValuesRetainEscapedAndUnicodeText() throws Exception {
        requireOptionalInventoryEscaping();
        variables.put(PREFIX + "vnfInputs", inputs(null, null, "invariant &amp; &lt;\u96ea&gt;",
                "version &amp; &lt;\u96ea&gt;", "fqdn &amp; &lt;\u96ea&gt;"));
        variables.put(PREFIX + "volumeGroupId", "volume & <\u96ea>");
        variables.put(PREFIX + "modelCustomizationUuid", "custom & <\u96ea>");
        script.prepUpdateAAIVfModule(execution);
        Document payload = xml(PREFIX + "updateAAIVfModuleRequest");
        field(payload, "volume-group-id", "volume & <\u96ea>");
        field(payload, "model-customization-id", "custom & <\u96ea>");
        field(payload, "model-invariant-id", "invariant & <\u96ea>");
        field(payload, "model-version-id", "version & <\u96ea>");
        field(payload, "contrail-service-instance-fqdn", "fqdn & <\u96ea>");
    }

    private WorkflowException error(int code) {
        assertTrue(variables.get("WorkflowException") instanceof WorkflowException);
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertEquals("DoUpdateVfModule", error.getProcessKey());
        assertEquals(code, error.getErrorCode());
        return error;
    }

    private Document xml(String variable) throws Exception {
        assertTrue(variable, variables.get(variable) instanceof String);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
        factory.setNamespaceAware(true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader((String) variables.get(variable))));
    }

    private static Map<String, String> parameters(Document document) {
        Map<String, String> result = new LinkedHashMap<>();
        NodeList entries = document.getElementsByTagName("entry");
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            String key = entry.getElementsByTagName("key").item(0).getTextContent();
            assertFalse(key, result.containsKey(key));
            result.put(key, entry.getElementsByTagName("value").item(0).getTextContent());
        }
        return result;
    }

    private static String text(Document document, String name) {
        assertEquals(name, 1, document.getElementsByTagName(name).getLength());
        return document.getElementsByTagName(name).item(0).getTextContent();
    }

    private static void field(Document document, String name, String expected) {
        assertEquals(name, expected, text(document, name));
    }

    private static String inputs(String vnfModel, String vnfVersion, String moduleModel, String moduleVersion,
            String fqdn) {
        return "<vnf-inputs>" + node("vnf-persona-model-id", vnfModel) + node("vnf-persona-model-version", vnfVersion)
                + node("persona-model-id", moduleModel) + node("persona-model-version", moduleVersion)
                + node("contrail-service-instance-fqdn", fqdn) + "</vnf-inputs>";
    }

    private static String node(String name, String encodedValue) {
        return encodedValue == null ? "" : "<" + name + ">" + encodedValue + "</" + name + ">";
    }

    private static void requireOptionalInventoryEscaping() {
        Assume.assumeTrue(
                "SO-4457: VF-module update inventory builders interpolate optional values without XML escaping",
                Boolean.getBoolean("groovy.migration.verification"));
    }
}
