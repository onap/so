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

package org.onap.so.bpmn.infrastructure.aai;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;

public class AAIDeleteServiceInstanceTest {
    private AAIDeleteServiceInstance aaiDeleteServiceInstance;
    @Mock
    private DelegateExecution execution;

    @Mock
    private AAIResourcesClient aaiResourcesClient;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);

        aaiDeleteServiceInstance = new AAIDeleteServiceInstance();
        aaiDeleteServiceInstance.setAaiClient(aaiResourcesClient);
    }

    @Test
    public void executeTest() throws Exception {
        doReturn("serviceInstanceId").when(execution).getVariable("serviceInstanceId");
        doNothing().when(aaiResourcesClient).delete(isA(AAIResourceUri.class));
        doNothing().when(execution).setVariable(isA(String.class), isA(Boolean.class));

        aaiDeleteServiceInstance.execute(execution);

        verify(execution, times(1)).getVariable("serviceInstanceId");
        verify(aaiResourcesClient, times(1)).delete(isA(AAIResourceUri.class));
        verify(execution, times(1)).setVariable("GENDS_SuccessIndicator", true);
    }

    @Test
    public void executeExceptionTest() throws Exception {
        expectedException.expect(BpmnError.class);

        doReturn("testProcessKey").when(execution).getVariable("testProcessKey");
        doReturn("serviceInstanceId").when(execution).getVariable("serviceInstanceId");
        doThrow(RuntimeException.class).when(aaiResourcesClient).delete(isA(AAIResourceUri.class));

        aaiDeleteServiceInstance.execute(execution);
    }
}
