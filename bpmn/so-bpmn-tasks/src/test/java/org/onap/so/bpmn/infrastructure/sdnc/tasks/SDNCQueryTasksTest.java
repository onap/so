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
import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.BadResponseException;
import org.onap.logging.filter.base.ONAPComponentsList;
import org.onap.logging.filter.base.ONAPComponents;

public class SDNCQueryTasksTest extends BaseTaskTest {
    @InjectMocks
    private SDNCQueryTasks sdncQueryTasks = new SDNCQueryTasks();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ServiceInstance serviceInstance;
    private GenericVnf genericVnf;
    private VfModule vfModule;

    @Before
    public void before() throws BBObjectNotFoundException {
        serviceInstance = setServiceInstance();
        genericVnf = setGenericVnf();
        vfModule = setVfModule();

        doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(
                any(BuildingBlockExecution.class), eq(7000), any(Exception.class), any(ONAPComponentsList.class));
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID)))
                .thenReturn(serviceInstance);

        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID)))
                .thenReturn(genericVnf);

        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID))).thenReturn(vfModule);

    }

    @Test
    public void queryVfModuleTest() throws Exception {
        String sdncQueryResponse = "response";
        vfModule.setSelflink("vfModuleSelfLink");

        doReturn(sdncQueryResponse).when(sdncVfModuleResources).queryVfModule(vfModule);

        assertNotEquals(sdncQueryResponse, execution.getVariable("SDNCQueryResponse_" + vfModule.getVfModuleId()));
        sdncQueryTasks.queryVfModule(execution);
        assertEquals(sdncQueryResponse, execution.getVariable("SDNCQueryResponse_" + vfModule.getVfModuleId()));

        verify(sdncVfModuleResources, times(1)).queryVfModule(vfModule);
    }

    @Test
    public void queryVfModuleBadResponseExceptionTest() throws Exception {
        BadResponseException exception = new BadResponseException("Error received from SDNC");
        doThrow(exception).when(sdncVfModuleResources).queryVfModule(vfModule);

        expectedException.expect(BpmnError.class);
        sdncQueryTasks.queryVfModule(execution);

        verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(execution, 700, exception, ONAPComponents.SDNC);
    }

    @Test
    public void queryVfModuleResponseExceptionNoResponseTest() throws Exception {
        BadResponseException exception = new BadResponseException("Error did not receive a response from SDNC.");
        doThrow(exception).when(sdncVfModuleResources).queryVfModule(vfModule);

        expectedException.expect(BpmnError.class);
        sdncQueryTasks.queryVfModule(execution);

        verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(execution, 700, exception, ONAPComponents.SO);
    }

    @Test
    public void queryVnfTest() throws Exception {
        String sdncQueryResponse = "response";

        doReturn(sdncQueryResponse).when(sdncVnfResources).queryVnf(genericVnf);

        assertNotEquals(sdncQueryResponse, execution.getVariable("SDNCQueryResponse_" + genericVnf.getVnfId()));
        sdncQueryTasks.queryVnf(execution);
        assertEquals(sdncQueryResponse, execution.getVariable("SDNCQueryResponse_" + genericVnf.getVnfId()));

        verify(sdncVnfResources, times(1)).queryVnf(genericVnf);
    }

    @Test
    public void queryVnfBadResponseExceptionTest() throws Exception {
        BadResponseException exception = new BadResponseException("Error received from SDNC");
        doThrow(exception).when(sdncVnfResources).queryVnf(genericVnf);

        expectedException.expect(BpmnError.class);
        sdncQueryTasks.queryVnf(execution);

        verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(execution, 700, exception, ONAPComponents.SDNC);
    }

    @Test
    public void queryVnfBadResponseExceptionNoResponseTest() throws Exception {
        BadResponseException exception = new BadResponseException("Error did not receive a response from SDNC.");
        doThrow(exception).when(sdncVnfResources).queryVnf(genericVnf);

        expectedException.expect(BpmnError.class);
        sdncQueryTasks.queryVnf(execution);

        verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(execution, 700, exception, ONAPComponents.SO);
    }

    @Test
    public void queryVfModuleForVolumeGroupTest() throws Exception {
        String sdncQueryResponse = "response";
        vfModule.setSelflink("vfModuleSelfLink");

        doReturn(sdncQueryResponse).when(sdncVfModuleResources).queryVfModule(vfModule);

        assertNotEquals(sdncQueryResponse, execution.getVariable("SDNCQueryResponse_" + vfModule.getVfModuleId()));
        sdncQueryTasks.queryVfModuleForVolumeGroup(execution);
        assertEquals(sdncQueryResponse, execution.getVariable("SDNCQueryResponse_" + vfModule.getVfModuleId()));

        verify(sdncVfModuleResources, times(1)).queryVfModule(vfModule);
    }

    @Test
    public void queryVfModuleForVolumeGroupBadResponseExceptionTest() throws Exception {
        BadResponseException exception = new BadResponseException("Error received from SDNC");
        doThrow(exception).when(sdncVfModuleResources).queryVfModule(vfModule);

        expectedException.expect(BpmnError.class);
        sdncQueryTasks.queryVfModuleForVolumeGroup(execution);

        verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(execution, 700, exception, ONAPComponents.SDNC);
    }

    @Test
    public void queryVfModuleForVolumeGroupBadResponseExceptionNoResponseTest() throws Exception {
        BadResponseException exception = new BadResponseException("Error did not receive a response from SDNC.");
        doThrow(exception).when(sdncVfModuleResources).queryVfModule(vfModule);

        expectedException.expect(BpmnError.class);
        sdncQueryTasks.queryVfModuleForVolumeGroup(execution);

        verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(execution, 700, exception, ONAPComponents.SO);
    }

    @Test
    public void queryVfModuleForVolumeGroupNoSelfLinkExceptionTest() {
        expectedException.expect(BpmnError.class);

        vfModule.setSelflink("");

        sdncQueryTasks.queryVfModuleForVolumeGroup(execution);
    }

    @Test
    public void queryVfModuleForVolumeGroupVfObjectExceptionTest() throws Exception {
        expectedException.expect(BpmnError.class);
        doThrow(RuntimeException.class).when(extractPojosForBB).extractByKey(any(),
                ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID));
        sdncQueryTasks.queryVfModuleForVolumeGroup(execution);

        verify(sdncVfModuleResources, times(0)).queryVfModule(any(VfModule.class));
    }

    @Test
    public void queryVfModuleForVolumeGroupNonVfObjectExceptionTest() {
        expectedException.expect(BpmnError.class);

        sdncQueryTasks.queryVfModuleForVolumeGroup(execution);
    }
}
