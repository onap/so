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

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.onap.so.bpmn.BaseTaskTest;

public class ActivateVfModuleTest extends BaseTaskTest {

    @InjectMocks
    private ActivateVfModule activateVfModule = new ActivateVfModule();

    @Test
    public void setWaitBeforeDurationTest() throws Exception {
        when(env.getProperty(ActivateVfModule.VF_MODULE_TIMER_DURATION_PATH, ActivateVfModule.DEFAULT_TIMER_DURATION))
                .thenReturn("PT300S");
        activateVfModule.setTimerDuration(execution);
        verify(env, times(1)).getProperty(ActivateVfModule.VF_MODULE_TIMER_DURATION_PATH,
                ActivateVfModule.DEFAULT_TIMER_DURATION);
        assertEquals("PT300S", (String) execution.getVariable("vfModuleActivateTimerDuration"));
    }

}
