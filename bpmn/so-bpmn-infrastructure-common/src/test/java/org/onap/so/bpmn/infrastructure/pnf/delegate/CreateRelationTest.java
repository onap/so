/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Nokia.
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

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.infrastructure.pnf.aai.AaiConnectionImpl;
import org.onap.so.bpmn.infrastructure.pnf.implementation.AaiConnection;

public class CreateRelationTest {

    private static final String SERVICE_INSTANCE_ID = "serviceTest";
    private static final String PNF_NAME = "pnfNameTest";

    private DelegateExecutionFake executionFake;

    @Before
    public void setUp() {
        executionFake = new DelegateExecutionFake();
        executionFake.setVariable("serviceInstanceId", SERVICE_INSTANCE_ID);
        executionFake.setVariable("correlationId", PNF_NAME);
    }

    @Test
    public void createRelationSuccessful() throws IOException {
        // given
        AaiConnection aaiConnectionMock = mock(AaiConnectionImpl.class);
        CreateRelation testedObject = new CreateRelation(aaiConnectionMock);
        // when
        testedObject.execute(executionFake);
        // then
        verify(aaiConnectionMock).createRelation(SERVICE_INSTANCE_ID, PNF_NAME);
    }

    @Test
    public void shouldThrowBpmnErrorWhenExceptionOccurred() {
        CreateRelation testedObject = new CreateRelation(new AaiConnectionThrowingException());
        executionFake.setVariable("testProcessKey", "testProcessKeyValue");

        assertThatThrownBy(() -> testedObject.execute(executionFake)).isInstanceOf(BpmnError.class);
    }
}
