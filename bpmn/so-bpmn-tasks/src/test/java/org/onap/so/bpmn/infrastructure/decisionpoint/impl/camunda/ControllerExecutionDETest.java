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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda;

import static org.junit.Assert.assertTrue;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.client.exception.ExceptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ControllerExecutionDE.class, MockControllerDE.class, ExceptionBuilder.class})
public class ControllerExecutionDETest {

    private static final String ACTOR_PARAM = "actor";
    private static final String ACTION_PARAM = "action";

    @Autowired
    private ControllerExecutionDE controllerExecutionDE;

    private DelegateExecution execution = new DelegateExecutionFake();

    @Before
    public void setUp() {
        execution.setVariable(ACTION_PARAM, MockControllerDE.TEST_ACTION);
        execution.setVariable(ACTOR_PARAM, MockControllerDE.TEST_ACTOR);
    }

    @Test
    public void testExecution_validInput_expectedOutput() {
        controllerExecutionDE.execute(execution);
        assertTrue((Boolean) execution.getVariable("ready"));
        assertTrue((Boolean) execution.getVariable("prepare"));
        assertTrue((Boolean) execution.getVariable("run"));
    }
}
