/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.manualhandling.tasks;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.ticket.ExternalTicket;

public class ExternalTicketTasksTest extends BaseTaskTest {

    @Mock
    private BuildingBlockExecution buildingBlockExecution;

    @Mock
    private GeneralBuildingBlock generalBuildingBlock;

    @Mock
    private RequestContext requestContext;

    @Mock
    private ExternalTicket MOCK_externalTicket;

    @Before
    public void before() {
        delegateExecution = new DelegateExecutionFake();
        buildingBlockExecution = new DelegateExecutionImpl(delegateExecution);
        generalBuildingBlock = new GeneralBuildingBlock();
        requestContext = new RequestContext();
        requestContext.setRequestorId("someRequestorId");
        generalBuildingBlock.setRequestContext(requestContext);
        buildingBlockExecution.setVariable("mso-request-id", ("testMsoRequestId"));
        buildingBlockExecution.setVariable("vnfType", "testVnfType");
        buildingBlockExecution.setVariable("gBBInput", generalBuildingBlock);
        buildingBlockExecution.setVariable("rainyDayVnfName", "someVnfName");
        buildingBlockExecution.setVariable("workStep", "someWorkstep");
        buildingBlockExecution.setVariable("taskTimeout", "PT5M");
    }

    @Test
    public void createExternalTicket_Test() throws Exception {
        ExternalTicketTasks externalTicketTasksSpy = spy(new ExternalTicketTasks());
        when(externalTicketTasksSpy.getExternalTicket()).thenReturn(MOCK_externalTicket);
        externalTicketTasksSpy.createExternalTicket(buildingBlockExecution);
        verify(MOCK_externalTicket, times(1)).createTicket();
    }
}
