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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.client.exception.ExceptionBuilder;
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

    @Autowired
    private ControllerExecutionBB controllerExecutionBB;

    @MockBean
    private CatalogDbClient catalogDbClient;

    @MockBean
    private BuildingBlockExecution execution;

    @Before
    public void setUp() {
        when(execution.getVariable(ACTOR_PARAM)).thenReturn(MockControllerBB.TEST_ACTOR);
        when(execution.getVariable(ACTION_PARAM)).thenReturn(MockControllerBB.TEST_ACTION);
    }

    @Test
    public void testExecution_validInput_expectedOutput() {
        controllerExecutionBB.execute(execution);
        verify(execution).setVariable("stage", "ready");
        verify(execution).setVariable("stage", "prepare");
        verify(execution).setVariable("stage", "run");
    }
}
