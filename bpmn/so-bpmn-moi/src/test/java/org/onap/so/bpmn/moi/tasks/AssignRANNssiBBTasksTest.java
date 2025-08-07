/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2022 Deutsche telekom
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


package org.onap.so.bpmn.moi.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.onap.aai.domain.yang.Customer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.ServiceSubscription;
import org.onap.aaiclient.client.aai.AAIRestClientImpl;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.moi.util.SliceProfileAaiToMoiMapperUtil;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)

public class AssignRANNssiBBTasksTest {
    @Mock
    protected InjectionHelper injectionHelper;
    @Mock
    protected ObjectMapper mapper = new ObjectMapper();
    @Mock
    protected AAIRestClientImpl aaiRestClient = new AAIRestClientImpl();
    @Mock
    protected SliceProfileAaiToMoiMapperUtil mapperUtil;
    @Mock
    GeneralBuildingBlock gBB;
    @Mock
    ServiceInstance serviceInstance;
    @Mock
    protected ExceptionBuilder exceptionUtil;
    @Mock
    AssignRANNssiBBTasks assignRANNssiBBTasks;
    @Mock
    private BuildingBlockExecution execution;
    @Mock
    private ModelInfoServiceInstance modelInfoServiceInstance;
    @Mock
    protected ExtractPojosForBB extractPojosForBBMock;


    private String operationalState;
    private String administrativeState;

    private String sliceProfileServiceInstanceId = "215";

    private String serviceInstanceId = "123";

    Customer customerA = new Customer();

    @Mock
    private AAIResourceUri allotedResourceURI;
    String serviceType;

    String globalCustomerId;

    private UUID allottedResourceUuidUuid = UUID.randomUUID();

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    private ServiceSubscription serviceSubscription;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() throws BBObjectNotFoundException {

        serviceInstance.setServiceInstanceId("123");
        modelInfoServiceInstance.setModelUuid("235");
        ServiceInstance serviceInstance = new ServiceInstance();
        when(extractPojosForBBMock.extractByKey(any(), eq(ResourceKey.SERVICE_INSTANCE_ID)))
                .thenReturn(serviceInstanceId);
        when(extractPojosForBBMock.extractByKey(any(), eq(ResourceKey.ServiceInstance))).thenReturn(serviceInstance);
        when(extractPojosForBBMock.extractByKey(any(), eq(ResourceKey.ModelInfoServiceInstance)))
                .thenReturn(modelInfoServiceInstance);
        when(extractPojosForBBMock.extractByKey(any(), eq(ResourceKey.operationalState))).thenReturn(operationalState);
        when(extractPojosForBBMock.extractByKey(any(), eq(ResourceKey.administrativeState)))
                .thenReturn(administrativeState);
        serviceSubscription = new ServiceSubscription();
        serviceSubscription.setServiceType(serviceType);
        doThrow(new BpmnError("BPMN Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
    }

    @Test
    public void createNssiTest() throws Exception {
        doNothing().when(assignRANNssiBBTasks).createNssi(execution);
        assignRANNssiBBTasks.createNssi(execution);
        verify(assignRANNssiBBTasks, times(1)).createNssi(execution);
        System.out.println("sucess");
    }

    @Test
    public void createSliceProfileInstanceTest() {
        doNothing().when(assignRANNssiBBTasks).createSliceProfileInstance(execution);
        assignRANNssiBBTasks.createSliceProfileInstance(execution);
        verify(assignRANNssiBBTasks, times(1)).createSliceProfileInstance(execution);
        System.out.println("sucess");
    }

    @Test
    public void allotResourcesTest() {
        doNothing().when(assignRANNssiBBTasks).allotResources(execution);
        assignRANNssiBBTasks.allotResources(execution);
        verify(assignRANNssiBBTasks, times(1)).allotResources(execution);
    }

    @Test
    public void addSliceProfileToNssiTest() {
        doNothing().when(assignRANNssiBBTasks).addSliceProfileToNssi(execution);
        assignRANNssiBBTasks.addSliceProfileToNssi(execution);
        verify(assignRANNssiBBTasks, times(1)).addSliceProfileToNssi(execution);
        System.out.println("sucess");
    }

    @Test
    public void activateNssiTest() {
        doNothing().when(assignRANNssiBBTasks).activateNssi(execution);
        assignRANNssiBBTasks.activateNssi(execution);
        verify(assignRANNssiBBTasks, times(1)).activateNssi(execution);
    }

}
