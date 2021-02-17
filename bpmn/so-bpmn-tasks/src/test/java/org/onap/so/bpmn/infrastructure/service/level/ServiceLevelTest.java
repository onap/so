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
import java.util.ArrayList;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Test;
import java.util.List;
import org.onap.so.bpmn.infrastructure.service.level.impl.ServiceLevelConstants;

public class ServiceLevelTest {

    private static final String EXECUTION_KEY_PNF_NAME_LIST = "pnfNameList";
    private static final String EXECUTION_KEY_PNF_COUNTER = "pnfCounter";

    @Test
    public void pnfCounterExecution_success() {
        // given
        String pnfName = "pnfName1";;
        DelegateExecution execution = new DelegateExecutionFake();
        execution.setVariable(EXECUTION_KEY_PNF_NAME_LIST, createPnfNameList(pnfName));
        execution.setVariable(EXECUTION_KEY_PNF_COUNTER, 0);
        // when
        new ServiceLevel().pnfCounterExecution(execution);
        // then
        assertThat(execution.getVariable(ServiceLevelConstants.PNF_NAME)).isEqualTo(pnfName);
        assertThat(execution.getVariable(EXECUTION_KEY_PNF_COUNTER)).isEqualTo(1);
    }

    private List<String> createPnfNameList(String pnfName) {
        List<String> pnfNameList = new ArrayList<>();
        pnfNameList.add(pnfName);
        return pnfNameList;
    }
}
