/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.onap.so.bpmn.infrastructure.process;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.beans.nsmf.oof.SubnetType;

public class NsiAllocationRoutingTest extends NsiAllocationProcessTestSupport {
    @Before
    public void retainActivityHistoryForRoutingAssertions() {
        // Full-history overflow is guarded in NsiAllocationPersistenceTest; it otherwise prevents route assertions.
        ProcessEngineConfigurationImpl configuration =
                (ProcessEngineConfigurationImpl) engine.getProcessEngineConfiguration();
        configuration.setHistoryLevel(HistoryLevel.HISTORY_LEVEL_ACTIVITY);
        assertEquals("activity", configuration.getHistoryLevel().getName());
    }

    @Test
    public void fronthaulContinuesThroughMidhaulAndBackhaulWithIndependentIdentities() {
        ProcessInstance process = start(true, "Activity_0z1ympd");
        completed(process, Arrays.asList(SubnetType.TN_FH, SubnetType.TN_MH, SubnetType.TN_BH), 15);
        activity(process, "Activity_129ztck", 1);
        activity(process, "Activity_0fvj9vy", 1);
        activity(process, "CallActivity_0b28wlb", 1);
        activity(process, "Activity_0zlfgf5", 1);
        activity(process, "ScriptTask_0stnvp3", 1);
    }

    @Test
    public void accessAndCoreJoinBeforeSelectingBackhaulOnly() {
        ProcessInstance process = start(false, "Task_0dkfe9n", "Task_0fwhsq2");
        completed(process, Arrays.asList(SubnetType.AN, SubnetType.CN, SubnetType.TN_BH), 17);
        activity(process, "CallActivity_1yh9tiq", 1);
        activity(process, "CallActivity_1ixah3o", 1);
        activity(process, "Gateway_0tpcoai", 1);
        activity(process, "ScriptTask_0stnvp3", 1);
        activity(process, "Activity_0z1ympd", 0);
        activity(process, "Activity_0zlfgf5", 0);
    }

    @Test
    public void accessAndCoreJoinBeforeSelectingFronthaulMidhaulAndBackhaul() {
        ProcessInstance process = start(true, "Task_0dkfe9n", "Task_0fwhsq2");
        completed(process, domains(), 27);
        activity(process, "CallActivity_1yh9tiq", 1);
        activity(process, "CallActivity_1ixah3o", 1);
        activity(process, "Gateway_0tpcoai", 1);
        activity(process, "Activity_0z1ympd", 1);
        activity(process, "Activity_0zlfgf5", 1);
    }
}
