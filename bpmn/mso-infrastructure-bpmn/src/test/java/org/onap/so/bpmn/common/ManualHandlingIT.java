/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

import static org.onap.so.bpmn.mock.StubResponseDatabase.MockPostRequestDB;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.junit.Assert;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for RainyDayHandler.bpmn.
 */
public class ManualHandlingIT extends BaseIntegrationTest {
    Logger logger = LoggerFactory.getLogger(ManualHandlingIT.class);

    @Test
    public void TestManualHandlingSuccess() {
        MockPostRequestDB(wireMockServer);
        String updatePath = "/v1/request-db/infra-requests/testRequestId/updates";
        wireMockServer.stubFor(post(urlPathEqualTo(updatePath)).willReturn(aResponse().withStatus(204)));

        Map<String, Object> variables = new HashMap<>();
        variables.put("isDebugLogEnabled", "true");
        variables.put("msoRequestId", "testRequestId");
        variables.put("serviceType", "X");
        variables.put("vnfType", "Y");
        variables.put("currentActivity", "BB1");
        variables.put("workStep", "1");
        variables.put("failedActivity", "AAI");
        variables.put("vnfName", "vSAMP12");
        variables.put("errorCode", "123");
        variables.put("errorText", "update failed");
        variables.put("validResponses", "Rollback");
        variables.put("vnfName", "vSAMP1");


        String businessKey = UUID.randomUUID().toString();
        ProcessInstance instance =
                processEngine.getRuntimeService().startProcessInstanceByKey("ManualHandling", businessKey, variables);
        Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(instance.getId()).singleResult();
        Assert.assertNotNull(task);
        processEngine.getTaskService().complete(task.getId(), Map.of("responseValue", "skip"));
        Assert.assertEquals(0, processEngine.getRuntimeService().createProcessInstanceQuery()
                .processInstanceId(instance.getId()).count());
        wireMockServer.verify(postRequestedFor(urlPathEqualTo(updatePath)).withRequestBody(
                equalToJson("{\"lastModifiedBy\":\"ManualHandling\",\"requestStatus\":\"PENDING_MANUAL_TASK\"}")));
        wireMockServer.verify(postRequestedFor(urlPathEqualTo(updatePath)).withRequestBody(
                equalToJson("{\"lastModifiedBy\":\"ManualHandling\",\"requestStatus\":\"IN_PROGRESS\"}")));
    }
}
