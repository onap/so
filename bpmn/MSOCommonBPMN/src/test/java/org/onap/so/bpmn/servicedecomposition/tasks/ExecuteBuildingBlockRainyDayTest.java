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

package org.onap.so.bpmn.servicedecomposition.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.BaseTest;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.constants.Status;
import org.onap.so.db.catalog.beans.macro.RainyDayHandlerStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class ExecuteBuildingBlockRainyDayTest extends BaseTest {
    @Autowired
    private ExecuteBuildingBlockRainyDay executeBuildingBlockRainyDay;

    private ServiceInstance serviceInstance;
    private Customer customer; // will build service sub
    private GenericVnf vnf;
    private static final String ASTERISK = "*";

    @Before
    public void before() {
        serviceInstance = setServiceInstance();
        customer = setCustomer();
        vnf = setGenericVnf();

        BuildingBlock buildingBlock = new BuildingBlock().setBpmnFlowName("AssignServiceInstanceBB");
        ExecuteBuildingBlock executeBuildingBlock = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock);

        delegateExecution.setVariable("gBBInput", gBBInput);
        delegateExecution.setVariable("WorkflowException", new WorkflowException("", 7000, ""));
        delegateExecution.setVariable("buildingBlock", executeBuildingBlock);
        delegateExecution.setVariable("lookupKeyMap", lookupKeyMap);

        delegateExecution.setVariable("WorkflowException", new WorkflowException("processKey", 7000, "errorMessage"));
    }

    @Test
    public void setRetryTimerTest() {
        delegateExecution.setVariable("retryCount", 2);
        executeBuildingBlockRainyDay.setRetryTimer(delegateExecution);
        assertEquals("PT40S", delegateExecution.getVariable("RetryDuration"));
    }

    @Test
    public void setRetryTimerExceptionTest() {
        expectedException.expect(BpmnError.class);
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable(eq("retryCount"))).thenThrow(BpmnError.class);
        executeBuildingBlockRainyDay.setRetryTimer(execution);
    }

    @Test
    public void queryRainyDayTableExists() {
        customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
        serviceInstance.getModelInfoServiceInstance().setServiceType("st1");
        vnf.setVnfType("vnft1");
        delegateExecution.setVariable("aLaCarte", true);
        delegateExecution.setVariable("suppressRollback", false);
        delegateExecution.setVariable("WorkflowExceptionCode", "7000");
        RainyDayHandlerStatus rainyDayHandlerStatus = new RainyDayHandlerStatus();
        rainyDayHandlerStatus.setErrorCode("7000");
        rainyDayHandlerStatus.setFlowName("AssignServiceInstanceBB");
        rainyDayHandlerStatus.setServiceType("st1");
        rainyDayHandlerStatus.setVnfType("vnft1");
        rainyDayHandlerStatus.setPolicy("Rollback");
        rainyDayHandlerStatus.setWorkStep(ASTERISK);

        doReturn(rainyDayHandlerStatus).when(MOCK_catalogDbClient).getRainyDayHandlerStatus("AssignServiceInstanceBB",
                "st1", "vnft1", "7000", "*", "errorMessage", "*");

        executeBuildingBlockRainyDay.queryRainyDayTable(delegateExecution, true);
        assertEquals("Rollback", delegateExecution.getVariable("handlingCode"));
    }

    @Test
    public void queryRainyDayTableDefault() {
        customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
        serviceInstance.getModelInfoServiceInstance().setServiceType("st1");
        vnf.setVnfType("vnft1");
        delegateExecution.setVariable("aLaCarte", true);
        delegateExecution.setVariable("suppressRollback", false);
        delegateExecution.setVariable("WorkflowExceptionCode", ASTERISK);
        RainyDayHandlerStatus rainyDayHandlerStatus = new RainyDayHandlerStatus();
        rainyDayHandlerStatus.setErrorCode(ASTERISK);
        rainyDayHandlerStatus.setFlowName("AssignServiceInstanceBB");
        rainyDayHandlerStatus.setServiceType(ASTERISK);
        rainyDayHandlerStatus.setVnfType(ASTERISK);
        rainyDayHandlerStatus.setPolicy("Rollback");
        rainyDayHandlerStatus.setWorkStep(ASTERISK);

        doReturn(rainyDayHandlerStatus).when(MOCK_catalogDbClient).getRainyDayHandlerStatus("AssignServiceInstanceBB",
                "st1", "vnft1", ASTERISK, ASTERISK, "errorMessage", "*");
        executeBuildingBlockRainyDay.queryRainyDayTable(delegateExecution, true);
        assertEquals("Rollback", delegateExecution.getVariable("handlingCode"));
        assertEquals(5, delegateExecution.getVariable("maxRetries"));
    }

    @Test
    public void queryRainyDayTableDoesNotExist() {
        customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
        serviceInstance.getModelInfoServiceInstance().setServiceType("st1");
        vnf.setVnfType("vnft1");
        delegateExecution.setVariable("aLaCarte", true);
        doReturn(null).when(MOCK_catalogDbClient).getRainyDayHandlerStatus(isA(String.class), isA(String.class),
                isA(String.class), isA(String.class), isA(String.class), isA(String.class), isA(String.class));
        delegateExecution.setVariable("suppressRollback", false);

        executeBuildingBlockRainyDay.queryRainyDayTable(delegateExecution, true);

        assertEquals("Abort", delegateExecution.getVariable("handlingCode"));
    }

    @Test
    public void queryRainyDayTableExceptionTest() {
        doThrow(RuntimeException.class).when(MOCK_catalogDbClient).getRainyDayHandlerStatus(isA(String.class),
                isA(String.class), isA(String.class), isA(String.class), isA(String.class), isA(String.class),
                isA(String.class));
        delegateExecution.setVariable("aLaCarte", true);
        executeBuildingBlockRainyDay.queryRainyDayTable(delegateExecution, true);
        delegateExecution.setVariable("suppressRollback", false);

        assertEquals("Abort", delegateExecution.getVariable("handlingCode"));
    }

    @Test
    public void queryRainyDayTableSecondaryPolicyExists() {
        customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
        serviceInstance.getModelInfoServiceInstance().setServiceType("st1");
        vnf.setVnfType("vnft1");
        delegateExecution.setVariable("aLaCarte", true);
        delegateExecution.setVariable("suppressRollback", false);
        delegateExecution.setVariable("WorkflowExceptionCode", "7000");
        RainyDayHandlerStatus rainyDayHandlerStatus = new RainyDayHandlerStatus();
        rainyDayHandlerStatus.setErrorCode("7000");
        rainyDayHandlerStatus.setFlowName("AssignServiceInstanceBB");
        rainyDayHandlerStatus.setServiceType("st1");
        rainyDayHandlerStatus.setVnfType("vnft1");
        rainyDayHandlerStatus.setPolicy("Retry");
        rainyDayHandlerStatus.setWorkStep(ASTERISK);
        rainyDayHandlerStatus.setSecondaryPolicy("Abort");

        doReturn(rainyDayHandlerStatus).when(MOCK_catalogDbClient).getRainyDayHandlerStatus("AssignServiceInstanceBB",
                "st1", "vnft1", "7000", "*", "errorMessage", "*");

        executeBuildingBlockRainyDay.queryRainyDayTable(delegateExecution, false);

        assertEquals("Abort", delegateExecution.getVariable("handlingCode"));
    }

    @Test
    public void queryRainyDayTableRollbackToAssignedMacro() {
        customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
        serviceInstance.getModelInfoServiceInstance().setServiceType("st1");
        vnf.setVnfType("vnft1");
        delegateExecution.setVariable("aLaCarte", false);
        delegateExecution.setVariable("suppressRollback", false);
        delegateExecution.setVariable("WorkflowExceptionCode", "7000");
        RainyDayHandlerStatus rainyDayHandlerStatus = new RainyDayHandlerStatus();
        rainyDayHandlerStatus.setErrorCode("7000");
        rainyDayHandlerStatus.setFlowName("AssignServiceInstanceBB");
        rainyDayHandlerStatus.setServiceType("st1");
        rainyDayHandlerStatus.setVnfType("vnft1");
        rainyDayHandlerStatus.setPolicy("RollbackToAssigned");
        rainyDayHandlerStatus.setWorkStep(ASTERISK);
        rainyDayHandlerStatus.setSecondaryPolicy("Abort");

        doReturn(rainyDayHandlerStatus).when(MOCK_catalogDbClient).getRainyDayHandlerStatus("AssignServiceInstanceBB",
                "st1", "vnft1", "7000", "*", "errorMessage", "*");

        executeBuildingBlockRainyDay.queryRainyDayTable(delegateExecution, true);

        assertEquals("Rollback", delegateExecution.getVariable("handlingCode"));
        assertEquals(Status.ROLLED_BACK.toString(), delegateExecution.getVariable("rollbackTargetState"));
    }

    @Test
    public void queryRainyDayTableRollbackToAssignedALaCarte() {
        customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
        serviceInstance.getModelInfoServiceInstance().setServiceType("st1");
        vnf.setVnfType("vnft1");
        delegateExecution.setVariable("aLaCarte", true);
        delegateExecution.setVariable("suppressRollback", false);
        delegateExecution.setVariable("WorkflowExceptionCode", "7000");
        RainyDayHandlerStatus rainyDayHandlerStatus = new RainyDayHandlerStatus();
        rainyDayHandlerStatus.setErrorCode("7000");
        rainyDayHandlerStatus.setFlowName("AssignServiceInstanceBB");
        rainyDayHandlerStatus.setServiceType("st1");
        rainyDayHandlerStatus.setVnfType("vnft1");
        rainyDayHandlerStatus.setPolicy("RollbackToAssigned");
        rainyDayHandlerStatus.setWorkStep(ASTERISK);
        rainyDayHandlerStatus.setSecondaryPolicy("Abort");

        doReturn(rainyDayHandlerStatus).when(MOCK_catalogDbClient).getRainyDayHandlerStatus("AssignServiceInstanceBB",
                "st1", "vnft1", "7000", "*", "errorMessage", "*");

        executeBuildingBlockRainyDay.queryRainyDayTable(delegateExecution, true);

        assertEquals("RollbackToAssigned", delegateExecution.getVariable("handlingCode"));
        assertEquals(Status.ROLLED_BACK_TO_ASSIGNED.toString(), delegateExecution.getVariable("rollbackTargetState"));
    }

    @Test
    public void queryRainyDayTableRollbackToCreated() {
        customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
        serviceInstance.getModelInfoServiceInstance().setServiceType("st1");
        vnf.setVnfType("vnft1");
        delegateExecution.setVariable("aLaCarte", true);
        delegateExecution.setVariable("suppressRollback", false);
        delegateExecution.setVariable("WorkflowExceptionCode", "7000");
        RainyDayHandlerStatus rainyDayHandlerStatus = new RainyDayHandlerStatus();
        rainyDayHandlerStatus.setErrorCode("7000");
        rainyDayHandlerStatus.setFlowName("AssignServiceInstanceBB");
        rainyDayHandlerStatus.setServiceType("st1");
        rainyDayHandlerStatus.setVnfType("vnft1");
        rainyDayHandlerStatus.setPolicy("RollbackToCreated");
        rainyDayHandlerStatus.setWorkStep(ASTERISK);
        rainyDayHandlerStatus.setSecondaryPolicy("Abort");

        doReturn(rainyDayHandlerStatus).when(MOCK_catalogDbClient).getRainyDayHandlerStatus("AssignServiceInstanceBB",
                "st1", "vnft1", "7000", "*", "errorMessage", "*");

        executeBuildingBlockRainyDay.queryRainyDayTable(delegateExecution, true);

        assertEquals("RollbackToCreated", delegateExecution.getVariable("handlingCode"));
        assertEquals(Status.ROLLED_BACK_TO_CREATED.toString(), delegateExecution.getVariable("rollbackTargetState"));
    }

    @Test
    public void queryRainyDayTableRollbackToCreatedNoConfiguration() {
        customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
        serviceInstance.getModelInfoServiceInstance().setServiceType("st1");
        vnf.setVnfType("vnft1");
        BuildingBlock buildingBlock = new BuildingBlock().setBpmnFlowName("AddFabricConfigurationBB");
        ExecuteBuildingBlock executeBuildingBlock = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock);
        delegateExecution.setVariable("buildingBlock", executeBuildingBlock);
        delegateExecution.setVariable("aLaCarte", true);
        delegateExecution.setVariable("suppressRollback", false);
        delegateExecution.setVariable("WorkflowExceptionCode", "7000");
        RainyDayHandlerStatus rainyDayHandlerStatus = new RainyDayHandlerStatus();
        rainyDayHandlerStatus.setErrorCode("7000");
        rainyDayHandlerStatus.setFlowName("AddFabricConfigurationBB");
        rainyDayHandlerStatus.setServiceType("st1");
        rainyDayHandlerStatus.setVnfType("vnft1");
        rainyDayHandlerStatus.setPolicy("RollbackToCreatedNoConfiguration");
        rainyDayHandlerStatus.setWorkStep(ASTERISK);
        rainyDayHandlerStatus.setSecondaryPolicy("Abort");

        doReturn(rainyDayHandlerStatus).when(MOCK_catalogDbClient).getRainyDayHandlerStatus("AddFabricConfigurationBB",
                "st1", "vnft1", "7000", "*", "errorMessage", "*");

        executeBuildingBlockRainyDay.queryRainyDayTable(delegateExecution, true);

        assertEquals("RollbackToCreatedNoConfiguration", delegateExecution.getVariable("handlingCode"));
        assertEquals(Status.ROLLED_BACK_TO_CREATED.toString(), delegateExecution.getVariable("rollbackTargetState"));
    }


    @Test
    public void suppressRollbackTest() {
        delegateExecution.setVariable("suppressRollback", true);
        delegateExecution.setVariable("aLaCarte", true);
        executeBuildingBlockRainyDay.queryRainyDayTable(delegateExecution, true);
        assertEquals("Abort", delegateExecution.getVariable("handlingCode"));
    }

    @Test
    public void queryRainyDayTableServiceRoleNotDefined() {
        customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
        serviceInstance.getModelInfoServiceInstance().setServiceType("st1");
        serviceInstance.getModelInfoServiceInstance().setServiceRole("sr1");
        vnf.setVnfType("vnft1");
        delegateExecution.setVariable("aLaCarte", true);
        delegateExecution.setVariable("suppressRollback", false);
        delegateExecution.setVariable("WorkflowExceptionCode", "7000");
        RainyDayHandlerStatus rainyDayHandlerStatus = new RainyDayHandlerStatus();
        rainyDayHandlerStatus.setErrorCode("7000");
        rainyDayHandlerStatus.setFlowName("AssignServiceInstanceBB");
        rainyDayHandlerStatus.setServiceType("st1");
        rainyDayHandlerStatus.setVnfType("vnft1");
        rainyDayHandlerStatus.setPolicy("Rollback");
        rainyDayHandlerStatus.setWorkStep(ASTERISK);

        doReturn(rainyDayHandlerStatus).when(MOCK_catalogDbClient).getRainyDayHandlerStatus("AssignServiceInstanceBB",
                "st1", "vnft1", "7000", "*", "errorMessage", "sr1");

        executeBuildingBlockRainyDay.queryRainyDayTable(delegateExecution, true);
        assertEquals("Rollback", delegateExecution.getVariable("handlingCode"));
    }

    @Test
    public void queryRainyDayTableServiceRoleNC() {
        customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
        serviceInstance.getModelInfoServiceInstance().setServiceType("st1");
        serviceInstance.getModelInfoServiceInstance().setServiceRole("NETWORK-COLLECTION");
        vnf.setVnfType("vnft1");
        delegateExecution.setVariable("aLaCarte", true);
        delegateExecution.setVariable("suppressRollback", false);
        delegateExecution.setVariable("WorkflowExceptionCode", "7000");
        RainyDayHandlerStatus rainyDayHandlerStatus = new RainyDayHandlerStatus();
        rainyDayHandlerStatus.setErrorCode("7000");
        rainyDayHandlerStatus.setFlowName("ActivateServiceInstanceBB");
        rainyDayHandlerStatus.setServiceType("st1");
        rainyDayHandlerStatus.setVnfType("vnft1");
        rainyDayHandlerStatus.setPolicy("Abort");
        rainyDayHandlerStatus.setWorkStep(ASTERISK);

        doReturn(rainyDayHandlerStatus).when(MOCK_catalogDbClient).getRainyDayHandlerStatus("AssignServiceInstanceBB",
                "st1", "vnft1", "7000", "*", "errorMessage", "NETWORK-COLLECTION");

        executeBuildingBlockRainyDay.queryRainyDayTable(delegateExecution, true);
        assertEquals("Abort", delegateExecution.getVariable("handlingCode"));
    }

}
