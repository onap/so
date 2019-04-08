/*
 * ============LICENSE_START======================================================= ONAP : SO
 * ================================================================================ Copyright (C) 2018 AT&T Intellectual
 * Property. All rights reserved. ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;

public class WorkflowExceptionTest {

    private WorkflowException workflowException;

    @Test
    public void test() {
        String processKey = "AnyProcessKey";
        int errorCode = 200;
        String errorMessage = "any error message!";
        workflowException = new WorkflowException(processKey, errorCode, errorMessage);
        assertEquals(errorCode, workflowException.getErrorCode());
        assertEquals(errorMessage, workflowException.getErrorMessage());
        assertEquals(processKey, workflowException.getProcessKey());
        assertEquals("*", workflowException.getWorkStep());
        String workStep = "one";
        workflowException = new WorkflowException(processKey, errorCode, errorMessage, workStep);
        assertEquals(workStep, workflowException.getWorkStep());
        assertNotEquals(null, workflowException.toString());
    }
}
