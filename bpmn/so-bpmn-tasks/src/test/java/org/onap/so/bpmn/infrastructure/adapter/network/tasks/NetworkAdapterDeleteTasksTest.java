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

package org.onap.so.bpmn.infrastructure.adapter.network.tasks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.nwrest.DeleteNetworkRequest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.adapter.network.mapper.NetworkAdapterObjectMapper;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;

@RunWith(MockitoJUnitRunner.Silent.class)
public class NetworkAdapterDeleteTasksTest extends TestDataSetup {

    @Mock
    protected ExtractPojosForBB extractPojosForBB;
    @Mock
    protected ExceptionBuilder exceptionUtil;
    @Mock
    protected NetworkAdapterObjectMapper networkAdapterObjectMapper;
    @InjectMocks
    private NetworkAdapterDeleteTasks networkAdapterDeleteTasks = new NetworkAdapterDeleteTasks();

    private ServiceInstance serviceInstance;
    private L3Network l3Network;
    private RequestContext requestContext;
    private CloudRegion cloudRegion;


    @Before
    public void before() throws BBObjectNotFoundException {
        serviceInstance = setServiceInstance();
        l3Network = setL3Network();
        requestContext = setRequestContext();
        cloudRegion = setCloudRegion();

        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.NETWORK_ID))).thenReturn(l3Network);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID)))
                .thenReturn(serviceInstance);
        doThrow(new BpmnError("BPMN Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
    }

    @Test
    public void test_deleteNetwork() {
        DeleteNetworkRequest deleteNetworkRequest = new DeleteNetworkRequest();
        doReturn(deleteNetworkRequest).when(networkAdapterObjectMapper).deleteNetworkRequestMapper(requestContext,
                cloudRegion, serviceInstance, l3Network);

        networkAdapterDeleteTasks.deleteNetwork(execution);
        verify(networkAdapterObjectMapper, times(1)).deleteNetworkRequestMapper(requestContext, cloudRegion,
                serviceInstance, l3Network);
    }

    @Test
    public void test_deleteNetwork_exception() {
        expectedException.expect(BpmnError.class);

        doThrow(RuntimeException.class).when(networkAdapterObjectMapper).deleteNetworkRequestMapper(
                any(RequestContext.class), any(CloudRegion.class), any(ServiceInstance.class), eq(l3Network));
        networkAdapterDeleteTasks.deleteNetwork(execution);
    }
}
