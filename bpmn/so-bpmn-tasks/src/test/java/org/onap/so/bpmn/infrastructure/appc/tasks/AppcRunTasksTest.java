/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.infrastructure.appc.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.onap.appc.client.lcm.model.Action;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;

public class AppcRunTasksTest extends BaseTaskTest {

	@InjectMocks
	private AppcRunTasks appcRunTasks = new AppcRunTasks();

	@Test
	public void mapRollbackVariablesTest() {
		
		BuildingBlockExecution mock = mock(BuildingBlockExecution.class);
		
		appcRunTasks.mapRollbackVariables(mock, Action.Lock, "1");
		verify(mock, times(0)).setVariable(any(String.class), any());
		appcRunTasks.mapRollbackVariables(mock, Action.Lock, "0");
		verify(mock, times(1)).setVariable("rollbackVnfLock", true);
		appcRunTasks.mapRollbackVariables(mock, Action.Unlock, "0");
		verify(mock, times(1)).setVariable("rollbackVnfLock", false);
		appcRunTasks.mapRollbackVariables(mock, Action.Start, "0");
		verify(mock, times(1)).setVariable("rollbackVnfStop", false);
		appcRunTasks.mapRollbackVariables(mock, Action.Stop, "0");
		verify(mock, times(1)).setVariable("rollbackVnfStop", true);
		appcRunTasks.mapRollbackVariables(mock, Action.QuiesceTraffic, "0");
		verify(mock, times(1)).setVariable("rollbackQuiesceTraffic", true);
		appcRunTasks.mapRollbackVariables(mock, Action.ResumeTraffic, "0");
		verify(mock, times(1)).setVariable("rollbackQuiesceTraffic", false);
	}

    @Test
    public void runAppcCommandVnfNull() throws BBObjectNotFoundException {
        execution.getLookupMap().put(ResourceKey.GENERIC_VNF_ID, "NULL-TEST");
        fillRequiredAppcExecutionFields();
        when(extractPojosForBB.extractByKey(eq(execution), eq(ResourceKey.GENERIC_VNF_ID)))
            .thenReturn(null);
        when(catalogDbClient.getControllerSelectionReferenceByVnfTypeAndActionCategory(
            isNull(), eq(Action.Lock.toString()))).
            thenThrow(new IllegalArgumentException("name or values is null"));

        appcRunTasks.runAppcCommand(execution, Action.Lock);

        // if vnf = null -> vnfType = null ->
        // IllegalArgumentException will be thrown in catalogDbClient.getControllerSelectionReferenceByVnfTypeAndActionCategory
        verify(exceptionUtil, times(1)).
            buildAndThrowWorkflowException(
                any(BuildingBlockExecution.class), eq(1002), eq("name or values is null"));
    }

    @Test
    public void runAppcCommandBBObjectNotFoundException() throws BBObjectNotFoundException {
        execution.getLookupMap().put(ResourceKey.GENERIC_VNF_ID, "EXCEPTION-TEST");
        fillRequiredAppcExecutionFields();
        when(extractPojosForBB.extractByKey(eq(execution), eq(ResourceKey.GENERIC_VNF_ID)))
            .thenThrow(new BBObjectNotFoundException());

        appcRunTasks.runAppcCommand(execution, Action.Lock);

        verify(exceptionUtil, times(1)).
            buildAndThrowWorkflowException(
                any(BuildingBlockExecution.class), eq(7000), eq("No valid VNF exists"));
    }

    @Test
    public void runAppcCommandVfModuleNull() throws BBObjectNotFoundException {
        execution.getLookupMap().put(ResourceKey.GENERIC_VNF_ID, "SUCCESS-TEST");
        fillRequiredAppcExecutionFields();
        GenericVnf genericVnf = getTestGenericVnf();
        when(extractPojosForBB.extractByKey(eq(execution), eq(ResourceKey.GENERIC_VNF_ID)))
            .thenReturn(genericVnf);
        mockReferenceResponse();
        execution.getLookupMap().put(ResourceKey.VF_MODULE_ID, "VF-MODULE-ID-TEST");
        when(extractPojosForBB.extractByKey(eq(execution), eq(ResourceKey.VF_MODULE_ID)))
            .thenReturn(null);
        when(appCClient.getErrorCode()).thenReturn("0");

        appcRunTasks.runAppcCommand(execution, Action.Lock);

        assertEquals(true, execution.getVariable("rollbackVnfLock"));
    }

    @Test
    public void runAppcCommand() throws BBObjectNotFoundException {
        execution.getLookupMap().put(ResourceKey.GENERIC_VNF_ID, "SUCCESS-TEST");
        fillRequiredAppcExecutionFields();
        GenericVnf genericVnf = getTestGenericVnf();
        when(extractPojosForBB.extractByKey(eq(execution), eq(ResourceKey.GENERIC_VNF_ID)))
            .thenReturn(genericVnf);
        mockReferenceResponse();
        execution.getLookupMap().put(ResourceKey.VF_MODULE_ID, "VF-MODULE-ID-TEST");
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("VF-MODULE-ID");
        when(extractPojosForBB.extractByKey(eq(execution), eq(ResourceKey.VF_MODULE_ID)))
            .thenReturn(vfModule);
        when(appCClient.getErrorCode()).thenReturn("0");

        appcRunTasks.runAppcCommand(execution, Action.Lock);

        assertEquals(true, execution.getVariable("rollbackVnfLock"));
    }

    private void mockReferenceResponse() {
        ControllerSelectionReference reference = new ControllerSelectionReference();
        reference.setControllerName("TEST-CONTROLLER-NAME");
        when(catalogDbClient.getControllerSelectionReferenceByVnfTypeAndActionCategory(
            eq("TEST-VNF-TYPE"), eq(Action.Lock.toString()))).thenReturn(reference);
    }

    private void fillRequiredAppcExecutionFields() {
        RequestContext context = new RequestContext();
        context.setMsoRequestId("TEST-MSO-ID");
        execution.setVariable("aicIdentity", "AIC-TEST");
        execution.setVariable("vmIdList", "VM-ID-LIST-TEST");
        execution.setVariable("vserverIdList", "VSERVER-ID-LIST");
        execution.setVariable("identityUrl", "IDENTITY-URL-TEST");
        execution.getGeneralBuildingBlock().setRequestContext(context);
    }

    private GenericVnf getTestGenericVnf() {
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("TEST-VNF-ID");
        genericVnf.setVnfType("TEST-VNF-TYPE");
        genericVnf.setVnfName("TEST-VNF-NAME");
        genericVnf.setIpv4OamAddress("129.0.0.1");
        return genericVnf;
    }
}
