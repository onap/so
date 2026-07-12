/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIConfigurationResources;
import org.onap.so.db.catalog.beans.macro.NorthBoundRequest;
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;
import org.onap.so.db.catalog.client.CatalogDbClient;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowActionUnitTest {

    private final static String JSON_FILE_LOCATION = "src/test/resources/__files/Macro/";

    @Mock
    private CatalogDbClient catalogDbClient;
    @Mock
    private BBInputSetup bbInputSetup;
    @Mock
    private BBInputSetupUtils bbInputSetupUtils;
    @Mock
    private ExceptionBuilder exceptionBuilder;
    @Mock
    private AAIConfigurationResources aaiConfigurationResources;

    @InjectMocks
    @Spy
    private WorkflowAction workflowAction;

    /**
     * This test previously targeted {@code WorkflowAction.traverseCatalogDbForConfiguration(vnfId, vfModuleId)}, but
     * that method no longer exists on {@link WorkflowAction}: the "SO WorkflowAction refactor" (git ea0e53188) moved
     * the configuration-traversal logic into {@code UserParamsServiceTraversal}, where it is now a private method with
     * the signature
     * {@code List<CvnfcConfigurationCustomization> traverseCatalogDbForConfiguration(String serviceModelUUID)} that
     * delegates to {@code catalogDbClient.getCvnfcCustomization(serviceModelUUID, vnfCustomizationUUID,
     * vfModuleCustomizationUUID)}. That behavior is already covered by {@code UserParamsServiceTraversalTest}.
     *
     * Rather than resurrect a dead signature (or duplicate the UserParamsServiceTraversal coverage), this test is
     * retargeted to the closest still-present, catalog-driven method on {@link WorkflowAction} that returns an
     * assertable list: {@link WorkflowAction#queryNorthBoundRequestCatalogDb}. It verifies that the orchestration flow
     * list configured on the matched NorthBoundRequest is returned, in sequence order, for BB-style flow names (which
     * are passed through directly rather than expanded via getOrchestrationFlowByAction).
     */
    @Test
    public void queryNorthBoundRequestCatalogDbReturnsOrchestrationFlows() {
        DelegateExecution execution = new DelegateExecutionFake();
        String requestAction = "createInstance";
        WorkflowType resourceType = WorkflowType.SERVICE;
        boolean aLaCarte = true;
        String cloudOwner = "my-custom-cloud-owner";

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        northBoundRequest.setIsToplevelflow(true);
        northBoundRequest.setOrchestrationFlowList(
                createFlowList("AssignServiceInstanceBB", "CreateServiceInstanceBB", "ActivateServiceInstanceBB"));

        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(eq(requestAction),
                eq(resourceType.toString()), eq(aLaCarte), eq(cloudOwner))).thenReturn(northBoundRequest);

        List<OrchestrationFlow> result = workflowAction.queryNorthBoundRequestCatalogDb(execution, requestAction,
                resourceType, aLaCarte, cloudOwner);

        List<String> flowNames = result.stream().map(OrchestrationFlow::getFlowName).collect(Collectors.toList());
        assertThat(flowNames,
                contains("AssignServiceInstanceBB", "CreateServiceInstanceBB", "ActivateServiceInstanceBB"));
    }

    private String getJson(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + filename)));
    }

    private List<OrchestrationFlow> createFlowList(String... myList) {

        List<OrchestrationFlow> result = new ArrayList<>();
        int sequenceNumber = 1;
        for (String name : myList) {
            OrchestrationFlow flow = new OrchestrationFlow();
            flow.setFlowName(name);
            flow.setSequenceNumber(sequenceNumber++);
            result.add(flow);
        }

        return result;

    }
}
