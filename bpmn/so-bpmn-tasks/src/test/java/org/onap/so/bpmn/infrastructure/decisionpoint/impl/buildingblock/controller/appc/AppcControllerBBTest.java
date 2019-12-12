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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock.controller.appc;


import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.client.appc.ApplicationControllerAction;
import org.onap.so.client.exception.ExceptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppcControllerBB.class, ExceptionBuilder.class})
public class AppcControllerBBTest {

    @Autowired
    private AppcControllerBB appcControllerBB;

    @MockBean
    private ControllerContext controllerContext;

    @MockBean
    protected ApplicationControllerAction client;

    @Before
    public void setUp() {
        when(controllerContext.getControllerActor()).thenReturn("appc");
    }

    @Test
    public void testUnderstand_validContext_TrueReturned() {
        assertTrue(appcControllerBB.understand(controllerContext));
    }

    @Test
    public void testUnderstand_invalidContext_FalseReturned() {
        when(controllerContext.getControllerActor()).thenReturn("sdnc");
        assertFalse(appcControllerBB.understand(controllerContext));
    }
}
