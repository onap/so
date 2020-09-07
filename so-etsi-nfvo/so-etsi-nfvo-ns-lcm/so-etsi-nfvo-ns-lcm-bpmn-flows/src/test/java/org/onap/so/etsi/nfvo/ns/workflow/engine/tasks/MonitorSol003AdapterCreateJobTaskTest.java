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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.onap.so.adapters.etsisol003adapter.lcm.v1.model.OperationStateEnum.COMPLETED;
import static org.onap.so.adapters.etsisol003adapter.lcm.v1.model.OperationStatusRetrievalStatusEnum.STATUS_FOUND;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.vnfm.Sol003AdapterConfiguration.SOL003_ADAPTER_REST_TEMPLATE_BEAN;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfResponse;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.QueryJobResponse;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.BaseTest;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.GsonProvider;
import org.onap.so.etsi.nfvo.ns.lcm.model.InlineResponse400;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class MonitorSol003AdapterCreateJobTaskTest extends BaseTest {

    private static final String MONITOR_SOL003_ADAPTER_CREATE_JOB_WORKFLOW = "MonitorSol003AdapterCreateJob";

    @Autowired
    @Qualifier(SOL003_ADAPTER_REST_TEMPLATE_BEAN)
    private RestTemplate restTemplate;

    @Autowired
    private GsonProvider gsonProvider;

    private MockRestServiceServer mockRestServiceServer;
    private Gson gson;

    @Before
    public void before() {
        wireMockServer.resetAll();
        final MockRestServiceServer.MockRestServiceServerBuilder builder = MockRestServiceServer.bindTo(restTemplate);
        builder.ignoreExpectOrder(true);
        mockRestServiceServer = builder.build();

        gson = gsonProvider.getGson();
        restTemplate.getMessageConverters().add(new GsonHttpMessageConverter(gson));
    }


    @Test
    public void testMonitorSol003AdapterCreateJobTaskWorkflow_SuccessfullCase() throws InterruptedException {

        mockRestServiceServer.expect(requestTo(SOL003_ADAPTER_ENDPOINT_URL + "/jobs/" + RANDOM_JOB_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(gson.toJson(getQueryJobResponse()), MediaType.APPLICATION_JSON));

        final ProcessInstance processInstance = executeWorkflow(MONITOR_SOL003_ADAPTER_CREATE_JOB_WORKFLOW,
                RANDOM_JOB_ID, getVariables(RANDOM_JOB_ID, new CreateVnfResponse().jobId(RANDOM_JOB_ID)));

        assertTrue(waitForProcessInstanceToFinish(processInstance.getProcessInstanceId()));

        final HistoricProcessInstance historicProcessInstance =
                getHistoricProcessInstance(processInstance.getProcessInstanceId());
        assertNotNull(historicProcessInstance);

        assertEquals(HistoricProcessInstance.STATE_COMPLETED, historicProcessInstance.getState());

        final HistoricVariableInstance nsResponseVariable = getVariable(processInstance.getProcessInstanceId(),
                CamundaVariableNameConstants.OPERATION_STATUS_PARAM_NAME);

        assertNotNull(nsResponseVariable);
        assertEquals(COMPLETED, nsResponseVariable.getValue());

    }

    @Test
    public void testMonitorSol003AdapterCreateJobTaskWorkflow_FailurelCase() throws InterruptedException {

        mockRestServiceServer.expect(requestTo(SOL003_ADAPTER_ENDPOINT_URL + "/jobs/" + RANDOM_JOB_ID))
                .andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        final ProcessInstance processInstance = executeWorkflow(MONITOR_SOL003_ADAPTER_CREATE_JOB_WORKFLOW,
                RANDOM_JOB_ID, getVariables(RANDOM_JOB_ID, new CreateVnfResponse().jobId(RANDOM_JOB_ID)));

        assertTrue(waitForProcessInstanceToFinish(processInstance.getProcessInstanceId()));

        final HistoricProcessInstance historicProcessInstance =
                getHistoricProcessInstance(processInstance.getProcessInstanceId());
        assertNotNull(historicProcessInstance);

        assertEquals(HistoricProcessInstance.STATE_COMPLETED, historicProcessInstance.getState());

        final HistoricVariableInstance nsResponseVariable = getVariable(processInstance.getProcessInstanceId(),
                CamundaVariableNameConstants.OPERATION_STATUS_PARAM_NAME);

        assertNull(nsResponseVariable);

        final HistoricVariableInstance workflowExceptionVariable = getVariable(processInstance.getProcessInstanceId(),
                CamundaVariableNameConstants.CREATE_NS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME);

        final InlineResponse400 problemDetails = (InlineResponse400) workflowExceptionVariable.getValue();
        assertNotNull(problemDetails);
        assertNotNull(problemDetails.getDetail());
    }


    private QueryJobResponse getQueryJobResponse() {
        return new QueryJobResponse().id(RANDOM_JOB_ID).operationState(COMPLETED)
                .operationStatusRetrievalStatus(STATUS_FOUND);
    }

    private Map<String, Object> getVariables(final String jobId, final CreateVnfResponse createVnfResponse) {
        final Map<String, Object> variables = new HashMap<>();
        variables.put(CamundaVariableNameConstants.JOB_ID_PARAM_NAME, jobId);
        variables.put(CamundaVariableNameConstants.CREATE_VNF_RESPONSE_PARAM_NAME, createVnfResponse);

        return variables;
    }

}
