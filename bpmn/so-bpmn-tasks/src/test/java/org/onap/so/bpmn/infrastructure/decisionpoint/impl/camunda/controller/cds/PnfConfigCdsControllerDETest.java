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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda.controller.cds;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.controller.ControllerPreparable;
import org.onap.so.client.cds.AbstractCDSProcessingBBUtils;
import org.onap.so.client.exception.ExceptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = {PnfConfigCdsControllerDE.class, ExceptionBuilder.class, AbstractCDSProcessingBBUtils.class})
public class PnfConfigCdsControllerDETest {

    @Autowired
    private PnfConfigCdsControllerDE pnfConfigCdsControllerDE;

    @MockBean
    private ControllerContext controllerContext;

    @MockBean
    private ControllerPreparable<DelegateExecution> preparable;

    @MockBean
    private AbstractCDSProcessingBBUtils abstractCDSProcessingBBUtils;

    @Test
    public void testUnderstand_action_assign_TrueReturned() {
        // when
        when(controllerContext.getControllerActor()).thenReturn("cds");
        when(controllerContext.getControllerScope()).thenReturn("pnf");
        when(controllerContext.getControllerAction()).thenReturn("config-assign");

        // verify
        assertTrue(pnfConfigCdsControllerDE.understand(controllerContext));
    }

    @Test
    public void testUnderstand_action_deploy_TrueReturned() {
        // when
        when(controllerContext.getControllerActor()).thenReturn("cds");
        when(controllerContext.getControllerScope()).thenReturn("pnf");
        when(controllerContext.getControllerAction()).thenReturn("config-deploy");

        // verify
        assertTrue(pnfConfigCdsControllerDE.understand(controllerContext));
    }

    @Test
    public void testUnderstand_action_any_FalseReturned() {
        // when
        when(controllerContext.getControllerActor()).thenReturn("cds");
        when(controllerContext.getControllerScope()).thenReturn("pnf");
        when(controllerContext.getControllerAction()).thenReturn("any-action");

        // verify
        assertFalse(pnfConfigCdsControllerDE.understand(controllerContext));
    }

    @Test
    public void testUnderstand_invalidContext_FalseReturned() {
        // when
        when(controllerContext.getControllerActor()).thenReturn("appc");

        // verify
        assertFalse(pnfConfigCdsControllerDE.understand(controllerContext));
    }

}
