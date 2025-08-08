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

package org.onap.so.bpmn.infrastructure.sdnc.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.net.URI;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.onap.sdnc.northbound.client.model.GenericResourceApiNetworkOperationInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiServiceOperationInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVfModuleOperationInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfOperationInformation;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.sdnc.beans.SDNCRequest;
import org.onap.so.client.sdnc.endpoint.SDNCTopology;

public class SDNCUnassignTasksTest extends BaseTaskTest {
    @InjectMocks
    private SDNCUnassignTasks sdncUnassignTasks = new SDNCUnassignTasks();

    private ServiceInstance serviceInstance;
    private RequestContext requestContext;
    private GenericVnf genericVnf;
    private VfModule vfModule;
    private CloudRegion cloudRegion;
    private L3Network network;
    private Customer customer;

    @Before
    public void before() throws BBObjectNotFoundException {
        customer = setCustomer();
        serviceInstance = setServiceInstance();
        requestContext = setRequestContext();
        genericVnf = setGenericVnf();
        vfModule = setVfModule();
        cloudRegion = setCloudRegion();
        network = setL3Network();
        doThrow(new BpmnError("BPMN Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID)))
                .thenReturn(genericVnf);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.NETWORK_ID))).thenReturn(network);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID))).thenReturn(vfModule);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID)))
                .thenReturn(serviceInstance);
        when(env.getRequiredProperty("mso.workflow.message.endpoint")).thenReturn("http://localhost:9090");
    }

    @Test
    public void unassignServiceInstanceTest() {
        doReturn(new GenericResourceApiServiceOperationInformation()).when(sdncServiceInstanceResources)
                .unassignServiceInstance(serviceInstance, customer, requestContext);
        sdncUnassignTasks.unassignServiceInstance(execution);
        verify(sdncServiceInstanceResources, times(1)).unassignServiceInstance(serviceInstance, customer,
                requestContext);
        SDNCRequest sdncRequest = execution.getVariable("SDNCRequest");
        assertEquals(SDNCTopology.SERVICE, sdncRequest.getTopology());
    }

    @Test
    public void unassignServiceInstanceExceptionTest() {
        expectedException.expect(BpmnError.class);
        doThrow(RuntimeException.class).when(sdncServiceInstanceResources).unassignServiceInstance(serviceInstance,
                customer, requestContext);
        sdncUnassignTasks.unassignServiceInstance(execution);
    }

    @Test
    public void unassignVfModuleTest() throws Exception {
        doReturn(new GenericResourceApiVfModuleOperationInformation()).when(sdncVfModuleResources).unassignVfModule(
                eq(vfModule), eq(genericVnf), eq(serviceInstance), eq(requestContext), any(URI.class));
        sdncUnassignTasks.unassignVfModule(execution);
        verify(sdncVfModuleResources, times(1)).unassignVfModule(eq(vfModule), eq(genericVnf), eq(serviceInstance),
                eq(requestContext), any(URI.class));
        SDNCRequest sdncRequest = execution.getVariable("SDNCRequest");
        assertEquals(SDNCTopology.VFMODULE, sdncRequest.getTopology());
    }

    @Test
    public void unassignVfModuleExceptionTest() throws Exception {
        expectedException.expect(BpmnError.class);
        doThrow(RuntimeException.class).when(sdncVfModuleResources).unassignVfModule(eq(vfModule), eq(genericVnf),
                eq(serviceInstance), eq(requestContext), any(URI.class));
        sdncUnassignTasks.unassignVfModule(execution);
    }

    @Test
    public void unassignVnfTest() {
        doReturn(new GenericResourceApiVnfOperationInformation()).when(sdncVnfResources).unassignVnf(eq(genericVnf),
                eq(serviceInstance), eq(customer), eq(cloudRegion), eq(requestContext), any(URI.class));
        sdncUnassignTasks.unassignVnf(execution);
        verify(sdncVnfResources, times(1)).unassignVnf(eq(genericVnf), eq(serviceInstance), eq(customer),
                eq(cloudRegion), eq(requestContext), any(URI.class));
        SDNCRequest sdncRequest = execution.getVariable("SDNCRequest");
        assertEquals(SDNCTopology.VNF, sdncRequest.getTopology());
    }

    @Test
    public void unassignVnfExceptionTest() {
        expectedException.expect(BpmnError.class);
        doThrow(RuntimeException.class).when(sdncVnfResources).unassignVnf(eq(genericVnf), eq(serviceInstance),
                eq(customer), eq(cloudRegion), eq(requestContext), any(URI.class));
        sdncUnassignTasks.unassignVnf(execution);
    }

    @Test
    public void unassignNetworkTest() throws Exception {
        String cloudRegionSdnc = "AAIAIC25";
        cloudRegion.setCloudRegionVersion("2.5");
        execution.setVariable("cloudRegionSdnc", cloudRegionSdnc);
        doReturn(new GenericResourceApiNetworkOperationInformation()).when(sdncNetworkResources)
                .unassignNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
        assertNotEquals(cloudRegionSdnc, cloudRegion.getLcpCloudRegionId());
        sdncUnassignTasks.unassignNetwork(execution);
        verify(sdncNetworkResources, times(1)).unassignNetwork(network, serviceInstance, customer, requestContext,
                cloudRegion);
        assertEquals(cloudRegionSdnc, cloudRegion.getLcpCloudRegionId());
        SDNCRequest sdncRequest = execution.getVariable("SDNCRequest");
        assertEquals(SDNCTopology.NETWORK, sdncRequest.getTopology());
    }
}
