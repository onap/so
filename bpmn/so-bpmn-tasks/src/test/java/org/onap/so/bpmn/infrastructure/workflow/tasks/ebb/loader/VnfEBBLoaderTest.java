/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2021 Nokia
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2021 Nokia
 * ================================================================================
 * Modifications Copyright (c) 2020 Tech Mahindra
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

package org.onap.so.bpmn.infrastructure.workflow.tasks.ebb.loader;


import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.javatuples.Pair;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionExtractResourcesAAI;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowType;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.bpmn.infrastructure.workflow.tasks.Resource;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


public class VnfEBBLoaderTest extends BaseTaskTest {

    private String serviceId;
    private String vnfId;
    private BBInputSetupUtils bbInputSetupUtils;
    private BBInputSetup bbInputSetup;
    private WorkflowActionExtractResourcesAAI workflowActionUtils;
    private ExceptionBuilder exceptionBuilder;
    private DelegateExecution delegateExecution;
    private VnfEBBLoader cut;

    private org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI;
    private ServiceInstance serviceInstanceMSO;

    @Before
    public void setup() {
        serviceId = "service123";
        vnfId = "vnf123";
        serviceInstanceAAI = mock(org.onap.aai.domain.yang.ServiceInstance.class);
        serviceInstanceMSO = mock(ServiceInstance.class);
        bbInputSetupUtils = mock(BBInputSetupUtils.class);
        bbInputSetup = mock(BBInputSetup.class);
        workflowActionUtils = mock(WorkflowActionExtractResourcesAAI.class);
        exceptionBuilder = mock(ExceptionBuilder.class);
        delegateExecution = new DelegateExecutionFake();
    }

    @Test
    public void traverseAAIVnf_shouldAddServiceToResourceList() throws Exception {
        List<Resource> resourceList = new ArrayList<>();
        List<Pair<WorkflowType, String>> aaiResourceIds = new ArrayList<>();
        doReturn(serviceInstanceAAI).when(bbInputSetupUtils).getAAIServiceInstanceById(serviceId);
        doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
        cut = new VnfEBBLoader(bbInputSetupUtils, bbInputSetup, workflowActionUtils, exceptionBuilder);
        cut.traverseAAIVnf(delegateExecution, resourceList, serviceId, vnfId, aaiResourceIds);
        assertEquals(WorkflowType.SERVICE, resourceList.get(0).getResourceType());
    }

    @Test(expected = NullPointerException.class)
    public void traverseAAIVnf_shouldReturnExceptionDueToLackOfVnfsToExecute() {
        ExceptionBuilder exceptionBuilder = new ExceptionBuilder();
        List<Resource> resourceList = new ArrayList<>();
        List<Pair<WorkflowType, String>> aaiResourceIds = new ArrayList<>();
        doReturn(serviceInstanceAAI).when(bbInputSetupUtils).getAAIServiceInstanceById(serviceId);
        cut = new VnfEBBLoader(bbInputSetupUtils, bbInputSetup, workflowActionUtils, exceptionBuilder);
        cut.traverseAAIVnf(delegateExecution, resourceList, serviceId, vnfId, aaiResourceIds);
    }
}
