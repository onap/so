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
import org.junit.Test;

public class BadInjectedFiledExceptionTest {

    private BadInjectedFieldException badInjectedFieldException;

    @Test
    public void test() {
        String fieldName = "anyFieldName";
        String taskName = "anyTask";
        Object info = new String("info");

        String expectedMessage1 = taskName + " injected field '" + fieldName + "' is bad: " + (String) info;
        badInjectedFieldException = new BadInjectedFieldException(fieldName, taskName, info);
        assertEquals(expectedMessage1, badInjectedFieldException.getMessage());

        String expectedMessage2 = "java.lang.Throwable: anyCause";
        Throwable cause = new Throwable("anyCause");
        badInjectedFieldException = new BadInjectedFieldException(fieldName, taskName, info, cause);
        assertEquals(expectedMessage2, badInjectedFieldException.getCause().toString());

    }
}
