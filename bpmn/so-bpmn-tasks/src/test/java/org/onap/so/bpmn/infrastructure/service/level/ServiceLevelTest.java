/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nokia
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.service.level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.infrastructure.service.level.impl.ServiceLevelConstants;
import org.onap.so.client.exception.ExceptionBuilder;

@RunWith(MockitoJUnitRunner.class)
public class ServiceLevelTest {

    private static final String EXECUTION_KEY_PNF_NAME_LIST = "pnfNameList";
    private static final String EXECUTION_KEY_PNF_COUNTER = "pnfCounter";
    private static final String PARAM_NAME = "param1";
    private static final String SCOPE = "scope1";
    private static final String PNF_NAME = "pnfName1";

    @Mock
    private ExceptionBuilder exceptionBuilderMock;
    @InjectMocks
    private ServiceLevel testedObject;

    private DelegateExecution execution;

    @Before
    public void init() {
        execution = new DelegateExecutionFake();
    }

    @Test
    public void pnfCounterExecution_success() {
        // given
        execution.setVariable(EXECUTION_KEY_PNF_NAME_LIST, createPnfNameList());
        execution.setVariable(EXECUTION_KEY_PNF_COUNTER, 0);
        // when
        testedObject.pnfCounterExecution(execution);
        // then
        assertThat(execution.getVariable(ServiceLevelConstants.PNF_NAME)).isEqualTo(PNF_NAME);
        assertThat(execution.getVariable(EXECUTION_KEY_PNF_COUNTER)).isEqualTo(1);
    }

    @Test
    public void validateParams_success_paramExistsInExecution() {
        // given
        execution.setVariable(PARAM_NAME, "anyValue");
        // when
        testedObject.validateParamsWithScope(execution, "anyScope", createParamList());
        // then
        verify(exceptionBuilderMock, times(0)).buildAndThrowWorkflowException(any(DelegateExecution.class),
                eq(ServiceLevelConstants.ERROR_CODE), any(String.class));
    }

    @Test
    public void validateParams_exceptionParamDoesNotExistInExecution() {
        // when
        testedObject.validateParamsWithScope(execution, SCOPE, createParamList());
        // then
        verify(exceptionBuilderMock).buildAndThrowWorkflowException(execution, ServiceLevelConstants.ERROR_CODE,
                "Validation of health check workflow parameters failed for the scope: " + SCOPE);
    }

    private List<String> createParamList() {
        List<String> params = new ArrayList<>();
        params.add(PARAM_NAME);
        return params;
    }

    private List<String> createPnfNameList() {
        List<String> pnfNameList = new ArrayList<>();
        pnfNameList.add(PNF_NAME);
        return pnfNameList;
    }
}
