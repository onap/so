/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.onap.so.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.extension.mockito.mock.FluentJavaDelegateMock;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ServiceTaskBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.onap.so.TestApplication;
import org.onap.so.bpmn.buildingblock.HomingV2;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.common.listener.validation.BuildingBlockValidatorRunner;
import org.onap.so.bpmn.infrastructure.aai.tasks.AAICommonTasks;
import org.onap.so.bpmn.infrastructure.aai.tasks.AAICreateTasks;
import org.onap.so.bpmn.infrastructure.aai.tasks.AAIDeleteTasks;
import org.onap.so.bpmn.infrastructure.aai.tasks.AAIFlagTasks;
import org.onap.so.bpmn.infrastructure.aai.tasks.AAIQueryTasks;
import org.onap.so.bpmn.infrastructure.aai.tasks.AAIUpdateTasks;
import org.onap.so.bpmn.infrastructure.adapter.network.tasks.NetworkAdapterCreateTasks;
import org.onap.so.bpmn.infrastructure.adapter.network.tasks.NetworkAdapterDeleteTasks;
import org.onap.so.bpmn.infrastructure.adapter.network.tasks.NetworkAdapterUpdateTasks;
import org.onap.so.bpmn.infrastructure.adapter.vnf.tasks.VnfAdapterCreateTasks;
import org.onap.so.bpmn.infrastructure.adapter.vnf.tasks.VnfAdapterDeleteTasks;
import org.onap.so.bpmn.infrastructure.adapter.vnf.tasks.VnfAdapterImpl;
import org.onap.so.bpmn.infrastructure.appc.tasks.AppcOrchestratorPreProcessor;
import org.onap.so.bpmn.infrastructure.appc.tasks.AppcRunTasks;
import org.onap.so.bpmn.infrastructure.audit.AuditTasks;
import org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock.ControllerExecutionBB;
import org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda.ControllerExecutionDE;
import org.onap.so.bpmn.infrastructure.flowspecific.tasks.ActivateVfModule;
import org.onap.so.bpmn.infrastructure.flowspecific.tasks.AssignNetwork;
import org.onap.so.bpmn.infrastructure.flowspecific.tasks.AssignNetworkBBUtils;
import org.onap.so.bpmn.infrastructure.flowspecific.tasks.AssignVnf;
import org.onap.so.bpmn.infrastructure.flowspecific.tasks.ConfigurationScaleOut;
import org.onap.so.bpmn.infrastructure.flowspecific.tasks.CreateNetwork;
import org.onap.so.bpmn.infrastructure.flowspecific.tasks.CreateNetworkCollection;
import org.onap.so.bpmn.infrastructure.flowspecific.tasks.GenericVnfHealthCheck;
import org.onap.so.bpmn.infrastructure.flowspecific.tasks.UnassignNetworkBB;
import org.onap.so.bpmn.infrastructure.flowspecific.tasks.UnassignVnf;
import org.onap.so.bpmn.infrastructure.namingservice.tasks.NamingServiceCreateTasks;
import org.onap.so.bpmn.infrastructure.namingservice.tasks.NamingServiceDeleteTasks;
import org.onap.so.bpmn.infrastructure.manualhandling.tasks.ManualHandlingTasks;
import org.onap.so.bpmn.infrastructure.sdnc.tasks.SDNCActivateTasks;
import org.onap.so.bpmn.infrastructure.sdnc.tasks.SDNCAssignTasks;
import org.onap.so.bpmn.infrastructure.sdnc.tasks.SDNCChangeAssignTasks;
import org.onap.so.bpmn.infrastructure.sdnc.tasks.SDNCDeactivateTasks;
import org.onap.so.bpmn.infrastructure.sdnc.tasks.SDNCQueryTasks;
import org.onap.so.bpmn.infrastructure.sdnc.tasks.SDNCUnassignTasks;
import org.onap.so.bpmn.infrastructure.workflow.tasks.FlowCompletionTasks;
import org.onap.so.bpmn.infrastructure.workflow.tasks.OrchestrationStatusValidator;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowAction;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionBBFailure;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionBBTasks;
import org.onap.so.bpmn.sdno.tasks.SDNOHealthCheckTasks;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.bpmn.servicedecomposition.tasks.ExecuteBuildingBlockRainyDay;
import org.onap.so.client.sdnc.SDNCClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseBPMNTest {
    @Autowired
    protected RuntimeService runtimeService;

    @Autowired
    protected ExternalTaskService externalTaskService;

    @Autowired
    private RepositoryService repositoryService;

    protected Map<String, Object> variables = new HashMap<>();

    protected List<String> mockedSubprocessList = new ArrayList<>();

    protected TestRestTemplate restTemplate = new TestRestTemplate();

    protected HttpHeaders headers = new HttpHeaders();

    @MockBean
    protected AAIUpdateTasks aaiUpdateTasks;

    @MockBean
    protected AAICreateTasks aaiCreateTasks;

    @MockBean
    protected AAIQueryTasks aaiQueryTasks;

    @MockBean
    protected AAIDeleteTasks aaiDeleteTasks;

    @MockBean
    protected AAIFlagTasks aaiFlagTasks;


    @MockBean
    protected AppcRunTasks appcRunTasks;

    @MockBean
    protected AppcOrchestratorPreProcessor appcOrchestratorPreProcessor;

    @MockBean
    protected SDNCActivateTasks sdncActivateTasks;

    @MockBean
    protected SDNCAssignTasks sdncAssignTasks;

    @MockBean
    protected SDNCUnassignTasks sdncUnassignTasks;

    @MockBean
    protected SDNCDeactivateTasks sdncDeactivateTasks;

    @MockBean
    protected SDNCQueryTasks sdncQueryTasks;

    @MockBean
    protected SDNCChangeAssignTasks sdncChangeAssignTasks;

    @MockBean
    protected NetworkAdapterDeleteTasks networkAdapterDeleteTasks;

    @MockBean
    protected NetworkAdapterUpdateTasks networkAdapterUpdateTasks;

    @MockBean
    protected VnfAdapterCreateTasks vnfAdapterCreateTasks;

    @MockBean
    protected NetworkAdapterCreateTasks networkAdapterCreateTasks;

    @MockBean
    protected CreateNetwork createNetwork;

    @MockBean
    protected AssignNetworkBBUtils assignNetworkBBUtils;

    @MockBean
    protected AssignNetwork assignNetwork;

    @MockBean
    protected CreateNetworkCollection createNetworkCollection;

    @MockBean
    protected VnfAdapterDeleteTasks vnfAdapterDeleteTasks;

    @MockBean
    protected AAICommonTasks aaiCommonTasks;

    @MockBean
    protected ActivateVfModule activateVfModule;

    @MockBean
    protected AssignVnf assignVnf;

    @MockBean
    protected UnassignVnf unassignVnf;

    @MockBean
    protected VnfAdapterImpl vnfAdapterImpl;

    @MockBean
    protected UnassignNetworkBB unassignNetworkBB;

    @MockBean
    protected OrchestrationStatusValidator orchestrationStatusValidator;

    @MockBean
    protected BBInputSetup bbInputSetup;

    @MockBean
    protected BBInputSetupUtils bbInputSetupUtils;

    @MockBean
    protected ExecuteBuildingBlockRainyDay executeBuildingBlockRainyDay;

    @MockBean
    protected WorkflowAction workflowAction;

    @MockBean
    protected WorkflowActionBBTasks workflowActionBBTasks;

    @MockBean
    protected GenericVnfHealthCheck genericVnfHealthCheck;

    @MockBean
    protected ConfigurationScaleOut configurationScaleOut;

    @MockBean
    protected FlowCompletionTasks flowCompletionTasks;

    @MockBean
    protected BuildingBlockValidatorRunner buildingBlockValidatorRunner;

    @MockBean
    protected SDNOHealthCheckTasks sdnoHealthCheckTasks;

    @MockBean
    protected SDNCClient sdncClient;

    @MockBean
    protected HomingV2 homing;

    @MockBean
    protected NamingServiceDeleteTasks namingServiceDeleteTasks;

    @MockBean
    protected NamingServiceCreateTasks namingServiceCreateTasks;

    @MockBean
    protected WorkflowActionBBFailure workflowActionBBFailure;

    @MockBean
    protected AuditTasks auditTasks;

    @MockBean
    protected ManualHandlingTasks manualHandlingTasks;

    @MockBean
    protected ControllerExecutionBB controllerExecutionBB;

    @MockBean
    protected ControllerExecutionDE controllerExecutionDE;

    @LocalServerPort
    protected int port;

    protected String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    @Before
    public void baseBefore() {
        variables.put("gBuildingBlockExecution", new DelegateExecutionImpl(new HashMap<>()));
    }

    @After
    public void baseAfter() {
        for (String deploymentId : mockedSubprocessList) {
            repositoryService.deleteDeployment(deploymentId);
        }
        mockedSubprocessList.clear();
    }

    /**
     * Create and deploy a process model with one logger delegate as service task.
     *
     * @param origProcessKey key to call
     * @param mockProcessName process name
     * @param fileName file name without extension
     */
    protected void mockSubprocess(String origProcessKey, String mockProcessName, String fileName) {
        mockSubprocess(origProcessKey, mockProcessName, fileName, new HashMap<String, String>());
    }

    /**
     * Create and deploy a process model with one logger delegate as service task.
     *
     * @param origProcessKey key to call
     * @param mockProcessName process name
     * @param fileName file name without extension
     * @param outParam output parameters
     */
    protected void mockSubprocess(String origProcessKey, String mockProcessName, String fileName,
            Map<String, String> outParam) {
        ServiceTaskBuilder builder = Bpmn.createExecutableProcess(origProcessKey).name(mockProcessName).startEvent()
                .name("Start_Event").serviceTask().name("Mock_Delegate").camundaClass(FluentJavaDelegateMock.class);

        for (String key : outParam.keySet()) {
            builder.camundaOutputParameter(key, outParam.get(key));
        }

        BpmnModelInstance modelInstance = builder.endEvent().name("End_Event").done();
        mockedSubprocessList.add(repositoryService.createDeployment()
                .addModelInstance(fileName + ".bpmn", modelInstance).deploy().getId());
    }

    protected void processExternalTasks(ProcessInstance pi, String taskName) {
        assertThat(pi).isWaitingAt(taskName);
        List<LockedExternalTask> tasks =
                externalTaskService.fetchAndLock(100, "externalWorkerId").topic("AppcService", 60L * 1000L).execute();
        while (!tasks.isEmpty()) {
            for (LockedExternalTask task : tasks) {
                externalTaskService.complete(task.getId(), "externalWorkerId");
            }
            tasks = externalTaskService.fetchAndLock(100, "externalWorkerId").topic("AppcService", 60L * 1000L)
                    .execute();
        }
    }
}
