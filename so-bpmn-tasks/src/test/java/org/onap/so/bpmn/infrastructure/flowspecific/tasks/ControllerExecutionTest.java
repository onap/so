/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019  Tech Mahindra
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

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import java.util.UUID;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.db.catalog.beans.BBNameSelectionReference;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;


public class ControllerExecutionTest extends BaseTaskTest {

    @InjectMocks
    private ControllerExecution controllerExecution = new ControllerExecution();


    private BuildingBlock buildingBlock = new BuildingBlock();
    VnfResourceCustomization vnfResourceCustomization = new VnfResourceCustomization();
    private ExecuteBuildingBlock executeBuildingBlock = new ExecuteBuildingBlock();
    private GenericVnf genericVnf;
    private ServiceInstance serviceInstance;
    private RequestContext requestContext;
    private String msoRequestId;

    @Before
    public void before() throws BBObjectNotFoundException {


        genericVnf = setGenericVnf();
        serviceInstance = setServiceInstance();
        msoRequestId = UUID.randomUUID().toString();
        requestContext = setRequestContext();
        requestContext.setMsoRequestId(msoRequestId);
        gBBInput.setRequestContext(requestContext);
        buildingBlock.setBpmnAction("ScaleOut");
        buildingBlock.setBpmnScope("VF");
        executeBuildingBlock.setBuildingBlock(buildingBlock);
        execution.setVariable("buildingBlock", executeBuildingBlock);



        doThrow(new BpmnError("BPMN Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));

        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID)))
                .thenReturn(genericVnf);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID)))
                .thenReturn(serviceInstance);


    }

    @Test
    public void testSetControllerActorScopeAction() throws Exception {


        doReturn(vnfResourceCustomization).when(catalogDbClient).getVnfResourceCustomizationByModelCustomizationUUID(
                genericVnf.getModelInfoGenericVnf().getModelCustomizationUuid());
        controllerExecution.setControllerActorScopeAction(execution);

        assertTrue(true);
    }


    @Test
    public void testSelectBB() throws Exception {
        BBNameSelectionReference bbNameSelectionReference = new BBNameSelectionReference();
        bbNameSelectionReference.setBB_NAME("ConfigurationScaleOut");
        bbNameSelectionReference.setAction("ScaleOut");
        bbNameSelectionReference.setControllerActor("APPC");
        bbNameSelectionReference.setScope("VF");
        doReturn(bbNameSelectionReference).when(catalogDbClient).getBBNameSelectionReference("APPC", "Vf", "ScaleOut");
        execution.setVariable("controller_actor", "APPC");
        execution.setVariable("scope", "Vf");
        execution.setVariable("action", "ScaleOut");
        controllerExecution.selectBB(execution);
        assertEquals("ConfigurationScaleOut", execution.getVariable("bbName"));
    }

}
