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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class ServiceIntentVariableContractTest {
    private final ServiceIntentUtils script = new ServiceIntentUtils();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();

    @Before
    public void configureExecution() {
        variables.put("testProcessKey", "ServiceIntentUtils");
        variables.put("mso-request-id", "request-1");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void commonInputsRetainNestedJsonTypesAndGenerateAValidJobIdentifier() {
        JSONObject extra = new JSONObject().put("enabled", false).put("count", 0).put("empty", "")
                .put("name", "München & 東京").put("nested", new JSONObject().put("value", true));
        variables.put("bpmnRequest",
                request().put("name", "München & 東京").put("additionalProperties", extra).toString());
        script.setCommonExecutionVars(execution);
        assertEquals("request-1", variables.get("msoRequestId"));
        assertEquals("subscriber-1", variables.get("globalSubscriberId"));
        assertEquals("intent", variables.get("serviceType"));
        assertEquals("5G", variables.get("subscriptionServiceType"));
        assertEquals("München & 東京", variables.get("servicename"));
        assertTrue(variables.get("serviceIntentParams") instanceof String);
        JSONObject actual = new JSONObject((String) variables.get("serviceIntentParams"));
        assertEquals(Boolean.FALSE, actual.get("enabled"));
        assertEquals(Integer.valueOf(0), actual.get("count"));
        assertEquals("", actual.get("empty"));
        assertEquals("München & 東京", actual.get("name"));
        assertEquals(Boolean.TRUE, actual.getJSONObject("nested").get("value"));
        assertNotNull(UUID.fromString((String) variables.get("jobId")));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void requiredCommonInputsFailWithTheBusinessCodeBeforeCreatingAJob() {
        for (String field : new String[] {"globalSubscriberId", "serviceType", "subscriptionServiceType"}) {
            for (String invalid : new String[] {null, "", " \t"}) {
                JSONObject input = request();
                if (invalid == null) {
                    input.remove(field);
                } else {
                    input.put(field, invalid);
                }
                variables.remove("WorkflowException");
                variables.remove("jobId");
                variables.put("bpmnRequest", input.toString());
                assertFailure(() -> script.setCommonExecutionVars(execution), 500, field);
                assertFalse(field, variables.containsKey("jobId"));
            }
        }
    }

    @Test
    public void malformedRequestsFailValidationBeforeCreatingAJob() {
        variables.put("bpmnRequest", "{malformed");
        assertFailure(() -> script.setCommonExecutionVars(execution), 500, "globalSubscriberId");
        assertFalse(variables.containsKey("jobId"));
    }

    @Test
    public void optionalModePermitsBlankSubscriberAndServiceTypeWithoutClearingAnExistingName() {
        variables.put("servicename", "existing-name");
        variables.put("bpmnRequest",
                request().put("globalSubscriberId", "").put("serviceType", "").put("name", " ").toString());
        script.setCommonExecutionVars(execution, false);
        assertEquals("", variables.get("globalSubscriberId"));
        assertEquals("", variables.get("serviceType"));
        assertEquals("existing-name", variables.get("servicename"));
        assertEquals("5G", variables.get("subscriptionServiceType"));
        assertNotNull(UUID.fromString((String) variables.get("jobId")));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void optionalJsonLookupPreservesExistingStateWhileFalseAndZeroRemainStringValues() {
        variables.put("target", "existing");
        script.setExecVarFromJsonIfExists(execution, "{}", "value", "target");
        assertEquals("existing", variables.get("target"));
        script.setExecVarFromJsonIfExists(execution, "{\"value\":\"\"}", "value", "target");
        assertEquals("existing", variables.get("target"));
        assertEquals("false", script.setExecVarFromJsonStr(execution, "{\"value\":false}", "value", "target", true));
        assertEquals("false", variables.get("target"));
        assertEquals("0", script.setExecVarFromJsonStr(execution, "{\"value\":0}", "value", "target", true));
        assertEquals("0", variables.get("target"));
    }

    @Test
    public void requiredJsonLookupReportsTheMissingKeyWithoutOverwritingExistingState() {
        variables.put("target", "existing");
        assertFailure(() -> script.setExecVarFromJsonStr(execution, "{}", "required-key", "target", true), 500,
                "required-key");
        assertEquals("existing", variables.get("target"));
    }

    @Test
    public void profileSelectorsRetainTheFirstEntryAndItsStringIdentity() {
        String profile = "{\"snssaiList\":[\"001-000001\",\"002-000002\"]," + "\"pLMNIdList\":[\"00101\",\"00202\"]}";
        assertEquals("001-000001", script.getFirstSnssaiFromSliceProfile(profile));
        assertEquals("00101", script.getFirstPlmnIdFromSliceProfile(profile));
    }

    @Test
    public void operationStatusRetainsAllIdentifiersAndStringProgress() {
        ResourceOperationStatus status = script.buildRoStatus("template", "resource", "job", "service", "Delete",
                "failed", "0", "controller & inventory failure");
        assertEquals("template", status.getResourceTemplateUUID());
        assertEquals("resource", status.getResourceInstanceID());
        assertEquals("service", status.getServiceId());
        assertEquals("job", status.getOperationId());
        assertEquals("Delete", status.getOperType());
        assertEquals("failed", status.getStatus());
        assertEquals("0", status.getProgress());
        assertEquals("controller & inventory failure", status.getStatusDescription());
    }

    @Test
    public void defaultControllerParameterEscapesTextWithoutCreatingExtraXmlElements() throws Exception {
        String name = "München & <name> \"東京\"";
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(script.buildDefaultVnfInputParams(name))));
        assertEquals(1, document.getElementsByTagName("param").getLength());
        assertEquals(1, document.getElementsByTagName("name").getLength());
        assertEquals(name, document.getElementsByTagName("name").item(0).getTextContent());
    }

    @Test
    public void creationRetainsModelIdentityAndAnExplicitServiceInstanceIdentifier() {
        variables.put("bpmnRequest",
                request().put("modelInvariantUuid", "invariant-1").put("modelUuid", "model-2").put("sst", "001")
                        .put("additionalProperties", new JSONObject().put("serviceInstanceID", "service-1"))
                        .toString());
        new CreateServiceIntentInstance().preProcessRequest(execution);
        assertEquals("CreateSiInstance_", variables.get("prefix"));
        assertEquals("invariant-1", variables.get("modelInvariantUuid"));
        assertEquals("model-2", variables.get("modelUuid"));
        assertEquals("001", variables.get("sst"));
        assertEquals("service-1", variables.get("serviceInstanceID"));
        assertTrue(variables.get("startTime") instanceof Long);
        assertNotNull(UUID.fromString((String) variables.get("jobId")));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void creationGeneratesAServiceIdentifierWhenTheOptionalIdentifierIsAbsent() {
        variables.put("bpmnRequest", request().put("modelInvariantUuid", "invariant-1").put("modelUuid", "model-2")
                .put("additionalProperties", new JSONObject()).toString());
        new CreateServiceIntentInstance().preProcessRequest(execution);
        assertNotNull(UUID.fromString((String) variables.get("serviceInstanceID")));
        assertNotNull(UUID.fromString((String) variables.get("jobId")));
        assertFalse(variables.get("serviceInstanceID").equals(variables.get("jobId")));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void creationRejectsEachMissingModelIdentityBeforeAssigningAServiceIdentifier() {
        for (String field : new String[] {"modelInvariantUuid", "modelUuid"}) {
            JSONObject input = request().put("modelInvariantUuid", "invariant-1").put("modelUuid", "model-2");
            input.remove(field);
            variables.put("bpmnRequest", input.toString());
            variables.remove("WorkflowException");
            assertFailure(() -> new CreateServiceIntentInstance().preProcessRequest(execution), 500, field);
            assertFalse(variables.containsKey("serviceInstanceID"));
        }
    }

    @Test
    public void modifyAndDeleteRetainTheRequestedServiceAndUseTheirOwnPrefixes() {
        AbstractServiceTaskProcessor[] scripts = {new ModifyServiceIntentInstance(), new DeleteServiceIntentInstance()};
        String[] prefixes = {"MCLL_", "DCLL_"};
        for (int index = 0; index < scripts.length; index++) {
            variables.put("bpmnRequest", request().put("serviceInstanceID", "service-1").toString());
            scripts[index].preProcessRequest(execution);
            assertEquals(prefixes[index], variables.get("prefix"));
            assertEquals("service-1", variables.get("serviceInstanceID"));
            assertTrue(variables.get("startTime") instanceof Long);
            assertNotNull(UUID.fromString((String) variables.get("jobId")));
            assertFalse(variables.containsKey("WorkflowException"));
        }
    }

    @Test
    public void modifyAndDeleteRejectAbsentEmptyAndBlankServiceIdentifiers() {
        for (AbstractServiceTaskProcessor workflow : new AbstractServiceTaskProcessor[] {
                new ModifyServiceIntentInstance(), new DeleteServiceIntentInstance()}) {
            for (String invalid : new String[] {null, "", " \t"}) {
                JSONObject input = request();
                if (invalid != null) {
                    input.put("serviceInstanceID", invalid);
                }
                variables.put("bpmnRequest", input.toString());
                variables.remove("WorkflowException");
                assertFailure(() -> workflow.preProcessRequest(execution), 500, "serviceInstanceID");
                assertFalse(variables.containsKey("serviceInstanceID"));
            }
        }
    }

    private JSONObject request() {
        return new JSONObject().put("globalSubscriberId", "subscriber-1").put("serviceType", "intent")
                .put("subscriptionServiceType", "5G");
    }

    private void assertFailure(Runnable action, int code, String detail) {
        try {
            action.run();
            fail("Expected workflow error " + code);
        } catch (BpmnError error) {
            assertEquals(String.valueOf(code), error.getErrorCode());
            assertTrue(variables.get("WorkflowException") instanceof WorkflowException);
            WorkflowException stored = (WorkflowException) variables.get("WorkflowException");
            assertEquals(code, stored.getErrorCode());
            assertTrue(stored.getErrorMessage(), stored.getErrorMessage().contains(detail));
        }
    }
}
