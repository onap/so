/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Intellectual Property. All rights reserved.
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

package org.onap.so.client.aai;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Rule;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.camunda.bpm.engine.delegate.BpmnError;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import org.junit.rules.ExpectedException;
import org.onap.so.bpmn.core.domain.LogicalLink;
import org.onap.so.bpmn.core.domain.PInterface;
import org.onap.so.bpmn.core.domain.EsrThirdpartySdnc;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.infrastructure.aai.OpticalAAIRestClientImpl;

public class OpticalAAIRestClientTest {

    private OpticalAAIRestClientImpl aaiRestClient;

    @Mock
    private DelegateExecution execution;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void executeTest() throws Exception {
        doReturn("serviceInstanceId").when(execution).getVariable("serviceInstanceId");
        doNothing().when(execution).setVariable(isA(String.class), isA(Boolean.class));
        execution = mock(DelegateExecution.class);

        aaiRestClient.getSdncResourceFromJson(execution, "jsonResp", "resourceName");
        aaiRestClient.getLLResourceFromJson(execution, "jsonResp", "resourceName");
        aaiRestClient.getPifResourceFromJson(execution, "jsonResp", "resourceName");
        EsrThirdpartySdnc esrThirdpartySdnc = aaiRestClient.getDomainControllerByIf(execution, "portId");
        LogicalLink logicalLink = aaiRestClient.getInterDomainLink(execution, "controller1");
        PInterface pInterface = aaiRestClient.getInterfaceDetails(execution, "portId");

        verify(execution, times(1)).getVariable("serviceInstanceId");
        verify(execution, times(1)).setVariable("GENDS_SuccessIndicator", true);
    }

    @Test
    public void executeExceptionTest() throws Exception {
        expectedException.expect(BpmnError.class);

        doReturn("testProcessKey").when(execution).getVariable("testProcessKey");
        doReturn("serviceInstanceId").when(execution).getVariable("serviceInstanceId");
        // doThrow(RuntimeException.class).when(aaiRestClient).getDomainControllerByIf(execution, "controller1",
        // "controller2");

    }

}
