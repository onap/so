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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.junit.Test;
import org.onap.so.beans.nsmf.NssmfAdapterNBIRequest;
import org.onap.so.beans.nsmf.SliceProfileAdapter;
import org.onap.so.beans.nsmf.SliceTaskInfo;
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter;
import org.onap.so.beans.nsmf.oof.SubnetType;
import org.onap.so.bpmn.common.verification.WorkflowPersistenceSupport;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.infrastructure.scripts.DoAllocateNSIandNSSI;

public class NsiRequestPersistenceTest extends WorkflowPersistenceSupport {
    private static final String[] REQUESTS =
            {"AnAllocateNssiNbiRequest", "CnAllocateNssiNbiRequest", "TnBHAllocateNssiNbiRequest"};
    private static final String[] SNAPSHOTS = {"sliceTaskParams", "anSliceTaskInfo", "cnSliceTaskInfo",
            "tnBHSliceTaskInfo", "AnAllocateNssiNbiRequest", "CnAllocateNssiNbiRequest", "TnBHAllocateNssiNbiRequest"};
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void generatedRequestsAndSelectionStateResumeWithTheirJavaTypesAndIsolatedCallbacks() {
        Map<String, Object> firstInput = input("first");
        Map<String, Object> secondInput = input("second");
        Map<String, JsonNode> firstExpected = snapshots(firstInput);
        Map<String, JsonNode> secondExpected = snapshots(secondInput);
        ProcessInstance first = start(firstInput);
        ProcessInstance second = start(secondInput);
        ((SliceTaskParamsAdapter) firstInput.get("sliceTaskParams")).getServiceProfile().put("enabled", true);
        ((NssmfAdapterNBIRequest) firstInput.get(REQUESTS[0])).getServiceInfo().setNsiId("caller changed after commit");
        restart();

        assertState(first, firstExpected, false);
        assertState(second, secondExpected, false);
        assertNotSame(firstInput.get(REQUESTS[0]), engine.getRuntimeService().getVariable(first.getId(), REQUESTS[0]));
        reject("unknown");
        correlate("second", "second-response");
        ended(second);
        assertState(second, secondExpected, true);
        assertEquals("second-response", historic(second, "WorkflowResponse"));
        assertEquals(Boolean.TRUE, historic(second, "RCVWFMSG_SuccessIndicator"));
        assertNull(historic(second, "WorkflowException"));
        assertEquals(1,
                engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(first.getId()).count());
        String response = "{\"status\":\"success\",\"count\":0,\"enabled\":false,\"name\":\"\u96ea\"}";
        correlate("first", response);
        ended(first);
        assertState(first, firstExpected, true);
        assertEquals(response, historic(first, "WorkflowResponse"));
        assertEquals(String.class, historic(first, "WorkflowResponse").getClass());
        assertEquals(Boolean.TRUE, historic(first, "RCVWFMSG_SuccessIndicator"));
        assertNull(historic(first, "WorkflowException"));
        assertEquals(0, engine.getManagementService().createJobQuery().processInstanceId(first.getId()).count());
        reject("first");
        reject("second");
        activity(first, "ScriptTask_1", 1);
        activity(first, "ScriptTask_setSuccess", 1);
        activity(first, "ScriptTask_5", 0);
    }

    @Test
    public void persistedTimeoutKeepsTheRequestGraphsAndExactWorkflowError() {
        Map<String, Object> input = input("timeout");
        Map<String, JsonNode> expected = snapshots(input);
        ProcessInstance process = start(input);
        Job timer = engine.getManagementService().createJobQuery().processInstanceId(process.getId()).singleResult();
        assertNotNull(timer);
        assertEquals(new Date(START_TIME + 60000), timer.getDuedate());
        restart();

        assertState(process, expected, false);
        Job restored = engine.getManagementService().createJobQuery().processInstanceId(process.getId()).singleResult();
        assertNotNull(restored);
        assertEquals(timer.getId(), restored.getId());
        assertEquals(timer.getDuedate(), restored.getDuedate());
        ClockUtil.setCurrentTime(new Date(START_TIME + 61000));
        engine.getManagementService().executeJob(restored.getId());
        ended(process);
        assertState(process, expected, true);
        assertTrue(historic(process, "WorkflowException") instanceof WorkflowException);
        WorkflowException error = (WorkflowException) historic(process, "WorkflowException");
        assertEquals(7010, error.getErrorCode());
        assertEquals("Receive Workflow Message Timeout Error", error.getErrorMessage());
        assertNull(historic(process, "WorkflowResponse"));
        assertEquals(0, engine.getManagementService().createJobQuery().processInstanceId(process.getId()).count());
        reject("timeout");
        activity(process, "ScriptTask_1", 1);
        activity(process, "BoundaryEvent_1", 1);
        activity(process, "ScriptTask_5", 1);
        activity(process, "ScriptTask_setSuccess", 0);
    }

    private void assertState(ProcessInstance process, Map<String, JsonNode> expected, boolean finished) {
        for (String key : SNAPSHOTS) {
            Object actual =
                    finished ? historic(process, key) : engine.getRuntimeService().getVariable(process.getId(), key);
            assertNotNull(key, actual);
            assertEquals(key, expected.get(key), mapper.valueToTree(actual));
        }
        for (String key : REQUESTS) {
            Object request =
                    finished ? historic(process, key) : engine.getRuntimeService().getVariable(process.getId(), key);
            assertEquals(key, NssmfAdapterNBIRequest.class, request.getClass());
            if (!finished) {
                ObjectValue stored = engine.getRuntimeService().getVariableTyped(process.getId(), key, false);
                assertEquals("application/x-java-serialized-object", stored.getSerializationDataFormat());
                assertEquals(NssmfAdapterNBIRequest.class.getName(), stored.getObjectTypeName());
                assertFalse(stored.isDeserialized());
                assertNotNull(stored.getValueSerialized());
            }
        }
        Object state = finished ? historic(process, "sliceTaskParams")
                : engine.getRuntimeService().getVariable(process.getId(), "sliceTaskParams");
        assertEquals(SliceTaskParamsAdapter.class, state.getClass());
        Map<String, Object> profile = ((SliceTaskParamsAdapter) state).getServiceProfile();
        assertEquals(Boolean.FALSE, profile.get("enabled"));
        assertEquals(Integer.class, profile.get("capacity").getClass());
        assertEquals(0, profile.get("capacity"));
        assertTrue(profile.containsKey("nullable"));
        assertNull(profile.get("nullable"));
        assertEquals("", profile.get("empty"));
        assertEquals(SubnetType.AN, finished ? historic(process, "anSubnetType")
                : engine.getRuntimeService().getVariable(process.getId(), "anSubnetType"));
        assertEquals(SubnetType.CN, finished ? historic(process, "cnSubnetType")
                : engine.getRuntimeService().getVariable(process.getId(), "cnSubnetType"));
        assertEquals(SubnetType.TN_BH, finished ? historic(process, "tnBHSubnetType")
                : engine.getRuntimeService().getVariable(process.getId(), "tnBHSubnetType"));
    }

    private Map<String, JsonNode> snapshots(Map<String, Object> input) {
        Map<String, JsonNode> snapshots = new LinkedHashMap<>();
        for (String name : SNAPSHOTS) {
            snapshots.put(name, mapper.valueToTree(input.get(name)));
        }
        return snapshots;
    }

    private ProcessInstance start(Map<String, Object> input) {
        ProcessInstance process = engine.getRuntimeService().startProcessInstanceByKey("ReceiveWorkflowMessage", input);
        assertEquals(1, engine.getRuntimeService().createEventSubscriptionQuery().processInstanceId(process.getId())
                .eventName("WorkflowMessage").count());
        return process;
    }

    private void correlate(String correlator, String response) {
        engine.getRuntimeService().createMessageCorrelation("WorkflowMessage")
                .processInstanceVariableEquals("NSSI_CORRELATOR", correlator).setVariable("NSSI_MESSAGE", response)
                .correlate();
    }

    private void reject(String correlator) {
        assertThrows(MismatchingMessageCorrelationException.class, () -> correlate(correlator, "unexpected-response"));
    }

    private void ended(ProcessInstance process) {
        assertEquals(0,
                engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertEquals(1, engine.getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(process.getId()).finished().count());
    }

    private Object historic(ProcessInstance process, String name) {
        HistoricVariableInstance value = engine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).singleResult();
        return value == null ? null : value.getValue();
    }

    private void activity(ProcessInstance process, String id, long count) {
        assertEquals(id, count, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId(id).finished().count());
    }

    private Map<String, Object> input(String correlator) {
        SliceTaskParamsAdapter parameters = new SliceTaskParamsAdapter();
        parameters.setSuggestNsiId("nsi-" + correlator);
        parameters.setSuggestNsiName("nsi \"\u96ea\" " + correlator);
        parameters.getServiceProfile().put("sST", "embb");
        parameters.getServiceProfile().put("resourceSharingLevel", "shared");
        parameters.getServiceProfile().put("enabled", false);
        parameters.getServiceProfile().put("capacity", 0);
        parameters.getServiceProfile().put("nullable", null);
        parameters.getServiceProfile().put("empty", "");
        configure(parameters.getAnSliceTaskInfo(), SubnetType.AN, correlator);
        configure(parameters.getCnSliceTaskInfo(), SubnetType.CN, correlator);
        configure(parameters.getTnBHSliceTaskInfo(), SubnetType.TN_BH, correlator);
        Map<String, Object> input = new HashMap<>();
        input.put("sliceTaskParams", parameters);
        input.put("globalSubscriberId", "customer-1");
        input.put("subscriptionServiceType", "5G");
        input.put("sliceServiceInstanceName", "slice \u96ea " + correlator);
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable(anyString())).thenAnswer(call -> input.get(call.getArgument(0)));
        doAnswer(call -> {
            input.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        DoAllocateNSIandNSSI script = new DoAllocateNSIandNSSI();
        script.prepareAllocateAnNssi(execution);
        script.prepareAllocateCnNssi(execution);
        script.prepareAllocateTnBHNssi(execution);
        input.put("msoRequestId", "request-" + correlator);
        input.put("RCVWFMSG_messageType", "NSSI");
        input.put("RCVWFMSG_correlator", correlator);
        input.put("RCVWFMSG_timeout", "PT1M");
        input.put("isDebugLogEnabled", false);
        return input;
    }

    private void configure(SliceTaskInfo<SliceProfileAdapter> task, SubnetType type, String correlator) {
        task.setNetworkType(type.getNetworkType());
        task.setSubnetType(type);
        task.setSliceInstanceId("profile-" + type + "-" + correlator);
        task.setEndPointId("endpoint-" + type + "-" + correlator);
        task.setVendor("vendor \u96ea");
        task.setScriptName("script-" + type + ".groovy");
        task.getNSSTInfo().setUUID("model-" + type);
        task.getNSSTInfo().setInvariantUUID("invariant-" + type);
        task.getNSSTInfo().setName("model " + type);
        SliceProfileAdapter profile = new SliceProfileAdapter();
        profile.setSNSSAIList("001-000001|001-000002");
        profile.setPLMNIdList("00101|00202");
        profile.setCoverageAreaTAList("100|200");
        profile.setResourceSharingLevel("shared");
        profile.setUeMobilityLevel("stationary");
        profile.setIpAddress("2001:db8::10");
        profile.setLogicInterfaceId("interface-" + type);
        profile.setNextHopInfo("2001:db8::1");
        task.setSliceProfile(profile);
    }
}
