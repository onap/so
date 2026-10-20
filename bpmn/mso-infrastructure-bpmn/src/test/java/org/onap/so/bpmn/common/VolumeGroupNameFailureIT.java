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
package org.onap.so.bpmn.common;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.bpmn.core.WorkflowException;

public class VolumeGroupNameFailureIT extends BaseIntegrationTest {
    private static final String PATH = "/aai/v[0-9]+/cloud-infrastructure/cloud-regions/"
            + "cloud-region/CloudOwner/MDTWNJ21/volume-groups/volume-group/volume-1";

    @Test
    public void rejectedInventoryPreservesTheWorkflowFailureAndBypassesNameComparison() {
        String process = start(500, "{\"error\":\"inventory unavailable\"}", "requested");
        WorkflowException error = failure(process);
        assertEquals(500, error.getErrorCode());
        assertEquals("AAI GET Failed", error.getErrorMessage());
        assertEquals(Integer.valueOf(500), value(process, "CVGN_queryVolumeGroupResponseCode"));
        assertActivity(process, "CheckAAIQueryResult", 0);
        assertActivity(process, "VolumeGroupNotInName", 0);
    }

    @Test
    public void malformedSuccessfulInventoryCannotConfirmAName() {
        String process = start(200, "{\"volume-group-id\":", "requested");
        assertEquals(500, failure(process).getErrorCode());
        assertActivity(process, "CheckAAIQueryResult", 0);
    }

    @Test
    public void emptySuccessfulInventoryCannotConfirmAName() {
        String process = start(200, "", "requested");
        assertTrue(failure(process).getErrorCode() > 0);
        assertEquals(Boolean.FALSE, value(process, "CVGN_volumeGroupNameMatches"));
    }

    @Test
    public void anOmittedInventoryNameProducesTheDefinedMismatchFailure() {
        String process = start(200, "{\"volume-group-id\":\"volume-1\"}", "requested");
        assertMismatch(process, "requested");
    }

    @Test
    public void matchingUnicodeNamesTakeTheSuccessRouteWithTypedVariables() {
        String process =
                start(200, "{\"volume-group-id\":\"volume-1\",\"volume-group-name\":\"München & 雪\"}", "München & 雪");
        assertEquals(Boolean.TRUE, value(process, "CVGN_volumeGroupNameMatches"));
        assertEquals(Boolean.class, value(process, "CVGN_volumeGroupNameMatches").getClass());
        assertEquals(Integer.valueOf(200), value(process, "CVGN_queryVolumeGroupResponseCode"));
        assertActivity(process, "CheckAAIQueryResult", 1);
        assertActivity(process, "VolumeGroupNotInName", 0);
        assertActivity(process, "EndEvent_4", 1);
        assertEquals(0, processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(process).variableName("WorkflowException").count());
    }

    @Test
    public void inventoryNamesAreComparedWithTheirOriginalCase() {
        String process =
                start(200, "{\"volume-group-id\":\"volume-1\",\"volume-group-name\":\"volume-name\"}", "VOLUME-NAME");
        assertMismatch(process, "VOLUME-NAME");
    }

    private String start(int status, String body, String requestedName) {
        wireMockServer.stubFor(get(urlPathMatching(PATH)).willReturn(aResponse().withStatus(status)
                .withHeader("Content-Type", "application/json").withHeader("Connection", "close").withBody(body)));
        Map<String, Object> input = new HashMap<>();
        input.put("mso-request-id", UUID.randomUUID().toString());
        input.put("ConfirmVolumeGroupName_volumeGroupId", "volume-1");
        input.put("ConfirmVolumeGroupName_volumeGroupName", requestedName);
        input.put("ConfirmVolumeGroupName_aicCloudRegion", "MDTWNJ21");
        input.put("CVGN_volumeGroupNameMatches", true);
        String process = invokeSubProcess("ConfirmVolumeGroupName", input);
        wireMockServer.verify(1,
                getRequestedFor(urlPathMatching(PATH)).withHeader("Accept", containing("application/json")));
        assertEquals(1, wireMockServer.getAllServeEvents().size());
        assertEquals(0,
                processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(process).count());
        assertEquals(1, processEngine.getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(process).finished().count());
        assertEquals(requestedName, value(process, "CVGN_volumeGroupName"));
        assertEquals("volume-1", value(process, "CVGN_volumeGroupId"));
        return process;
    }

    private WorkflowException failure(String process) {
        Object raw = value(process, "WorkflowException");
        assertEquals(WorkflowException.class, raw.getClass());
        WorkflowException error = (WorkflowException) raw;
        assertEquals("ConfirmVolumeGroupName", error.getProcessKey());
        assertEquals(Boolean.FALSE, value(process, "CVGN_volumeGroupNameMatches"));
        assertActivity(process, "EndEvent_4", 0);
        return error;
    }

    private void assertMismatch(String process, String requestedName) {
        WorkflowException error = failure(process);
        assertEquals(1002, error.getErrorCode());
        assertEquals("Error occurred - volume group id volume-1 is not associated with " + requestedName,
                error.getErrorMessage());
        assertEquals(Integer.valueOf(200), value(process, "CVGN_queryVolumeGroupResponseCode"));
        assertActivity(process, "CheckAAIQueryResult", 1);
        assertActivity(process, "VolumeGroupNotInName", 1);
    }

    private Object value(String process, String name) {
        HistoricVariableInstance variable = processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(process).variableName(name).singleResult();
        assertNotNull(name, variable);
        return variable.getValue();
    }

    private void assertActivity(String process, String activity, long count) {
        assertEquals(activity, count, processEngine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(process).activityId(activity).finished().count());
    }
}
