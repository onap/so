/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.etsi.nfvo.ns.workflow.engine.tasks;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.BaseTest;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.tasks.MonitorInstantiateSol003AdapterNodeTask;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class MonitorInstantiateSol003AdapterNodeTaskTest extends BaseTest {

    private static final String RANDOWM_GENERIC_VNF_ID = UUID.randomUUID().toString();
    private static final String MONITOR_SOL003_ADAPTER_CREATE_NODE_STATUS_WORKFLOW =
            "MonitorSol003AdapterCreateNodeStatus";

    @Before
    public void before() {
        wireMockServer.resetAll();
    }

    @Test
    public void testMonitorSol003AdapterCreateNodeStatus_SuccessfullCase() throws InterruptedException {

        final String modelEndpoint = "/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + UUID_REGEX;

        wireMockServer.stubFor(get(urlMatching(modelEndpoint)).willReturn(ok())
                .willReturn(okJson("{\"orchestration-status\": \"Created\"}")));


        final ProcessInstance processInstance = executeWorkflow(MONITOR_SOL003_ADAPTER_CREATE_NODE_STATUS_WORKFLOW,
                RANDOM_JOB_ID, getVariables(RANDOM_JOB_ID, RANDOWM_GENERIC_VNF_ID));

        assertTrue(waitForProcessInstanceToFinish(processInstance.getProcessInstanceId()));

        final HistoricProcessInstance historicProcessInstance =
                getHistoricProcessInstance(processInstance.getProcessInstanceId());
        assertNotNull(historicProcessInstance);

        assertEquals(HistoricProcessInstance.STATE_COMPLETED, historicProcessInstance.getState());

        final HistoricVariableInstance nsResponseVariable = getVariable(processInstance.getProcessInstanceId(),
                MonitorInstantiateSol003AdapterNodeTask.CREATE_VNF_NODE_STATUS);

        assertNotNull(nsResponseVariable);
        assertTrue((boolean) nsResponseVariable.getValue());

    }

    private Map<String, Object> getVariables(final String jobId, final String vnfId) {
        final Map<String, Object> variables = new HashMap<>();
        variables.put(CamundaVariableNameConstants.JOB_ID_PARAM_NAME, jobId);
        variables.put(CamundaVariableNameConstants.NF_INST_ID_PARAM_NAME, vnfId);

        return variables;
    }


}
