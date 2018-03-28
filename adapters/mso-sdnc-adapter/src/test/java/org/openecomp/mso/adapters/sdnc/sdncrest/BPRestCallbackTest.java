/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *l
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.adapters.sdnc.sdncrest;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;


public class BPRestCallbackTest {

    @Test
    public void testBPRestCallbackException() {

        BPRestCallback test = new BPRestCallback();
        test.send("workflow/","messageType","correlator","message");
        assertFalse(test.send("workflowMessageUrl/", "messageType", "correlator", "message"));

    }
}
