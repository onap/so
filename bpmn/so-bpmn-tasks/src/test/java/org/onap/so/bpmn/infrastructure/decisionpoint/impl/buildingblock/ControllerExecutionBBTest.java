/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock.MockControllerBB.TEST_ACTION;
import static org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock.MockControllerBB.TEST_ACTOR;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.onap.so.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.client.cds.PayloadConstants;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ControllerExecutionBB.class, MockControllerBB.class, ExceptionBuilder.class})
public class ControllerExecutionBBTest {

    private static final String ACTOR_PARAM = "actor";
    private static final String ACTION_PARAM = "action";
    private static final String RESOURCE_TYPE_PARAM = "resource_type";

    private static final String TEST_RESOURCE_CUSTOMIZATION_UUID = "68dc9a92-214c-11e7-93ae-92361f002680";
    private static final String TEST_CATALOGDB_CONTROLLER_ACTOR = "cds";
    private static final String TEST_RESOURCE_TYPE = "Firewall";
    private static final String TEST_CONTROLLER_REFERENCE_ACTOR = "sdnc";
    private static final String TEST_SCOPE = "pnf";

    @Autowired
    private ControllerExecutionBB controllerExecutionBB;

    @MockBean
    private BuildingBlockExecution execution;

    @MockBean
    private CatalogDbClient client;

    @MockBean
    private PnfResourceCustomization pnfResourceCustomization;

    @MockBean
    private VnfResourceCustomization vnfResourceCustomization;

    @MockBean
    private ControllerSelectionReference controllerSelectionReference;

    @MockBean
    ExceptionBuilder exceptionBuilder;

    @Before
    public void setUp() {
        when(execution.getVariable(ACTOR_PARAM)).thenReturn(TEST_ACTOR);
        when(execution.getVariable(ACTION_PARAM)).thenReturn(MockControllerBB.TEST_ACTION);

        when(pnfResourceCustomization.getControllerActor()).thenReturn(TEST_CATALOGDB_CONTROLLER_ACTOR);
        when(client.getPnfResourceCustomizationByModelCustomizationUUID(TEST_RESOURCE_CUSTOMIZATION_UUID))
                .thenReturn(pnfResourceCustomization);

        when(execution.getVariable(RESOURCE_TYPE_PARAM)).thenReturn(TEST_RESOURCE_TYPE);
        when(controllerSelectionReference.getControllerName()).thenReturn(TEST_CONTROLLER_REFERENCE_ACTOR);
        when(client.getControllerSelectionReferenceByVnfTypeAndActionCategory(TEST_RESOURCE_TYPE, TEST_ACTION))
                .thenReturn(controllerSelectionReference);
    }

    @Test
    public void testExecution_validInput_expectedOutput() {
        controllerExecutionBB.execute(execution);
        verify(execution).setVariable("stage", "ready");
        verify(execution).setVariable("stage", "prepare");
        verify(execution).setVariable("stage", "run");
    }

    @Test
    public void testGetController_availableInExecutionContext_returnFromExecutionContext() {
        String controllerActor = controllerExecutionBB.getControllerActor(execution, TEST_SCOPE, "", TEST_ACTION);
        assertEquals(TEST_ACTOR, controllerActor);
    }

    @Test
    public void testGetController_soRefDataInExecutionContext_returnFromControllerReference() {
        when(execution.getVariable(ACTOR_PARAM)).thenReturn("so-ref-data");
        String controllerActor = controllerExecutionBB.getControllerActor(execution, TEST_SCOPE, "", TEST_ACTION);
        assertEquals(TEST_CONTROLLER_REFERENCE_ACTOR, controllerActor);
    }

    @Test
    public void testGetController_notAvailableInExecutionContext_returnFromCatalogdb() {
        when(execution.getVariable(ACTOR_PARAM)).thenReturn("");
        String controllerActor = controllerExecutionBB.getControllerActor(execution, TEST_SCOPE,
                TEST_RESOURCE_CUSTOMIZATION_UUID, TEST_ACTION);
        assertEquals("The controller actor should be the same as in the catalogdb", TEST_CATALOGDB_CONTROLLER_ACTOR,
                controllerActor);
    }

    @Test
    public void testGetController_notAvailableInExecutionContextAndSoRefDataInCatalogdb_returnFromControllerReference() {
        when(execution.getVariable(ACTOR_PARAM)).thenReturn("");
        when(pnfResourceCustomization.getControllerActor()).thenReturn("SO-REF-DATA");
        String controllerActor = controllerExecutionBB.getControllerActor(execution, TEST_SCOPE,
                TEST_RESOURCE_CUSTOMIZATION_UUID, TEST_ACTION);
        assertEquals("The controller actor should be the same as in the catalogdb", TEST_CONTROLLER_REFERENCE_ACTOR,
                controllerActor);
    }

    @Test
    public void testGetController_notAvailableInExecutionContextAndCatalogdb_returnFromControllerReference() {
        when(execution.getVariable(ACTOR_PARAM)).thenReturn("");
        when(pnfResourceCustomization.getControllerActor()).thenReturn("");

        String controllerActor = controllerExecutionBB.getControllerActor(execution, TEST_SCOPE,
                TEST_RESOURCE_CUSTOMIZATION_UUID, TEST_ACTION);
        assertEquals("The controller actor should be the same as in the controller reference table",
                TEST_CONTROLLER_REFERENCE_ACTOR, controllerActor);
    }

    @Test
    public void testGetController_notAvailableInExecutionContextVnfType_returnFromCatalogdb() {
        when(execution.getVariable(ACTOR_PARAM)).thenReturn("");

        String expectedVnfControllerActor = "appc";

        String[] controllerScopeArray = new String[] {"vnf", "vfModule"};

        for (String controllerScope : controllerScopeArray) {

            when(vnfResourceCustomization.getControllerActor()).thenReturn(expectedVnfControllerActor);
            when(client.getVnfResourceCustomizationByModelCustomizationUUID(TEST_RESOURCE_CUSTOMIZATION_UUID))
                    .thenReturn(vnfResourceCustomization);

            String controllerActor = controllerExecutionBB.getControllerActor(execution, controllerScope,
                    TEST_RESOURCE_CUSTOMIZATION_UUID, TEST_ACTION);

            assertEquals("The controller actor should be the same as in the catalogdb for scope: " + controllerScope,
                    expectedVnfControllerActor, controllerActor);
        }
    }

    @Test
    public void testHandleFailure() {
        when(execution.getVariable(PayloadConstants.CONTROLLER_ERROR_MESSAGE)).thenReturn("ERROR MESSAGE");

        controllerExecutionBB.handleFailure(execution);

        verify(exceptionBuilder).buildAndThrowWorkflowException(execution, 9003, "ERROR MESSAGE", ONAPComponents.SO);
    }

    @Test
    public void testHandleTimeoutFailure() {
        when(execution.getVariable(PayloadConstants.CONTROLLER_MSG_TIMEOUT_REACHED)).thenReturn(true);

        controllerExecutionBB.handleFailure(execution);

        ArgumentCaptor<String> errMsgCaptor = ArgumentCaptor.forClass(String.class);
        verify(exceptionBuilder, times(1)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), anyInt(),
                errMsgCaptor.capture(), any());

        assertTrue(errMsgCaptor.getValue().contains("timeout"));
    }
}
