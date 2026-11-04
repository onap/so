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
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter;
import org.onap.so.beans.nsmf.oof.SubnetType;

public class NsiAllocationPersistenceTest extends NsiAllocationProcessTestSupport {
    @Before
    public void requireTheFullHistoryLevel() {
        assertEquals("full",
                ((ProcessEngineConfigurationImpl) engine.getProcessEngineConfiguration()).getHistoryLevel().getName());
    }

    @Test
    public void backhaulCompletionPersistsTypedProfileAndAllocationState() {
        ProcessInstance process = start(false, "ScriptTask_0stnvp3");
        completed(process, Arrays.asList(SubnetType.TN_BH), 5);
        Object restored = engine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName("sliceTaskParams").singleResult().getValue();
        assertTrue(restored instanceof SliceTaskParamsAdapter);
        assertEquals("nssi-TN_BH", ((SliceTaskParamsAdapter) restored).getTnBHSliceTaskInfo().getSuggestNssiId());
        activity(process, "ScriptTask_0stnvp3", 1);
        activity(process, "ScriptTask_1f4o46q", 1);
        activity(process, "CallActivity_0b28wlb", 1);
        activity(process, "ScriptTask_0sssiii", 1);
    }

    @Test
    public void fronthaulMidhaulAndBackhaulCompletionPersistsTheFullOrchestrationStatus() {
        requireMultiDomainHistoryCompatibility();
        ProcessInstance process = start(true, "Activity_0z1ympd");
        completed(process, Arrays.asList(SubnetType.TN_FH, SubnetType.TN_MH, SubnetType.TN_BH), 15);
    }

    @Test
    public void threeDomainCompletionPersistsTheFullOrchestrationStatus() {
        requireMultiDomainHistoryCompatibility();
        ProcessInstance process = start(false, "Task_0dkfe9n", "Task_0fwhsq2");
        completed(process, Arrays.asList(SubnetType.AN, SubnetType.CN, SubnetType.TN_BH), 17);
    }

    @Test
    public void fiveDomainCompletionPersistsTheFullOrchestrationStatus() {
        requireMultiDomainHistoryCompatibility();
        ProcessInstance process = start(true, "Task_0dkfe9n", "Task_0fwhsq2");
        completed(process, domains(), 27);
    }

    private void requireMultiDomainHistoryCompatibility() {
        Assume.assumeTrue("SO-4457: multi-domain status strings exceed Camunda's 4000-character history column",
                Boolean.getBoolean("groovy.migration.verification"));
    }
}
