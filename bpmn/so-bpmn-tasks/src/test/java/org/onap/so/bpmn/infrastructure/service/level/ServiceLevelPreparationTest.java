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

package org.onap.so.bpmn.infrastructure.service.level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.infrastructure.service.level.impl.ServiceLevelConstants;
import org.onap.so.bpmn.infrastructure.service.level.impl.ServiceLevelPreparation;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.Workflow;


public class ServiceLevelPreparationTest extends BaseTaskTest {

    private static final String TEST_PNF_SCOPE = "pnf";
    private static final String TEST_PROCESS_KEY = "testProcessKey";
    private static final String PROCESS_KEY_VALUE = "testProcessKeyValue";
    private static final String BPMN_REQUEST = "bpmnRequest";
    private static final String RESOURCE_TYPE = "resourceType";
    private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
    private static final String PNF_NAME = "pnfName";
    private static final String HEALTH_CHECK_OPERATION = "ResourceHealthCheck";
    private static final String PNF_HEALTH_CHECK_WORKFLOW = "PNFHealthCheck";
    private static final Map<String, List<String>> HEALTH_CHECK_PARAMS_MAP = Map.of(TEST_PNF_SCOPE,
            Arrays.asList(SERVICE_INSTANCE_ID, RESOURCE_TYPE, BPMN_REQUEST, PNF_NAME), "vnf", Collections.emptyList());

    private List<Workflow> workflowList = new ArrayList<>();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @InjectMocks
    private ServiceLevelPreparation serviceLevelPrepare;

    @InjectMocks
    @Spy
    private ExceptionBuilder exceptionBuilder;

    private DelegateExecution execution = new DelegateExecutionFake();
    private DelegateExecution invalidExecution = new DelegateExecutionFake();


    @Before
    public void setUpPnfUpgradeTest() {
        execution.setVariable(RESOURCE_TYPE, TEST_PNF_SCOPE);
        execution.setVariable(TEST_PROCESS_KEY, PROCESS_KEY_VALUE);
        execution.setVariable(BPMN_REQUEST, "bpmnRequestValue");
        execution.setVariable(SERVICE_INSTANCE_ID, "serviceInstanceIdValue");
        execution.setVariable(PNF_NAME, "PnfDemo");

        invalidExecution.setVariables(execution.getVariables());

        Workflow pnfWorkflow = new Workflow();
        pnfWorkflow.setName(PNF_HEALTH_CHECK_WORKFLOW);
        pnfWorkflow.setOperationName(HEALTH_CHECK_OPERATION);
        pnfWorkflow.setResourceTarget(TEST_PNF_SCOPE);
        workflowList.add(pnfWorkflow);

        when(catalogDbClient.findWorkflowByOperationName(HEALTH_CHECK_OPERATION)).thenReturn(workflowList);
    }

    @Test
    public void executePnfUpgradeSuccessTest() throws Exception {
        serviceLevelPrepare.execute(execution);
        // Expect the pnf health check workflow to be set in to execution if validation is successful
        assertThat(String.valueOf(execution.getVariable(ServiceLevelConstants.HEALTH_CHECK_WORKFLOW_TO_INVOKE)))
                .isEqualTo("PNFHealthCheck");
    }

    @Test
    public void validateFailureParamsForPnfTest() {
        invalidExecution.removeVariable(BPMN_REQUEST);
        // BPMN exception is thrown in case of validation failure or invalid execution
        thrown.expect(BpmnError.class);
        serviceLevelPrepare.validateParamsWithScope(invalidExecution, TEST_PNF_SCOPE,
                HEALTH_CHECK_PARAMS_MAP.get(TEST_PNF_SCOPE));
    }

    @Test
    public void invalidScopeExecuteTest() throws Exception {
        invalidExecution.setVariable(RESOURCE_TYPE, "InvalidResource");
        // BPMN workflow exception is expected incase of invalid resource type other than pnf/vnf
        thrown.expect(BpmnError.class);
        serviceLevelPrepare.execute(invalidExecution);
    }

    @Test
    public void invokeServiceLevelPrepareWithoutScope() throws Exception {
        invalidExecution.removeVariable(RESOURCE_TYPE);
        thrown.expect(BpmnError.class);
        serviceLevelPrepare.execute(invalidExecution);

    }

    @Test
    public void validateDefaultWorkflowIsSetWithoutDBData() throws Exception {
        // Mock empty workflow list in db response
        when(catalogDbClient.findWorkflowByOperationName(HEALTH_CHECK_OPERATION)).thenReturn(new ArrayList<Workflow>());
        serviceLevelPrepare.execute(execution);
        // Expect default workflow gets assigned when workflow name not found in db.
        assertThat(String.valueOf(execution.getVariable(ServiceLevelConstants.HEALTH_CHECK_WORKFLOW_TO_INVOKE)))
                .isEqualTo(ServiceLevelConstants.DEFAULT_HEALTH_CHECK_WORKFLOWS.get(TEST_PNF_SCOPE));
    }

    @Test
    public void validateWorkflowSetFromDb() throws Exception {
        Workflow vnfWorkflow = new Workflow();
        vnfWorkflow.setName("VNFHealthCheck");
        vnfWorkflow.setOperationName(HEALTH_CHECK_OPERATION);
        vnfWorkflow.setResourceTarget("vnf");
        workflowList.add(vnfWorkflow);
        // Mock db response with multiple worklfows mapped with same operation name
        when(catalogDbClient.findWorkflowByOperationName(HEALTH_CHECK_OPERATION)).thenReturn(workflowList);
        serviceLevelPrepare.execute(execution);

        // Expect right workflow gets assigned from db based on the controller scope.
        assertThat(String.valueOf(execution.getVariable(ServiceLevelConstants.HEALTH_CHECK_WORKFLOW_TO_INVOKE)))
                .isEqualTo(PNF_HEALTH_CHECK_WORKFLOW);
    }

}


