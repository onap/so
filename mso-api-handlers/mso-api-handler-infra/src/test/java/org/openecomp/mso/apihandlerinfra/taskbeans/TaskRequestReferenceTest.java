/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.apihandlerinfra.taskbeans;

import org.junit.After;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.apihandlerinfra.tasksbeans.TaskRequestReference;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TaskRequestReferenceTest {

    TaskRequestReference _taskRequestReference;

    protected String _taskId;

    public TaskRequestReferenceTest() {
    }

    @Before
    public void setUp() {
        _taskRequestReference = mock(TaskRequestReference.class);
        _taskId = "taskid";

        when(_taskRequestReference.getTaskId()).thenReturn(_taskId);
    }

    @After
    public void tearDown() {
        _taskRequestReference = null;
    }

    /**
     * Test getTaskRequestReference
     */
    @Test
    public void taskGetRequestReference() {
        String result = _taskRequestReference.getTaskId();
        assertEquals(_taskId, result);
    }

    /**
     * Test setTaskRequestReference
     */
    @Test
    public void testSetRequestInfo() {
        _taskRequestReference.setTaskId(_taskId);
        verify(_taskRequestReference).setTaskId(_taskId);
    }
}
