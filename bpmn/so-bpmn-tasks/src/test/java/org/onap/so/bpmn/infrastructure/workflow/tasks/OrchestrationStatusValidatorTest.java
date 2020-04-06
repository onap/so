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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.so.db.catalog.beans.BuildingBlockDetail;
import org.onap.so.db.catalog.beans.OrchestrationAction;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.db.catalog.beans.OrchestrationStatusStateTransitionDirective;
import org.onap.so.db.catalog.beans.OrchestrationStatusValidationDirective;
import org.onap.so.db.catalog.beans.ResourceType;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.springframework.beans.factory.annotation.Autowired;

public class OrchestrationStatusValidatorTest extends BaseTaskTest {

    private static final String vfModuleExistExpectedMessage =
            "The VfModule was found to already exist, thus no new VfModule was created in the cloud via this request";

    private static final String vfModuleNotExistExpectedMessage =
            "The VfModule was not found, thus no VfModule was deleted in the cloud via this request";

    @InjectMocks
    protected OrchestrationStatusValidator orchestrationStatusValidator = new OrchestrationStatusValidator();

    @Test
    public void test_validateOrchestrationStatus() throws Exception {
        String flowToBeCalled = "AssignServiceInstanceBB";
        setServiceInstance().setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("flowToBeCalled", flowToBeCalled);

        BuildingBlockDetail buildingBlockDetail = new BuildingBlockDetail();
        buildingBlockDetail.setBuildingBlockName("AssignServiceInstanceBB");
        buildingBlockDetail.setId(1);
        buildingBlockDetail.setResourceType(ResourceType.SERVICE);
        buildingBlockDetail.setTargetAction(OrchestrationAction.ASSIGN);

        doReturn(buildingBlockDetail).when(catalogDbClient).getBuildingBlockDetail(flowToBeCalled);

        org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstance =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance();
        serviceInstance.setServiceInstanceId("serviceInstanceId");
        serviceInstance.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID)))
                .thenReturn(serviceInstance);

        OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective =
                new OrchestrationStatusStateTransitionDirective();
        orchestrationStatusStateTransitionDirective.setFlowDirective(OrchestrationStatusValidationDirective.CONTINUE);
        orchestrationStatusStateTransitionDirective.setId(1);
        orchestrationStatusStateTransitionDirective.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        orchestrationStatusStateTransitionDirective.setResourceType(ResourceType.SERVICE);
        orchestrationStatusStateTransitionDirective.setTargetAction(OrchestrationAction.ASSIGN);

        doReturn(orchestrationStatusStateTransitionDirective).when(catalogDbClient)
                .getOrchestrationStatusStateTransitionDirective(ResourceType.SERVICE, OrchestrationStatus.PRECREATED,
                        OrchestrationAction.ASSIGN);

        orchestrationStatusValidator.validateOrchestrationStatus(execution);

        assertEquals(OrchestrationStatusValidationDirective.CONTINUE,
                execution.getVariable("orchestrationStatusValidationResult"));

        Mockito.verifyZeroInteractions(requestsDbClient);
    }

    @Test
    public void test_validateOrchestrationStatusConfiguration() throws Exception {
        lookupKeyMap.put(ResourceKey.CONFIGURATION_ID, "configurationId");
        String flowToBeCalled = "UnassignFabricConfigurationBB";
        ServiceInstance si = setServiceInstance();
        List<Configuration> configurations = new ArrayList<>();
        Configuration config = new Configuration();

        si.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        config.setConfigurationId("configurationId");
        config.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        configurations.add(config);
        si.setConfigurations(configurations);

        execution.setVariable("flowToBeCalled", flowToBeCalled);
        execution.setVariable("aLaCarte", true);

        BuildingBlockDetail buildingBlockDetail = new BuildingBlockDetail();
        buildingBlockDetail.setBuildingBlockName("UnassignFabricConfigurationBB");
        buildingBlockDetail.setId(1);
        buildingBlockDetail.setResourceType(ResourceType.CONFIGURATION);
        buildingBlockDetail.setTargetAction(OrchestrationAction.UNASSIGN);

        doReturn(buildingBlockDetail).when(catalogDbClient).getBuildingBlockDetail(flowToBeCalled);

        org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration configuration =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration();
        configuration.setConfigurationId("configurationId");
        configuration.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.CONFIGURATION_ID)))
                .thenReturn(configuration);

        OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective =
                new OrchestrationStatusStateTransitionDirective();
        orchestrationStatusStateTransitionDirective
                .setFlowDirective(OrchestrationStatusValidationDirective.SILENT_SUCCESS);
        orchestrationStatusStateTransitionDirective.setId(1);
        orchestrationStatusStateTransitionDirective.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        orchestrationStatusStateTransitionDirective.setResourceType(ResourceType.CONFIGURATION);
        orchestrationStatusStateTransitionDirective.setTargetAction(OrchestrationAction.UNASSIGN);

        doReturn(orchestrationStatusStateTransitionDirective).when(catalogDbClient)
                .getOrchestrationStatusStateTransitionDirective(ResourceType.CONFIGURATION,
                        OrchestrationStatus.PRECREATED, OrchestrationAction.UNASSIGN);

        orchestrationStatusValidator.validateOrchestrationStatus(execution);

        assertEquals(OrchestrationStatusValidationDirective.SILENT_SUCCESS,
                execution.getVariable("orchestrationStatusValidationResult"));

        Mockito.verifyZeroInteractions(requestsDbClient);
    }

    @Ignore
    @Test
    public void test_validateOrchestrationStatus_buildingBlockDetailNotFound() throws Exception {
        expectedException.expect(BpmnError.class);

        String flowToBeCalled = "AssignServiceInstanceBB";

        execution.setVariable("flowToBeCalled", flowToBeCalled);

        doReturn(null).when(catalogDbClient).getBuildingBlockDetail(flowToBeCalled);

        orchestrationStatusValidator.validateOrchestrationStatus(execution);
    }

    @Ignore
    @Test
    public void test_validateOrchestrationStatus_orchestrationValidationFail() throws Exception {
        expectedException.expect(BpmnError.class);

        String flowToBeCalled = "AssignServiceInstanceBB";

        execution.setVariable("flowToBeCalled", flowToBeCalled);

        BuildingBlockDetail buildingBlockDetail = new BuildingBlockDetail();
        buildingBlockDetail.setBuildingBlockName("AssignServiceInstanceBB");
        buildingBlockDetail.setId(1);
        buildingBlockDetail.setResourceType(ResourceType.SERVICE);
        buildingBlockDetail.setTargetAction(OrchestrationAction.ASSIGN);

        doReturn(buildingBlockDetail).when(catalogDbClient).getBuildingBlockDetail(flowToBeCalled);

        OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective =
                new OrchestrationStatusStateTransitionDirective();
        orchestrationStatusStateTransitionDirective.setFlowDirective(OrchestrationStatusValidationDirective.FAIL);
        orchestrationStatusStateTransitionDirective.setId(1);
        orchestrationStatusStateTransitionDirective.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        orchestrationStatusStateTransitionDirective.setResourceType(ResourceType.SERVICE);
        orchestrationStatusStateTransitionDirective.setTargetAction(OrchestrationAction.ASSIGN);

        doReturn(orchestrationStatusStateTransitionDirective).when(catalogDbClient)
                .getOrchestrationStatusStateTransitionDirective(ResourceType.SERVICE, OrchestrationStatus.PRECREATED,
                        OrchestrationAction.ASSIGN);

        orchestrationStatusValidator.validateOrchestrationStatus(execution);

        Mockito.verifyZeroInteractions(requestsDbClient);
    }

    @Ignore
    @Test
    public void test_validateOrchestrationStatus_orchestrationValidationNotFound() throws Exception {
        expectedException.expect(BpmnError.class);

        String flowToBeCalled = "AssignServiceInstanceBB";

        execution.setVariable("flowToBeCalled", flowToBeCalled);

        BuildingBlockDetail buildingBlockDetail = new BuildingBlockDetail();
        buildingBlockDetail.setBuildingBlockName("AssignServiceInstanceBB");
        buildingBlockDetail.setId(1);
        buildingBlockDetail.setResourceType(ResourceType.SERVICE);
        buildingBlockDetail.setTargetAction(OrchestrationAction.ASSIGN);

        doReturn(buildingBlockDetail).when(catalogDbClient).getBuildingBlockDetail(flowToBeCalled);

        OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective =
                new OrchestrationStatusStateTransitionDirective();
        orchestrationStatusStateTransitionDirective.setFlowDirective(OrchestrationStatusValidationDirective.FAIL);
        orchestrationStatusStateTransitionDirective.setId(1);
        orchestrationStatusStateTransitionDirective.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        orchestrationStatusStateTransitionDirective.setResourceType(ResourceType.SERVICE);
        orchestrationStatusStateTransitionDirective.setTargetAction(OrchestrationAction.ASSIGN);

        doReturn(orchestrationStatusStateTransitionDirective).when(catalogDbClient)
                .getOrchestrationStatusStateTransitionDirective(ResourceType.NETWORK, OrchestrationStatus.PRECREATED,
                        OrchestrationAction.ASSIGN);

        orchestrationStatusValidator.validateOrchestrationStatus(execution);

        Mockito.verifyZeroInteractions(requestsDbClient);
    }

    @Test
    public void test_validateOrchestrationStatus_unassignNotFound() throws Exception {
        String flowToBeCalled = "UnassignServiceInstanceBB";

        execution.setVariable("flowToBeCalled", flowToBeCalled);
        execution.setVariable("aLaCarte", true);

        BuildingBlockDetail buildingBlockDetail = new BuildingBlockDetail();
        buildingBlockDetail.setBuildingBlockName("UnassignServiceInstanceBB");
        buildingBlockDetail.setId(1);
        buildingBlockDetail.setResourceType(ResourceType.SERVICE);
        buildingBlockDetail.setTargetAction(OrchestrationAction.UNASSIGN);

        doReturn(buildingBlockDetail).when(catalogDbClient).getBuildingBlockDetail(flowToBeCalled);

        lookupKeyMap = new HashMap<ResourceKey, String>();

        orchestrationStatusValidator.validateOrchestrationStatus(execution);

        assertNull(execution.getVariable("orchestrationStatusValidationResult"));
    }

    @Test
    public void test_validateOrchestrationStatusSecondStageOfMultiStageEnabledVfModule() throws Exception {
        String flowToBeCalled = "CreateVfModuleBB";

        execution.setVariable("orchestrationStatusValidationResult",
                OrchestrationStatusValidationDirective.SILENT_SUCCESS);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("flowToBeCalled", flowToBeCalled);

        GenericVnf genericVnf = buildGenericVnf();
        ModelInfoGenericVnf modelInfoGenericVnf = genericVnf.getModelInfoGenericVnf();
        modelInfoGenericVnf.setMultiStageDesign("true");
        setGenericVnf().setModelInfoGenericVnf(modelInfoGenericVnf);
        setVfModule().setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);

        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
        vfModule.setVfModuleId("vfModuleId");
        vfModule.setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID))).thenReturn(vfModule);

        BuildingBlockDetail buildingBlockDetail = new BuildingBlockDetail();
        buildingBlockDetail.setBuildingBlockName("CreateVfModuleBB");
        buildingBlockDetail.setId(1);
        buildingBlockDetail.setResourceType(ResourceType.VF_MODULE);
        buildingBlockDetail.setTargetAction(OrchestrationAction.CREATE);

        doReturn(buildingBlockDetail).when(catalogDbClient).getBuildingBlockDetail(flowToBeCalled);

        OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective =
                new OrchestrationStatusStateTransitionDirective();
        orchestrationStatusStateTransitionDirective.setFlowDirective(OrchestrationStatusValidationDirective.CONTINUE);
        orchestrationStatusStateTransitionDirective.setId(1);
        orchestrationStatusStateTransitionDirective.setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);
        orchestrationStatusStateTransitionDirective.setResourceType(ResourceType.VF_MODULE);
        orchestrationStatusStateTransitionDirective.setTargetAction(OrchestrationAction.CREATE);

        doReturn(orchestrationStatusStateTransitionDirective).when(catalogDbClient)
                .getOrchestrationStatusStateTransitionDirective(ResourceType.VF_MODULE,
                        OrchestrationStatus.PENDING_ACTIVATION, OrchestrationAction.CREATE);

        orchestrationStatusValidator.validateOrchestrationStatus(execution);

        assertEquals(OrchestrationStatusValidationDirective.CONTINUE,
                execution.getVariable("orchestrationStatusValidationResult"));

        Mockito.verifyZeroInteractions(requestsDbClient);
    }


    @Test
    public void test_validateOrchestrationStatusSecondStageOfMultiStageWrongPrevStatusVfModule() throws Exception {
        String flowToBeCalled = "CreateVfModuleBB";

        execution.setVariable("orchestrationStatusValidationResult", OrchestrationStatusValidationDirective.CONTINUE);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("flowToBeCalled", flowToBeCalled);

        GenericVnf genericVnf = buildGenericVnf();
        ModelInfoGenericVnf modelInfoGenericVnf = genericVnf.getModelInfoGenericVnf();
        modelInfoGenericVnf.setMultiStageDesign("true");
        setGenericVnf().setModelInfoGenericVnf(modelInfoGenericVnf);
        setVfModule().setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);

        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
        vfModule.setVfModuleId("vfModuleId");
        vfModule.setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID))).thenReturn(vfModule);

        BuildingBlockDetail buildingBlockDetail = new BuildingBlockDetail();
        buildingBlockDetail.setBuildingBlockName("CreateVfModuleBB");
        buildingBlockDetail.setId(1);
        buildingBlockDetail.setResourceType(ResourceType.VF_MODULE);
        buildingBlockDetail.setTargetAction(OrchestrationAction.CREATE);

        doReturn(buildingBlockDetail).when(catalogDbClient).getBuildingBlockDetail(flowToBeCalled);

        OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective =
                new OrchestrationStatusStateTransitionDirective();
        orchestrationStatusStateTransitionDirective
                .setFlowDirective(OrchestrationStatusValidationDirective.SILENT_SUCCESS);
        orchestrationStatusStateTransitionDirective.setId(1);
        orchestrationStatusStateTransitionDirective.setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);
        orchestrationStatusStateTransitionDirective.setResourceType(ResourceType.VF_MODULE);
        orchestrationStatusStateTransitionDirective.setTargetAction(OrchestrationAction.CREATE);

        doReturn(orchestrationStatusStateTransitionDirective).when(catalogDbClient)
                .getOrchestrationStatusStateTransitionDirective(ResourceType.VF_MODULE,
                        OrchestrationStatus.PENDING_ACTIVATION, OrchestrationAction.CREATE);

        InfraActiveRequests request = new InfraActiveRequests();
        request.setRequestId("testVfModuleId1");
        request.setResourceStatusMessage(vfModuleExistExpectedMessage);

        Mockito.doNothing().when(requestsDbClient).patchInfraActiveRequests(request);

        orchestrationStatusValidator.validateOrchestrationStatus(execution);

        assertEquals(OrchestrationStatusValidationDirective.SILENT_SUCCESS,
                execution.getVariable("orchestrationStatusValidationResult"));

        ArgumentCaptor<InfraActiveRequests> argument = ArgumentCaptor.forClass(InfraActiveRequests.class);
        Mockito.verify(requestsDbClient).patchInfraActiveRequests(argument.capture());

        assertEquals(vfModuleExistExpectedMessage, argument.getValue().getResourceStatusMessage());
    }

    @Test
    public void test_validateOrchestrationStatusSecondStageOfMultiStageDisabledVfModule() throws Exception {
        String flowToBeCalled = "CreateVfModuleBB";

        execution.setVariable("orchestrationStatusValidationResult",
                OrchestrationStatusValidationDirective.SILENT_SUCCESS);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("flowToBeCalled", flowToBeCalled);

        GenericVnf genericVnf = buildGenericVnf();
        ModelInfoGenericVnf modelInfoGenericVnf = genericVnf.getModelInfoGenericVnf();
        modelInfoGenericVnf.setMultiStageDesign("false");
        setGenericVnf().setModelInfoGenericVnf(modelInfoGenericVnf);
        setVfModule().setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);

        BuildingBlockDetail buildingBlockDetail = new BuildingBlockDetail();
        buildingBlockDetail.setBuildingBlockName("CreateVfModuleBB");
        buildingBlockDetail.setId(1);
        buildingBlockDetail.setResourceType(ResourceType.VF_MODULE);
        buildingBlockDetail.setTargetAction(OrchestrationAction.CREATE);

        doReturn(buildingBlockDetail).when(catalogDbClient).getBuildingBlockDetail(flowToBeCalled);

        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
        vfModule.setVfModuleId("vfModuleId");
        vfModule.setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID))).thenReturn(vfModule);

        OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective =
                new OrchestrationStatusStateTransitionDirective();
        orchestrationStatusStateTransitionDirective
                .setFlowDirective(OrchestrationStatusValidationDirective.SILENT_SUCCESS);
        orchestrationStatusStateTransitionDirective.setId(1);
        orchestrationStatusStateTransitionDirective.setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);
        orchestrationStatusStateTransitionDirective.setResourceType(ResourceType.VF_MODULE);
        orchestrationStatusStateTransitionDirective.setTargetAction(OrchestrationAction.CREATE);

        doReturn(orchestrationStatusStateTransitionDirective).when(catalogDbClient)
                .getOrchestrationStatusStateTransitionDirective(ResourceType.VF_MODULE,
                        OrchestrationStatus.PENDING_ACTIVATION, OrchestrationAction.CREATE);

        InfraActiveRequests request = new InfraActiveRequests();
        request.setRequestId("testVfModuleId1");
        request.setResourceStatusMessage(vfModuleExistExpectedMessage);

        Mockito.doNothing().when(requestsDbClient).patchInfraActiveRequests(request);

        orchestrationStatusValidator.validateOrchestrationStatus(execution);

        assertEquals(OrchestrationStatusValidationDirective.SILENT_SUCCESS,
                execution.getVariable("orchestrationStatusValidationResult"));

        ArgumentCaptor<InfraActiveRequests> argument = ArgumentCaptor.forClass(InfraActiveRequests.class);
        Mockito.verify(requestsDbClient).patchInfraActiveRequests(argument.capture());

        assertEquals(vfModuleExistExpectedMessage, argument.getValue().getResourceStatusMessage());
    }

    @Test
    public void test_validateOrchestrationStatusSecondStageOfMultiStageWrongOrchStatusVfModule() throws Exception {
        String flowToBeCalled = "CreateVfModuleBB";

        execution.setVariable("orchestrationStatusValidationResult",
                OrchestrationStatusValidationDirective.SILENT_SUCCESS);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("flowToBeCalled", flowToBeCalled);

        GenericVnf genericVnf = buildGenericVnf();
        ModelInfoGenericVnf modelInfoGenericVnf = genericVnf.getModelInfoGenericVnf();
        modelInfoGenericVnf.setMultiStageDesign("true");
        setGenericVnf().setModelInfoGenericVnf(modelInfoGenericVnf);
        setVfModule().setOrchestrationStatus(OrchestrationStatus.ASSIGNED);

        BuildingBlockDetail buildingBlockDetail = new BuildingBlockDetail();
        buildingBlockDetail.setBuildingBlockName("CreateVfModuleBB");
        buildingBlockDetail.setId(1);
        buildingBlockDetail.setResourceType(ResourceType.VF_MODULE);
        buildingBlockDetail.setTargetAction(OrchestrationAction.CREATE);

        doReturn(buildingBlockDetail).when(catalogDbClient).getBuildingBlockDetail(flowToBeCalled);

        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
        vfModule.setVfModuleId("vfModuleId");
        vfModule.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID))).thenReturn(vfModule);

        OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective =
                new OrchestrationStatusStateTransitionDirective();
        orchestrationStatusStateTransitionDirective
                .setFlowDirective(OrchestrationStatusValidationDirective.SILENT_SUCCESS);
        orchestrationStatusStateTransitionDirective.setId(1);
        orchestrationStatusStateTransitionDirective.setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);
        orchestrationStatusStateTransitionDirective.setResourceType(ResourceType.VF_MODULE);
        orchestrationStatusStateTransitionDirective.setTargetAction(OrchestrationAction.CREATE);

        doReturn(orchestrationStatusStateTransitionDirective).when(catalogDbClient)
                .getOrchestrationStatusStateTransitionDirective(ResourceType.VF_MODULE, OrchestrationStatus.ASSIGNED,
                        OrchestrationAction.CREATE);

        InfraActiveRequests request = new InfraActiveRequests();
        request.setRequestId("testVfModuleId1");
        request.setResourceStatusMessage(vfModuleExistExpectedMessage);

        Mockito.doNothing().when(requestsDbClient).patchInfraActiveRequests(request);

        orchestrationStatusValidator.validateOrchestrationStatus(execution);

        assertEquals(OrchestrationStatusValidationDirective.SILENT_SUCCESS,
                execution.getVariable("orchestrationStatusValidationResult"));

        ArgumentCaptor<InfraActiveRequests> argument = ArgumentCaptor.forClass(InfraActiveRequests.class);
        Mockito.verify(requestsDbClient).patchInfraActiveRequests(argument.capture());

        assertEquals(vfModuleExistExpectedMessage, argument.getValue().getResourceStatusMessage());
    }

    @Test
    public void test_validateOrchestrationStatusSecondStageOfMultiStageWrongTargetActionVfModule() throws Exception {
        String flowToBeCalled = "CreateVfModuleBB";

        execution.setVariable("orchestrationStatusValidationResult",
                OrchestrationStatusValidationDirective.SILENT_SUCCESS);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("flowToBeCalled", flowToBeCalled);

        GenericVnf genericVnf = buildGenericVnf();
        ModelInfoGenericVnf modelInfoGenericVnf = genericVnf.getModelInfoGenericVnf();
        modelInfoGenericVnf.setMultiStageDesign("true");
        setGenericVnf().setModelInfoGenericVnf(modelInfoGenericVnf);
        setVfModule().setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);

        BuildingBlockDetail buildingBlockDetail = new BuildingBlockDetail();
        buildingBlockDetail.setBuildingBlockName("CreateVfModuleBB");
        buildingBlockDetail.setId(1);
        buildingBlockDetail.setResourceType(ResourceType.VF_MODULE);
        buildingBlockDetail.setTargetAction(OrchestrationAction.ACTIVATE);

        doReturn(buildingBlockDetail).when(catalogDbClient).getBuildingBlockDetail(flowToBeCalled);

        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
        vfModule.setVfModuleId("vfModuleId");
        vfModule.setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID))).thenReturn(vfModule);

        OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective =
                new OrchestrationStatusStateTransitionDirective();
        orchestrationStatusStateTransitionDirective
                .setFlowDirective(OrchestrationStatusValidationDirective.SILENT_SUCCESS);
        orchestrationStatusStateTransitionDirective.setId(1);
        orchestrationStatusStateTransitionDirective.setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);
        orchestrationStatusStateTransitionDirective.setResourceType(ResourceType.VF_MODULE);
        orchestrationStatusStateTransitionDirective.setTargetAction(OrchestrationAction.ACTIVATE);

        doReturn(orchestrationStatusStateTransitionDirective).when(catalogDbClient)
                .getOrchestrationStatusStateTransitionDirective(ResourceType.VF_MODULE,
                        OrchestrationStatus.PENDING_ACTIVATION, OrchestrationAction.ACTIVATE);

        InfraActiveRequests request = new InfraActiveRequests();
        request.setRequestId("testVfModuleId1");
        request.setResourceStatusMessage(vfModuleExistExpectedMessage);

        Mockito.doNothing().when(requestsDbClient).patchInfraActiveRequests(request);

        orchestrationStatusValidator.validateOrchestrationStatus(execution);

        assertEquals(OrchestrationStatusValidationDirective.SILENT_SUCCESS,
                execution.getVariable("orchestrationStatusValidationResult"));

        ArgumentCaptor<InfraActiveRequests> argument = ArgumentCaptor.forClass(InfraActiveRequests.class);
        Mockito.verify(requestsDbClient).patchInfraActiveRequests(argument.capture());

        assertEquals(vfModuleExistExpectedMessage, argument.getValue().getResourceStatusMessage());
    }

    @Test
    public void test_validateOrchestrationStatusSecondStageOfMultiStageWrongAlacarteValueVfModule() throws Exception {
        String flowToBeCalled = "CreateVfModuleBB";

        execution.setVariable("orchestrationStatusValidationResult",
                OrchestrationStatusValidationDirective.SILENT_SUCCESS);
        execution.setVariable("aLaCarte", false);
        execution.setVariable("flowToBeCalled", flowToBeCalled);

        GenericVnf genericVnf = buildGenericVnf();
        ModelInfoGenericVnf modelInfoGenericVnf = genericVnf.getModelInfoGenericVnf();
        modelInfoGenericVnf.setMultiStageDesign("true");
        setGenericVnf().setModelInfoGenericVnf(modelInfoGenericVnf);
        setVfModule().setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);

        BuildingBlockDetail buildingBlockDetail = new BuildingBlockDetail();
        buildingBlockDetail.setBuildingBlockName("CreateVfModuleBB");
        buildingBlockDetail.setId(1);
        buildingBlockDetail.setResourceType(ResourceType.VF_MODULE);
        buildingBlockDetail.setTargetAction(OrchestrationAction.CREATE);

        doReturn(buildingBlockDetail).when(catalogDbClient).getBuildingBlockDetail(flowToBeCalled);

        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
        vfModule.setVfModuleId("vfModuleId");
        vfModule.setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID))).thenReturn(vfModule);

        OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective =
                new OrchestrationStatusStateTransitionDirective();
        orchestrationStatusStateTransitionDirective
                .setFlowDirective(OrchestrationStatusValidationDirective.SILENT_SUCCESS);
        orchestrationStatusStateTransitionDirective.setId(1);
        orchestrationStatusStateTransitionDirective.setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);
        orchestrationStatusStateTransitionDirective.setResourceType(ResourceType.VF_MODULE);
        orchestrationStatusStateTransitionDirective.setTargetAction(OrchestrationAction.ACTIVATE);

        doReturn(orchestrationStatusStateTransitionDirective).when(catalogDbClient)
                .getOrchestrationStatusStateTransitionDirective(ResourceType.VF_MODULE,
                        OrchestrationStatus.PENDING_ACTIVATION, OrchestrationAction.CREATE);

        InfraActiveRequests request = new InfraActiveRequests();
        request.setRequestId("testVfModuleId1");
        request.setResourceStatusMessage(vfModuleExistExpectedMessage);

        Mockito.doNothing().when(requestsDbClient).patchInfraActiveRequests(request);

        orchestrationStatusValidator.validateOrchestrationStatus(execution);

        assertEquals(OrchestrationStatusValidationDirective.SILENT_SUCCESS,
                execution.getVariable("orchestrationStatusValidationResult"));

        ArgumentCaptor<InfraActiveRequests> argument = ArgumentCaptor.forClass(InfraActiveRequests.class);
        Mockito.verify(requestsDbClient).patchInfraActiveRequests(argument.capture());

        assertEquals(vfModuleExistExpectedMessage, argument.getValue().getResourceStatusMessage());
    }

    @Test
    public void continueValidationActivatedTest() throws Exception {
        String flowToBeCalled = "DeactivateVnfBB";
        BuildingBlockDetail buildingBlockDetail = new BuildingBlockDetail();
        buildingBlockDetail.setBuildingBlockName(flowToBeCalled);
        buildingBlockDetail.setId(1);
        buildingBlockDetail.setResourceType(ResourceType.VF_MODULE);
        buildingBlockDetail.setTargetAction(OrchestrationAction.DEACTIVATE);
        when(catalogDbClient.getBuildingBlockDetail(flowToBeCalled)).thenReturn(buildingBlockDetail);

        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
        vfModule.setVfModuleId("vfModuleId");
        vfModule.setOrchestrationStatus(OrchestrationStatus.ACTIVATED);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID))).thenReturn(vfModule);

        OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective =
                new OrchestrationStatusStateTransitionDirective();
        orchestrationStatusStateTransitionDirective.setFlowDirective(OrchestrationStatusValidationDirective.CONTINUE);
        orchestrationStatusStateTransitionDirective.setId(1);
        orchestrationStatusStateTransitionDirective.setOrchestrationStatus(OrchestrationStatus.ACTIVATED);
        orchestrationStatusStateTransitionDirective.setResourceType(ResourceType.VF_MODULE);
        orchestrationStatusStateTransitionDirective.setTargetAction(OrchestrationAction.DEACTIVATE);
        doReturn(orchestrationStatusStateTransitionDirective).when(catalogDbClient)
                .getOrchestrationStatusStateTransitionDirective(ResourceType.VF_MODULE, OrchestrationStatus.ACTIVATED,
                        OrchestrationAction.DEACTIVATE);

        execution.setVariable("aLaCarte", true);
        execution.setVariable("flowToBeCalled", flowToBeCalled);
        orchestrationStatusValidator.validateOrchestrationStatus(execution);

        assertEquals(OrchestrationStatusValidationDirective.CONTINUE,
                execution.getVariable("orchestrationStatusValidationResult"));

        Mockito.verifyZeroInteractions(requestsDbClient);
    }

    @Test
    public void test_validateOrchestrationStatusDeleteVfModuleSilentSuccess() throws Exception {
        String flowToBeCalled = "DeleteVfModuleBB";

        execution.setVariable("orchestrationStatusValidationResult",
                OrchestrationStatusValidationDirective.SILENT_SUCCESS);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("flowToBeCalled", flowToBeCalled);

        GenericVnf genericVnf = buildGenericVnf();
        ModelInfoGenericVnf modelInfoGenericVnf = genericVnf.getModelInfoGenericVnf();
        modelInfoGenericVnf.setMultiStageDesign("true");
        setGenericVnf().setModelInfoGenericVnf(modelInfoGenericVnf);
        setVfModule().setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);

        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
        vfModule.setVfModuleId("vfModuleId");
        vfModule.setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID))).thenReturn(vfModule);

        BuildingBlockDetail buildingBlockDetail = new BuildingBlockDetail();
        buildingBlockDetail.setBuildingBlockName("DeleteVfModuleBB");
        buildingBlockDetail.setId(1);
        buildingBlockDetail.setResourceType(ResourceType.VF_MODULE);
        buildingBlockDetail.setTargetAction(OrchestrationAction.CREATE);

        doReturn(buildingBlockDetail).when(catalogDbClient).getBuildingBlockDetail(flowToBeCalled);

        OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective =
                new OrchestrationStatusStateTransitionDirective();
        orchestrationStatusStateTransitionDirective
                .setFlowDirective(OrchestrationStatusValidationDirective.SILENT_SUCCESS);
        orchestrationStatusStateTransitionDirective.setId(1);
        orchestrationStatusStateTransitionDirective.setOrchestrationStatus(OrchestrationStatus.PENDING_ACTIVATION);
        orchestrationStatusStateTransitionDirective.setResourceType(ResourceType.VF_MODULE);
        orchestrationStatusStateTransitionDirective.setTargetAction(OrchestrationAction.CREATE);

        doReturn(orchestrationStatusStateTransitionDirective).when(catalogDbClient)
                .getOrchestrationStatusStateTransitionDirective(ResourceType.VF_MODULE,
                        OrchestrationStatus.PENDING_ACTIVATION, OrchestrationAction.CREATE);

        InfraActiveRequests request = new InfraActiveRequests();
        request.setRequestId("testVfModuleId1");
        request.setResourceStatusMessage(vfModuleNotExistExpectedMessage);

        Mockito.doNothing().when(requestsDbClient).patchInfraActiveRequests(request);

        orchestrationStatusValidator.validateOrchestrationStatus(execution);

        assertEquals(OrchestrationStatusValidationDirective.SILENT_SUCCESS,
                execution.getVariable("orchestrationStatusValidationResult"));

        ArgumentCaptor<InfraActiveRequests> argument = ArgumentCaptor.forClass(InfraActiveRequests.class);
        Mockito.verify(requestsDbClient).patchInfraActiveRequests(argument.capture());

        assertEquals(vfModuleNotExistExpectedMessage, argument.getValue().getResourceStatusMessage());
    }
}
